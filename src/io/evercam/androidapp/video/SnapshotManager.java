package io.evercam.androidapp.video;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

public class SnapshotManager 
{
	public static final String SNAPSHOT_FOLDER_NAME_EVERCAM = "Evercam";
	public static final String SNAPSHOT_FOLDER_NAME_PLAY = "Evercam Play";
	public enum FileType {PNG,JPG};
	
	/**
	 * Produce a path for the snapshot to be saved in format:
	 * Evercam folder/camera id + current time + file type ending 
	 * For example Pictures/Evercam/Evercam Play/cameraid_20141225_091011.jpg
	 * 
	 * @param cameraId the camera id from Evercam
	 * @param fileType PNG or JPG depend on it's from video or JPG view
	 * @return snapshot file path
	 */
	public static String createFilePath(String cameraId, FileType fileType)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeString = dateFormat.format(Calendar.getInstance().getTime());
		String fileName = cameraId + "_" + timeString + fileType(fileType);
		
		File folder = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
						+ File.separator + SNAPSHOT_FOLDER_NAME_EVERCAM + File.separator + SNAPSHOT_FOLDER_NAME_PLAY);
		if (!folder.exists())
		{
			folder.mkdirs();
		}
		
		return folder.getPath() + File.separator + fileName;
	}
	
	/**
	 * Notify Gallery about the snapshot that got saved, otherwise the image
	 * won't show in Gallery
	 * 
	 * @param full snapshot path
	 */
	public static void updateGallery(String path, Activity activity)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
		{
			activity
					.sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_MOUNTED,
							Uri.parse("file://"
									+ Environment
											.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))));
		}
		else
		{
			new SingleMediaScanner(activity,path);
		}
	}
	
	private static String fileType(FileType fileType)
	{
		if(fileType.equals(FileType.PNG))
		{
			return ".png";
		}
		else
		{
			return ".jpg";
		}
	}

	static class SingleMediaScanner implements MediaScannerConnectionClient
	{
		MediaScannerConnection connection;
		Context ctxt;
		private String imagepath;

		public SingleMediaScanner(Context ctxt, String url)
		{
			this.ctxt = ctxt;
			startScan(url);
		}

		public void startScan(String url)
		{
			imagepath = url;
			if (connection != null) connection.disconnect();
			connection = new MediaScannerConnection(ctxt, this);
			connection.connect();
		}

		@Override
		public void onMediaScannerConnected()
		{
			try
			{
				connection.scanFile(imagepath, null);
			}
			catch (java.lang.IllegalStateException e)
			{
			}
		}

		@Override
		public void onScanCompleted(String path, Uri uri)
		{
			connection.disconnect();
		}
	}
}
