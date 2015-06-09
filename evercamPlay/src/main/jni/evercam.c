#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <jni.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <gst/gst.h>
#include <gst/video/video.h>
#include <pthread.h>

GST_DEBUG_CATEGORY_STATIC (debug_category);
#define GST_CAT_DEFAULT debug_category

/*
 * These macros provide a way to store the native pointer to CustomData, which might be 32 or 64 bits, into
 * a jlong, which is always 64 bits, without warnings.
 */
#if GLIB_SIZEOF_VOID_P == 8
# define GET_CUSTOM_DATA(env, thiz, fieldID) (CustomData *)(*env)->GetLongField (env, thiz, fieldID)
# define SET_CUSTOM_DATA(env, thiz, fieldID, data) (*env)->SetLongField (env, thiz, fieldID, (jlong)data)
#else
# define GET_CUSTOM_DATA(env, thiz, fieldID) (CustomData *)(jint)(*env)->GetLongField (env, thiz, fieldID)
# define SET_CUSTOM_DATA(env, thiz, fieldID, data) (*env)->SetLongField (env, thiz, fieldID, (jlong)(jint)data)
#endif

/* Structure to contain all our information, so we can pass it to callbacks */
typedef struct _CustomData {
    jobject app;                  /* Application instance, used to call its methods. A global reference is kept. */
    GstElement *pipeline;         /* The running pipeline */
    GstElement *rtspsrc;          /* The rtspsrc */
    GMainContext *context;        /* GLib context used to run the main loop */
    GMainLoop *main_loop;         /* GLib main loop */
    gboolean initialized;         /* To avoid informing the UI multiple times about the initialization */
    GstElement *video_sink;       /* The video sink element which receives XOverlay commands */
    ANativeWindow *native_window; /* The Android native window where video will be rendered */
    gint tcp_timeout;             /* tcp timeout for rtspsrc */
    GstState target_state;        /* Target pipeline state for playing errors detection */
    gboolean busy_in_conversion;  /* TRUE if sample conversion in progress */
} CustomData;

typedef struct {
    GstCaps    *caps;             /* Target frame caps */
    GstSample  *sample;           /* Sample for conversion */
    CustomData *data;             /* Global data for using CustomData::busy_in_conversion and call java stuff */
} ConvertSampleContext;


/* These global variables cache values which are not changing during execution */
static pthread_t gst_app_thread;
static pthread_key_t current_jni_env;
static JavaVM *java_vm;
static jfieldID custom_data_field_id;
static jmethodID on_stream_loaded_method_id;
static jmethodID on_stream_load_failed_method_id;
static jmethodID on_request_sample_failed_method_id;
static jmethodID on_request_sample_seccess_method_id;

/*
 * Private methods
 */

/* Register this thread with the VM */
static JNIEnv *attach_current_thread (void) {
    JNIEnv *env;
    JavaVMAttachArgs args;

    GST_DEBUG ("Attaching thread %p", g_thread_self ());
    args.version = JNI_VERSION_1_4;
    args.name = NULL;
    args.group = NULL;

    if ((*java_vm)->AttachCurrentThread (java_vm, &env, &args) < 0) {
        GST_ERROR ("Failed to attach current thread");
        return NULL;
    }

    return env;
}

/* Unregister this thread from the VM */
static void detach_current_thread (void *env) {
    GST_DEBUG ("Detaching thread %p", g_thread_self ());
    (*java_vm)->DetachCurrentThread (java_vm);
}

/* Retrieve the JNI environment for this thread */
static JNIEnv *get_jni_env (void) {
    JNIEnv *env;

    if ((env = pthread_getspecific (current_jni_env)) == NULL) {
        env = attach_current_thread ();
        pthread_setspecific (current_jni_env, env);
    }

    return env;
}

// Source callbacks

/* This function is called when playbin has created the rtsprc element, so we have
 * a chance to configure it. */
static void handle_source_setup (GstElement *pipeline, GstElement *source, CustomData *data)
{
    if (data->tcp_timeout > 0)
        g_object_set (G_OBJECT (source), "tcp-timeout", data->tcp_timeout, NULL);

    g_object_set (G_OBJECT (source), "latency", 0, NULL);
    g_object_set (G_OBJECT (source), "drop-on-latency", 1, NULL);
    g_object_set (G_OBJECT (source), "protocols", 4, NULL);

}

