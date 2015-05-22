package io.evercam.androidapp;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import io.evercam.Auth;
import io.evercam.CameraBuilder;
import io.evercam.Defaults;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.PatchCameraBuilder;
import io.evercam.Vendor;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.AddCameraTask;
import io.evercam.androidapp.tasks.PatchCameraTask;
import io.evercam.androidapp.tasks.TestSnapshotTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;
import io.evercam.network.discovery.DiscoveredCamera;

public class AddEditCameraActivity extends ParentActivity
{
    private final String TAG = "AddEditCameraActivity";
    private EditText cameraIdEdit;
    private TextView cameraIdTextView;
    private EditText cameraNameEdit;
    private Spinner vendorSpinner;
    private Spinner modelSpinner;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private EditText externalHostEdit;
    private EditText externalHttpEdit;
    private EditText externalRtspEdit;
    private EditText jpgUrlEdit;
    private Button addEditButton;
    private TreeMap<String, String> vendorMap;
    private TreeMap<String, String> vendorMapIdAsKey;
    private TreeMap<String, String> modelMap;

    private DiscoveredCamera discoveredCamera;
    private EvercamCamera cameraEdit;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        // Edit Camera
        if(bundle != null && bundle.containsKey(Constants.KEY_IS_EDIT))
        {
            EvercamPlayApplication.sendScreenAnalytics(this,
                    getString(R.string.screen_edit_camera));
            cameraEdit = VideoActivity.evercamCamera;

            getActionBar().setTitle(R.string.title_edit_camera);
        }
        else
        // Add Camera
        {
            EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_add_camera));

            // Get camera object from video activity before initial screen
            discoveredCamera = (DiscoveredCamera) getIntent().getSerializableExtra("camera");
        }

        setContentView(R.layout.activity_add_camera);

        if(this.getActionBar() != null)
        {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initial UI elements
        initialScreen();

        fillDiscoveredCameraDetails(discoveredCamera);

        fillEditCameraDetails(cameraEdit);
    }

    @Override
    public void onBackPressed()
    {
        showConfirmQuitIfAddingCamera();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                showConfirmQuitIfAddingCamera();
                return true;
        }
        return true;
    }

    private void showConfirmQuitIfAddingCamera()
    {
        //If edit camera
        if(addEditButton.getText().equals(getString(R.string.save_changes)))
        {
            setResult(Constants.RESULT_FALSE);
            super.onBackPressed();
        }
        //If add camera
        else
        {
            String cameraId = cameraIdEdit.getText().toString();
            String cameraName = cameraNameEdit.getText().toString();
            String username = usernameEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            String externalHost = externalHostEdit.getText().toString();
            String externalHttp = externalHttpEdit.getText().toString();
            String externalRtsp = externalRtspEdit.getText().toString();
            String jpgUrl = jpgUrlEdit.getText().toString();

            if(!(cameraId.isEmpty() && cameraName.isEmpty() && username.isEmpty() && password
                    .isEmpty() && externalHost.isEmpty() && externalHttp.isEmpty() &&
                    externalRtsp.isEmpty() && jpgUrl.isEmpty()))
            {
                CustomedDialog.getConfirmCancelAddCameraDialog(this).show();
            }
            else
            {
                setResult(Constants.RESULT_FALSE);
                super.onBackPressed();
            }
        }
    }

    private void initialScreen()
    {
        cameraIdEdit = (EditText) findViewById(R.id.add_id_edit);
        cameraIdTextView = (TextView) findViewById(R.id.add_id_txt_view);
        cameraNameEdit = (EditText) findViewById(R.id.add_name_edit);
        vendorSpinner = (Spinner) findViewById(R.id.vendor_spinner);
        modelSpinner = (Spinner) findViewById(R.id.model_spinner);
        ImageView modelExplanationImageButton = (ImageView) findViewById(R.id.model_explanation_btn);
        usernameEdit = (EditText) findViewById(R.id.add_username_edit);
        passwordEdit = (EditText) findViewById(R.id.add_password_edit);
        externalHostEdit = (EditText) findViewById(R.id.add_external_host_edit);
        externalHttpEdit = (EditText) findViewById(R.id.add_external_http_edit);
        externalRtspEdit = (EditText) findViewById(R.id.add_external_rtsp_edit);
        jpgUrlEdit = (EditText) findViewById(R.id.add_jpg_edit);
        addEditButton = (Button) findViewById(R.id.button_add_edit_camera);
        Button testButton = (Button) findViewById(R.id.button_test_snapshot);

        if(cameraEdit != null)
        {
            addEditButton.setText(getString(R.string.save_changes));
            cameraIdEdit.setVisibility(View.GONE);
            cameraIdTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            addEditButton.setText(getString(R.string.finish_and_add));
        }
        buildVendorSpinner(null, null);
        buildModelSpinner(null, null);

        new RequestVendorListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        vendorSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int
                    position, long id)
            {
                if(position == 0)
                {
                    buildModelSpinner(new ArrayList<Model>(), null);
                }
                else
                {
                    String vendorName = vendorSpinner.getSelectedItem().toString();
                    String vendorId = vendorMap.get(vendorName).toLowerCase(Locale.UK);

                    if(!vendorName.equals(getString(R.string.vendor_other)))
                    {
                        new RequestModelListTask(vendorId).executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR);
                    }
                    else
                    {
                        modelSpinner.setEnabled(false);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        modelSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id)
            {
                // Do not update camera defaults in edit screen.
                if(cameraEdit == null)
                {
                    if(position == 0)
                    {
                        clearDefaults();
                    }
                    else
                    {
                        String vendorId = getVendorIdFromSpinner();
                        String modelName = getModelNameFromSpinner();

                        new RequestDefaultsTask(vendorId, modelName).executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        modelExplanationImageButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CustomedDialog.getMessageDialog(AddEditCameraActivity.this, R.string
                        .msg_model_explanation).show();
            }
        });

        jpgUrlEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(!jpgUrlEdit.isFocusable())
                {
                    CustomedDialog.getMessageDialog(AddEditCameraActivity.this, R.string.msg_url_ending_not_editable).show();
                }
            }
        });

        addEditButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                String externalHost = externalHostEdit.getText().toString();
                if(Commons.isLocalIp(externalHost))
                {
                    CustomedDialog.getStandardAlertDialog(AddEditCameraActivity.this, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            performAddEdit();
                        }
                    }, R.string.msg_local_ip_warning).show();
                }
                else
                {
                    performAddEdit();
                }
            }
        });

        testButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String externalHost = externalHostEdit.getText().toString();
                if(Commons.isLocalIp(externalHost))
                {
                    CustomedDialog.getStandardAlertDialog(AddEditCameraActivity.this, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            launchTestSnapshot();
                        }
                    }, R.string.msg_local_ip_warning).show();
                }
                else
                {
                    launchTestSnapshot();
                }
            }
        });
    }

    private void performAddEdit()
    {
        if(addEditButton.getText().equals(getString(R.string.save_changes)))
        {
            PatchCameraBuilder patchCameraBuilder = buildPatchCameraWithLocalCheck();
            if(patchCameraBuilder != null)
            {
                new PatchCameraTask(patchCameraBuilder.build(),
                        AddEditCameraActivity.this).executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR);
            }
            else
            {
                Log.e(TAG, "Camera to patch is null");
            }
        }
        else
        {
            CameraBuilder cameraBuilder = buildCameraWithLocalCheck();
            if(cameraBuilder != null)
            {
                boolean isFromScan = discoveredCamera != null;
                new AddCameraTask(cameraBuilder.build(), AddEditCameraActivity.this,
                        isFromScan).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else
            {
                Log.e(TAG, "Camera to add is null");
            }
        }
    }

    private void fillDiscoveredCameraDetails(DiscoveredCamera camera)
    {
        if(camera != null)
        {
            // Log.d(TAG, camera.toString());
            if(camera.hasExternalIp())
            {
                externalHostEdit.setText(camera.getExternalIp());
            }
            if(camera.hasExternalHttp())
            {
                externalHttpEdit.setText(String.valueOf(camera.getExthttp()));
            }
            if(camera.hasExternalRtsp())
            {
                externalRtspEdit.setText(String.valueOf(camera.getExtrtsp()));
            }
        }
    }

    private void fillEditCameraDetails(EvercamCamera camera)
    {
        if(camera != null)
        {
            // Log.d(TAG, cameraEdit.toString());
            cameraIdTextView.setText(camera.getCameraId());
            cameraNameEdit.setText(camera.getName());
            usernameEdit.setText(camera.getUsername());
            passwordEdit.setText(camera.getPassword());
            jpgUrlEdit.setText(camera.getJpgPath());
            externalHostEdit.setText(camera.getExternalHost());
            int externalHttp = camera.getExternalHttp();
            int externalRtsp = camera.getExternalRtsp();
            if(externalHttp != 0)
            {
                externalHttpEdit.setText(String.valueOf(externalHttp));
            }
            if(externalRtsp != 0)
            {
                externalRtspEdit.setText(String.valueOf(externalRtsp));
            }
        }
    }

    /**
     * Read and validate user input for add camera.
     */
    private CameraBuilder buildCameraWithLocalCheck()
    {
        CameraBuilder cameraBuilder = null;
        String cameraId = cameraIdEdit.getText().toString();

        String cameraName = cameraNameEdit.getText().toString();

        if(cameraId.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.id_required));
            return null;
        }
        if(cameraName.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.name_required));
            return null;
        }
        try
        {
            cameraBuilder = new CameraBuilder(cameraId, cameraName, false);
        }
        catch(EvercamException e)
        {
            Log.e(TAG, e.toString());
        }

        String vendorId = getVendorIdFromSpinner();
        if(!vendorId.isEmpty())
        {
            cameraBuilder.setVendor(vendorId);
        }

        String modelId = getModelIdFromSpinner();
        if(!modelId.isEmpty())
        {
            cameraBuilder.setModel(modelId);
        }

        String username = usernameEdit.getText().toString();
        if(!username.isEmpty())
        {
            cameraBuilder.setCameraUsername(username);
        }

        String password = passwordEdit.getText().toString();
        if(!password.isEmpty())
        {
            cameraBuilder.setCameraPassword(password);
        }

        String externalHost = externalHostEdit.getText().toString();
        if(externalHost.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.host_required));
            return null;
        }
        else
        {
            cameraBuilder.setExternalHost(externalHost);

            String externalHttp = externalHttpEdit.getText().toString();
            if(!externalHttp.isEmpty())
            {
                int externalHttpInt = getPortIntByString(externalHttp);
                if(externalHttpInt != 0)
                {
                    cameraBuilder.setExternalHttpPort(externalHttpInt);
                }
                else
                {
                    return null;
                }
            }

            String externalRtsp = externalRtspEdit.getText().toString();
            if(!externalRtsp.isEmpty())
            {
                int externalRtspInt = getPortIntByString(externalRtsp);
                if(externalRtspInt != 0)
                {
                    cameraBuilder.setExternalRtspPort(externalRtspInt);
                }
                else
                {
                    return null;
                }
            }
        }

        String jpgUrl = buildJpgUrlWithSlash(jpgUrlEdit.getText().toString());
        if(!jpgUrl.isEmpty())
        {
            cameraBuilder.setJpgUrl(jpgUrl);
        }

        return cameraBuilder;
    }

    /**
     * Convert port string to port int, show error toast if port number is not valid,
     *
     * @return int port number, if port is not valid, return 0.
     */
    private int getPortIntByString(String portString)
    {
        try
        {
            int portInt = Integer.valueOf(portString);
            if(portInt > 0)
            {
                if(portInt <= 65535)
                {
                    return portInt;
                }
                else
                {
                    CustomToast.showInCenter(this, getString(R.string.msg_port_range_error));
                    return 0;
                }
            }
            else
            {
                CustomToast.showInCenter(this, getString(R.string.msg_port_range_error));
                return 0;
            }
        }
        catch(NumberFormatException e)
        {
            CustomToast.showInCenter(this, getString(R.string.msg_port_range_error));
            return 0;
        }
    }

    /**
     * Read and validate user input for edit camera.
     */
    private PatchCameraBuilder buildPatchCameraWithLocalCheck()
    {
        PatchCameraBuilder patchCameraBuilder = null;

        try
        {
            patchCameraBuilder = new PatchCameraBuilder(cameraEdit.getCameraId());
        }
        catch(EvercamException e)
        {
            Log.e(TAG, e.toString());
        }

        String cameraName = cameraNameEdit.getText().toString();
        if(cameraName.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.name_required));
            return null;
        }
        else if(!cameraName.equals(cameraEdit.getName()))
        {
            patchCameraBuilder.setName(cameraName);
        }

        String vendorId = getVendorIdFromSpinner();
        patchCameraBuilder.setVendor(vendorId);

        String modelName = getModelIdFromSpinner();
        patchCameraBuilder.setModel(modelName);

        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        if(!username.equals(cameraEdit.getUsername()) || !password.equals(cameraEdit.getPassword()))
        {
            patchCameraBuilder.setCameraUsername(username);
            patchCameraBuilder.setCameraPassword(password);
        }

        String externalHost = externalHostEdit.getText().toString();
        if(externalHost.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.host_required));
            return null;
        }
        else
        {
            patchCameraBuilder.setExternalHost(externalHost);

            String externalHttp = externalHttpEdit.getText().toString();
            if(!externalHttp.isEmpty())
            {
                int externalHttpInt = getPortIntByString(externalHttp);
                if(externalHttpInt != 0)
                {
                    patchCameraBuilder.setExternalHttpPort(externalHttpInt);
                }
                else
                {
                    return null;
                }
            }

            String externalRtsp = externalRtspEdit.getText().toString();
            if(!externalRtsp.isEmpty())
            {
                int externalRtspInt = getPortIntByString(externalRtsp);
                if(externalRtspInt != 0)
                {
                    patchCameraBuilder.setExternalRtspPort(externalRtspInt);
                }
                else
                {
                    return null;
                }
            }
        }

        String jpgUrl = buildJpgUrlWithSlash(jpgUrlEdit.getText().toString());
        if(jpgUrl.equals(cameraEdit.getJpgPath()))
        {
            patchCameraBuilder.setJpgUrl(jpgUrl);
        }

        return patchCameraBuilder;
    }

    private void buildVendorSpinner(ArrayList<Vendor> vendorList, String selectedVendor)
    {
        if(vendorMap == null)
        {
            vendorMap = new TreeMap<>();
        }

        if(vendorMapIdAsKey == null)
        {
            vendorMapIdAsKey = new TreeMap<>();
        }

        if(vendorList != null)
        {
            for(Vendor vendor : vendorList)
            {
                try
                {
                    vendorMap.put(vendor.getName(), vendor.getId());
                    vendorMapIdAsKey.put(vendor.getId(), vendor.getName());
                }
                catch(EvercamException e)
                {
                    Log.e(TAG, e.toString());
                }
            }
        }

        Set<String> set = vendorMap.keySet();
        String[] vendorArray = Commons.joinStringArray(new String[]{getResources().getString(R
                .string.select_vendor)}, set.toArray(new String[0]));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, vendorArray);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner);

        int selectedPosition = 0;
        if(discoveredCamera != null)
        {
            if(discoveredCamera.hasVendor())
            {
                String vendorId = discoveredCamera.getVendor();
                String vendorName = vendorMapIdAsKey.get(vendorId);
                selectedPosition = spinnerArrayAdapter.getPosition(vendorName);
            }
        }
        if(selectedVendor != null)
        {
            selectedPosition = spinnerArrayAdapter.getPosition(selectedVendor);
        }
        vendorSpinner.setAdapter(spinnerArrayAdapter);

        if(selectedPosition != 0)
        {
            vendorSpinner.setSelection(selectedPosition);
        }
    }

    private void buildModelSpinner(ArrayList<Model> modelList, String selectedModel)
    {
        if(selectedModel != null && !selectedModel.isEmpty())
        {
            selectedModel = selectedModel.toLowerCase(Locale.UK);
        }
        if(modelMap == null)
        {
            modelMap = new TreeMap<>();
        }
        modelMap.clear();

        if(modelList == null)
        {
            modelSpinner.setEnabled(false);
        }
        else
        {
            if(modelList.size() == 0)
            {
                modelSpinner.setEnabled(false);
            }
            else
            {
                modelSpinner.setEnabled(true);

                for(Model model : modelList)
                {
                    try
                    {
                        modelMap.put(model.getName(), model.getId());
                    }
                    catch(EvercamException e)
                    {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }
        Set<String> set = modelMap.keySet();
        String[] fullModelArray = Commons.joinStringArray(new String[]{getResources().getString(R
                .string.select_model)}, set.toArray(new String[0]));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, fullModelArray);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner);
        modelSpinner.setAdapter(spinnerArrayAdapter);

        int selectedPosition = 0;
        if(selectedModel != null)
        {
            if(modelMap.get(selectedModel) != null)
            {
                selectedPosition = spinnerArrayAdapter.getPosition(selectedModel);
            }
        }
        if(selectedPosition != 0)
        {
            modelSpinner.setSelection(selectedPosition);
        }
        else
        {
            modelSpinner.setSelection(spinnerArrayAdapter.getPosition(getString(R.string
                    .model_default)));
        }
    }

    private void fillDefaults(Model model)
    {
        try
        {
            // FIXME: Sometimes vendor with no default model, contains default
            // jpg url.
            // TODO: Consider if no default values associated, clear defaults
            // that has been filled.
            Defaults defaults = model.getDefaults();
            Auth basicAuth = defaults.getAuth(Auth.TYPE_BASIC);
            if(basicAuth != null)
            {
                usernameEdit.setText(basicAuth.getUsername());
                passwordEdit.setText(basicAuth.getPassword());
            }
            jpgUrlEdit.setText(defaults.getJpgURL());

            //If user specified a specific model, make it not editable
            if(!model.getName().equals(Model.DEFAULT_MODEL_NAME))
            {
                jpgUrlEdit.setFocusable(false);
                jpgUrlEdit.setClickable(true);
            }
            else
            {
                jpgUrlEdit.setFocusable(true);
                jpgUrlEdit.setClickable(true);
            }
        }
        catch(EvercamException e)
        {
            Log.e(TAG, "Fill defaults: " + e.toString());
        }
    }

    private void clearDefaults()
    {
        usernameEdit.setText("");
        passwordEdit.setText("");
        jpgUrlEdit.setText("");
    }

    private String getVendorIdFromSpinner()
    {
        String vendorName = vendorSpinner.getSelectedItem().toString();
        if(vendorName.equals(getString(R.string.select_vendor)))
        {
            return "";
        }
        else
        {
            return vendorMap.get(vendorName).toLowerCase(Locale.UK);
        }

    }

    private String getModelIdFromSpinner()
    {
        String modelName = modelSpinner.getSelectedItem().toString();
        if(modelName.equals(getString(R.string.select_model)))
        {
            return "";
        }
        else
        {
            return modelMap.get(modelName).toLowerCase(Locale.UK);
        }
    }

    private String getModelNameFromSpinner()
    {
        String modelName = modelSpinner.getSelectedItem().toString();
        if(modelName.equals(getString(R.string.select_model)))
        {
            return "";
        }
        else
        {
            return modelName;
        }
    }

    public static String buildJpgUrlWithSlash(String originalJpgUrl)
    {
        String jpgUrl = "";
        if(originalJpgUrl != null && !originalJpgUrl.equals(""))
        {
            if(!originalJpgUrl.startsWith("/"))
            {
                jpgUrl = "/" + originalJpgUrl;
            }
            else
            {
                jpgUrl = originalJpgUrl;
            }
        }
        return jpgUrl;
    }

    private void launchTestSnapshot()
    {
        String externalHost = externalHostEdit.getText().toString();

        if(externalHost.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.host_required));
        }
        else
        {
            final String username = usernameEdit.getText().toString();
            final String password = passwordEdit.getText().toString();
            String jpgUrlString = jpgUrlEdit.getText().toString();
            final String jpgUrl = buildJpgUrlWithSlash(jpgUrlString);

            String externalUrl = getExternalUrl();
            if(externalUrl != null)
            {
                new TestSnapshotTask(externalUrl, jpgUrl, username, password,
                        AddEditCameraActivity.this).executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR);
            }
        }
    }

    /**
     * Check external HTTP port is filled or not and return external URL with
     * snapshot ending.
     */
    private String getExternalUrl()
    {
        String externalHost = externalHostEdit.getText().toString();
        String externalHttp = externalHttpEdit.getText().toString();
        if(externalHttp.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.external_http_required));
            return null;
        }
        else
        {
            int externalHttpInt = getPortIntByString(externalHttp);
            if(externalHttpInt != 0)
            {
                return getString(R.string.prefix_http) + externalHost + ":" + externalHttp;
            }
            else
            {
                return null;
            }
        }
    }

    class RequestVendorListTask extends AsyncTask<Void, Void, ArrayList<Vendor>>
    {

        @Override
        protected void onPostExecute(ArrayList<Vendor> vendorList)
        {
            if(vendorList != null)
            {
                // If the camera has vendor, show as selected in spinner
                if(cameraEdit != null && !cameraEdit.getVendor().isEmpty())
                {
                    buildVendorSpinner(vendorList, cameraEdit.getVendor());
                }
                else
                {
                    buildVendorSpinner(vendorList, null);
                }
            }
            else
            {
                Log.e(TAG, "Vendor list is null");
            }
        }

        @Override
        protected ArrayList<Vendor> doInBackground(Void... params)
        {
            try
            {
                return Vendor.getAll();
            }
            catch(EvercamException e)
            {
                Log.e(TAG, e.toString());
            }
            return null;
        }
    }

    class RequestModelListTask extends AsyncTask<Void, Void, ArrayList<Model>>
    {
        private String vendorId;

        public RequestModelListTask(String vendorId)
        {
            this.vendorId = vendorId;
        }

        @Override
        protected ArrayList<Model> doInBackground(Void... params)
        {
            try
            {
                return Model.getAllByVendorId(vendorId);
            }
            catch(EvercamException e)
            {
                EvercamPlayApplication.sendCaughtException(AddEditCameraActivity.this,
                        e.toString() + " " + "with vendor id: " + vendorId);
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Model> modelList)
        {
            if(modelList != null)
            {
                if(cameraEdit != null && !cameraEdit.getModel().isEmpty())
                {
                    buildModelSpinner(modelList, cameraEdit.getModel());
                }
                else if(discoveredCamera != null && !discoveredCamera.getModel().isEmpty())
                {
                    buildModelSpinner(modelList, discoveredCamera.getModel());
                }
                else
                {
                    buildModelSpinner(modelList, null);
                }
            }
        }
    }

    class RequestDefaultsTask extends AsyncTask<Void, Void, Model>
    {
        private String vendorId;
        private String modelName;

        public RequestDefaultsTask(String vendorId, String modelName)
        {
            this.vendorId = vendorId;
            this.modelName = modelName;
        }

        @Override
        protected void onPreExecute()
        {
            clearDefaults();
        }

        @Override
        protected Model doInBackground(Void... params)
        {
            try
            {
                ArrayList<Model> modelList = Model.getAll(modelName, vendorId);
                if(modelList.size() > 0)
                {
                    return modelList.get(0);
                }
            }
            catch(EvercamException e)
            {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Model model)
        {
            if(model != null)
            {
                fillDefaults(model);
            }
        }
    }
}