// handle video channel setup
static void handle_video_changed(GstElement *playbin, CustomData *data)
{
    if (data->target_state == GST_STATE_PLAYING) {
        JNIEnv *env = get_jni_env ();

        (*env)->CallVoidMethod (env, data->app, on_stream_loaded_method_id);

        if ((*env)->ExceptionCheck (env)) {
            GST_ERROR ("Failed to call Java method");
            (*env)->ExceptionClear (env);
        }
    }

    data->target_state = GST_STATE_NULL;

    GstElement *video_sink;
    g_object_get(playbin, "video-sink", &video_sink, NULL);

    if (video_sink != NULL)
        g_object_set(video_sink, "sync", FALSE, NULL);
    else
        GST_DEBUG("Could not to get video_sink");
}

/* Change the content of the UI's TextView */
static void set_ui_message (const gchar *message, CustomData *data) {
    //JNIEnv *env = get_jni_env ();
    GST_DEBUG ("Setting message to: %s", message);
/*    jstring jmessage = (*env)->NewStringUTF(env, message);
    (*env)->CallVoidMethod (env, data->app, set_message_method_id, jmessage);
    if ((*env)->ExceptionCheck (env)) {
        GST_ERROR ("Failed to call Java method");
        (*env)->ExceptionClear (env);
    }
    (*env)->DeleteLocalRef (env, jmessage);*/
}

/* Retrieve errors from the bus and show them on the UI */
static void error_cb (GstBus *bus, GstMessage *msg, CustomData *data) {
    GError *err;
    gchar *debug_info;
    gchar *message_string;

    gst_message_parse_error (msg, &err, &debug_info);
    message_string = g_strdup_printf ("Error received from element %s: %s", GST_OBJECT_NAME (msg->src), err->message);
    g_clear_error (&err);
    g_free (debug_info);
    set_ui_message (message_string, data);
    g_free (message_string);

    if (data->target_state == GST_STATE_PLAYING) {
        JNIEnv *env = get_jni_env ();

        (*env)->CallVoidMethod (env, data->app, on_stream_load_failed_method_id);

        if ((*env)->ExceptionCheck (env)) {
            GST_ERROR ("Failed to call Java method");
            (*env)->ExceptionClear (env);
        }
    }

    data->target_state = GST_STATE_NULL;
    gst_element_set_state (data->pipeline, GST_STATE_NULL);
}

/* Notify UI about pipeline state changes */
static void state_changed_cb (GstBus *bus, GstMessage *msg, CustomData *data) {
    GstState old_state, new_state, pending_state;
    gst_message_parse_state_changed (msg, &old_state, &new_state, &pending_state);
    /* Only pay attention to messages coming from the pipeline, not its children */
    if (GST_MESSAGE_SRC (msg) == GST_OBJECT (data->pipeline)) {
        gchar *message = g_strdup_printf("State changed to %s", gst_element_state_get_name(new_state));
        set_ui_message(message, data);
        g_free (message);
    }
}

static void notify_about_get_sample_failed(CustomData *data)
{
    JNIEnv *env = get_jni_env ();

    (*env)->CallVoidMethod (env, data->app, on_request_sample_failed_method_id);

    if ((*env)->ExceptionCheck (env)) {
        GST_ERROR ("Failed to call Java method");
        (*env)->ExceptionClear (env);
    }
}

/* Handle sample conversion */
static void process_converted_sample(GstSample *sample, GError *err, ConvertSampleContext *data)
{
    gst_caps_unref(data->caps);

    if (err == NULL) {
        if (sample != NULL) {
            GstBuffer *buf = gst_sample_get_buffer(sample);
            GstMapInfo info;
            gst_buffer_map (buf, &info, GST_MAP_READ);

            JNIEnv *env = get_jni_env ();
            jbyteArray array = (*env)->NewByteArray(env, info.size);
            (*env)->SetByteArrayRegion(env, array, 0, info.size, info.data);
            (*env)->CallVoidMethod (env, data->data->app, on_request_sample_seccess_method_id, array, info.size);

            if ((*env)->ExceptionCheck (env)) {
                GST_ERROR ("Failed to call Java method");
                (*env)->ExceptionClear (env);
            }

            gst_buffer_unmap (buf, &info);
            gst_sample_unref(sample);
        }
    }
    else {
        g_error_free(err);
        notify_about_get_sample_failed(data->data);
    }

    gst_sample_unref(sample);
    gst_sample_unref(data->sample);
    gst_caps_unref(data->caps);
}

/* Sample pthread function */

static void *convert_thread_func(void *arg)
{
    ConvertSampleContext *data = (ConvertSampleContext*) arg;
    GError *err = NULL;
    GstSample *sample = gst_video_convert_sample(data->sample, data->caps, GST_CLOCK_TIME_NONE, &err);
    process_converted_sample(sample, err, data);
    g_free(data);
    data->data->busy_in_conversion = FALSE;
    return NULL;
}

/* Asynchronous function for converting frame */
static void convert_sample(ConvertSampleContext *ctx)
{
    ctx->data->busy_in_conversion = TRUE;
    pthread_t thread;

    if (pthread_create(&thread, NULL, convert_thread_func, ctx) != 0)
        GST_DEBUG("Strange, but can't create sample conversion thread");
}

/* Check if all conditions are met to report GStreamer as initialized.
 * These conditions will change depending on the application */
static void check_initialization_complete (CustomData *data) {
    JNIEnv *env = get_jni_env ();
    if (!data->initialized && data->native_window && data->main_loop) {
        GST_DEBUG ("Initialization complete, notifying application. native_window:%p main_loop:%p", data->native_window, data->main_loop);

        /* The main loop is running and we received a native window, inform the sink about it */
        if (data->native_window != 0)
            gst_video_overlay_set_window_handle (GST_VIDEO_OVERLAY (data->video_sink), (guintptr)data->native_window);

        data->initialized = TRUE;
    }
}

/* Main method for the native code. This is executed on its own thread. */
static void *app_function (void *userdata) {
    JavaVMAttachArgs args;
    GstBus *bus;
    CustomData *data = (CustomData *)userdata;
    data->tcp_timeout = 0;
    data->target_state = GST_STATE_NULL;
    data->busy_in_conversion = FALSE;
    GSource *bus_source;
    GError *error = NULL;

    GST_DEBUG ("Creating pipeline in CustomData at %p", data);

    /* Create our own GLib Main Context and make it the default one */
    data->context = g_main_context_new ();
    g_main_context_push_thread_default(data->context);

    /* Build pipeline */
    data->pipeline = gst_parse_launch("playbin", &error);
    if (error) {
        gchar *message = g_strdup_printf("Unable to build pipeline: %s", error->message);
        g_clear_error (&error);
        set_ui_message(message, data);
        g_free (message);
        return NULL;
    }

    g_signal_connect (data->pipeline, "source-setup", G_CALLBACK (handle_source_setup), data);
    g_signal_connect (data->pipeline, "video-changed", G_CALLBACK (handle_video_changed), data);
    g_object_set (G_OBJECT (data->pipeline), "buffer-size", 0, NULL);

    /* Set the pipeline to READY, so it can already accept a window handle, if we have one */
    data->target_state = GST_STATE_READY;
    gst_element_set_state(data->pipeline, GST_STATE_READY);

    data->video_sink = gst_bin_get_by_interface(GST_BIN(data->pipeline), GST_TYPE_VIDEO_OVERLAY);
    if (!data->video_sink) {
        GST_ERROR ("Could not retrieve video sink");
        return NULL;
    }

    /* Instruct the bus to emit signals for each received message, and connect to the interesting signals */
    bus = gst_element_get_bus (data->pipeline);
    bus_source = gst_bus_create_watch (bus);
    g_source_set_callback (bus_source, (GSourceFunc) gst_bus_async_signal_func, NULL, NULL);
    g_source_attach (bus_source, data->context);
    g_source_unref (bus_source);
    g_signal_connect (G_OBJECT (bus), "message::error", (GCallback)error_cb, data);
    g_signal_connect (G_OBJECT (bus), "message::state-changed", (GCallback)state_changed_cb, data);
    gst_object_unref (bus);

    /* Create a GLib Main Loop and set it to run */
    GST_DEBUG ("Entering main loop... (CustomData:%p)", data);
    data->main_loop = g_main_loop_new (data->context, FALSE);
    check_initialization_complete (data);
    g_main_loop_run (data->main_loop);
    GST_DEBUG ("Exited main loop");
    g_main_loop_unref (data->main_loop);
    data->main_loop = NULL;

    /* Free resources */
    g_main_context_pop_thread_default(data->context);
    g_main_context_unref (data->context);
    data->target_state = GST_STATE_NULL;
    gst_element_set_state (data->pipeline, GST_STATE_NULL);
    gst_object_unref (data->video_sink);
    gst_object_unref (data->pipeline);

    return NULL;
}

/*
 * Java Bindings
 */

/* Instruct the native code to create its internal data structure, pipeline and thread */
static void gst_native_init (JNIEnv* env, jobject thiz) {
    //gst_init(NULL, NULL);
    CustomData *data = g_new0 (CustomData, 1);
    SET_CUSTOM_DATA (env, thiz, custom_data_field_id, data);
    GST_DEBUG_CATEGORY_INIT (debug_category, "evercam", 0, "Android evercam");
    gst_debug_set_threshold_for_name("evercam", GST_LEVEL_LOG);
    GST_DEBUG ("Created CustomData at %p", data);
    data->app = (*env)->NewGlobalRef (env, thiz);
    GST_DEBUG ("Created GlobalRef for app object at %p", data->app);
    pthread_create (&gst_app_thread, NULL, &app_function, data);
}

/* Quit the main loop, remove the native thread and free resources */
static void gst_native_finalize (JNIEnv* env, jobject thiz) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    GST_DEBUG ("Quitting main loop...");
    g_main_loop_quit (data->main_loop);
    GST_DEBUG ("Waiting for thread to finish...");
    pthread_join (gst_app_thread, NULL);
    GST_DEBUG ("Deleting GlobalRef for app object at %p", data->app);
    (*env)->DeleteGlobalRef (env, data->app);
    GST_DEBUG ("Freeing CustomData at %p", data);
    g_free (data);
    SET_CUSTOM_DATA (env, thiz, custom_data_field_id, NULL);
    GST_DEBUG ("Done finalizing");
}

/* Set pipeline to PLAYING state */
static void gst_native_play (JNIEnv* env, jobject thiz) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    GST_DEBUG ("Setting state to PLAYING");
    data->target_state = GST_STATE_PLAYING;
    gst_element_set_state (data->pipeline, GST_STATE_PLAYING);

}

/* Set pipeline to PAUSED state */
static void gst_native_pause (JNIEnv* env, jobject thiz) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    GST_DEBUG ("Setting state to PAUSED");
    data->target_state = GST_STATE_PAUSED;
    gst_element_set_state (data->pipeline, GST_STATE_PAUSED);
}

/* Set playbin's URI */
void gst_native_set_uri (JNIEnv* env, jobject thiz, jstring uri, jint timeout) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data || !data->pipeline) return;
    const jbyte *char_uri = (*env)->GetStringUTFChars (env, uri, NULL);
    data->tcp_timeout = timeout;
    GST_DEBUG("Set tcp timeout to %d", data->tcp_timeout);
    g_object_set(data->pipeline, "uri", char_uri, NULL);
    data->target_state = GST_STATE_READY;
    gst_element_set_state (data->pipeline, GST_STATE_READY);
    (*env)->ReleaseStringUTFChars (env, uri, char_uri);
}

/* Set playbin's URI */
void gst_native_request_sample (JNIEnv* env, jobject thiz, jstring format) {

    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data || !data->pipeline) return;

    /* If conversion in process, do nothing */
    /*if (data->busy_in_conversion == TRUE) {
        GST_DEBUG("Currently busy with previous sample conversion, plase try later");
        notify_about_get_sample_failed(data);
        return;
    }*/

    const jbyte *fmt = (*env)->GetStringUTFChars (env, format, NULL);

    if (strcmp(fmt, "png") != 0 && strcmp(fmt, "jpeg") != 0 && strcmp(fmt, "jpg") != 0) {
        GST_DEBUG("Unsupported image format %s", fmt);
        (*env)->ReleaseStringUTFChars (env, format, fmt);
        return;
    }

    GstSample *sample;
    g_object_get(data->pipeline, "sample", &sample, NULL);

    if (sample != NULL) {
        ConvertSampleContext *ctx = g_malloc( sizeof(ConvertSampleContext) );
        memset(ctx, 0, sizeof(ConvertSampleContext));
        gchar *img_fmt = g_strdup_printf("image/%s", fmt);
        GST_DEBUG("img fmt == %s", img_fmt);
        ctx->caps = gst_caps_new_simple (img_fmt, NULL);
        g_free(img_fmt);
        ctx->sample = sample;
        ctx->data = data;
        convert_sample(ctx);
    } else
        notify_about_get_sample_failed(data);

    (*env)->ReleaseStringUTFChars (env, format, fmt);
}

/* Static class initializer: retrieve method and field IDs */
static jboolean gst_native_class_init (JNIEnv* env, jclass klass) {
    custom_data_field_id = (*env)->GetFieldID (env, klass, "native_custom_data", "J");
    on_stream_loaded_method_id = (*env)->GetMethodID (env, klass, "onVideoLoaded", "()V");
    on_stream_load_failed_method_id = (*env)->GetMethodID (env, klass, "onVideoLoadFailed", "()V");
    on_request_sample_failed_method_id = (*env)->GetMethodID (env, klass, "onSampleRequestFailed", "()V");
    on_request_sample_seccess_method_id = (*env)->GetMethodID (env, klass, "onSampleRequestSuccess", "([BI)V");


    if (!custom_data_field_id || !on_stream_loaded_method_id || !on_stream_load_failed_method_id || !on_request_sample_failed_method_id || !on_request_sample_seccess_method_id) {
        /* We emit this message through the Android log instead of the GStreamer log because the later
         * has not been initialized yet.
         */
        __android_log_print (ANDROID_LOG_ERROR, "evercam", "The calling class does not implement all necessary interface methods");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

static void gst_native_surface_init (JNIEnv *env, jobject thiz, jobject surface) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    ANativeWindow *new_native_window = ANativeWindow_fromSurface(env, surface);
    GST_DEBUG ("Received surface %p (native window %p)", surface, new_native_window);

    if (data->native_window) {
        ANativeWindow_release (data->native_window);
        if (data->native_window == new_native_window) {
            GST_DEBUG ("New native window is the same as the previous one %p", data->native_window);
            if (data->video_sink) {
                gst_video_overlay_expose(GST_VIDEO_OVERLAY (data->video_sink));
                gst_video_overlay_expose(GST_VIDEO_OVERLAY (data->video_sink));
            }
            return;
        } else {
            GST_DEBUG ("Released previous native window %p", data->native_window);
            data->initialized = FALSE;
        }
    }
    data->native_window = new_native_window;

    check_initialization_complete (data);
}

static void gst_native_surface_finalize (JNIEnv *env, jobject thiz) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    GST_DEBUG ("Releasing Native Window %p", data->native_window);

    if (data->video_sink) {
        gst_video_overlay_set_window_handle (GST_VIDEO_OVERLAY (data->video_sink), (guintptr)NULL);
        data->target_state = GST_STATE_READY;
        gst_element_set_state (data->pipeline, GST_STATE_READY);
    }

    ANativeWindow_release (data->native_window);
    data->native_window = NULL;
    data->initialized = FALSE;
}

/* List of implemented native methods */
static JNINativeMethod native_methods[] = {
    { "nativeInit", "()V", (void *) gst_native_init},
    { "nativeFinalize", "()V", (void *) gst_native_finalize},
    { "nativePlay", "()V", (void *) gst_native_play},
    { "nativePause", "()V", (void *) gst_native_pause},
    { "nativeRequestSample", "(Ljava/lang/String;)V", (void *) gst_native_request_sample},
    { "nativeSetUri", "(Ljava/lang/String;I)V", (void *) gst_native_set_uri},
    { "nativeSurfaceInit", "(Ljava/lang/Object;)V", (void *) gst_native_surface_init},
    { "nativeSurfaceFinalize", "()V", (void *) gst_native_surface_finalize},
    { "nativeClassInit", "()Z", (void *) gst_native_class_init}

};

/* Library initializer */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;

    java_vm = vm;

    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        __android_log_print (ANDROID_LOG_ERROR, "evercam", "Could not retrieve JNIEnv");
        return 0;
    }
    jclass klass = (*env)->FindClass (env, "io/evercam/androidapp/video/VideoActivity");
    (*env)->RegisterNatives (env, klass, native_methods, G_N_ELEMENTS(native_methods));

    pthread_key_create (&current_jni_env, detach_current_thread);

    return JNI_VERSION_1_4;
}
