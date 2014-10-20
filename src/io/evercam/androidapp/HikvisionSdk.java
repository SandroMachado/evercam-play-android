package io.evercam.androidapp;

import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.dto.EvercamCamera;
import org.MediaPlayer.PlayM4.Player;

import android.util.Log;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PLAYBACK_INFO;
import com.hikvision.netsdk.NET_DVR_TIME;
import com.hikvision.netsdk.PlaybackCallBack;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class HikvisionSdk implements Callback
{
	private final String TAG = "evercamplay-HikvisionSdk";

	private EvercamCamera evercamCamera;
	private LocalStorageActivity activity;

	private SurfaceView surfaceView;
	private Player hikvisionPlayer = null;
	private NET_DVR_DEVICEINFO_V30 netDvrDeviceInfoV30 = null;
	private int loginId = -1;
	private int playBackId = -1;
	public int playPort = -1;

	public HikvisionSdk(SurfaceView surfaceView, EvercamCamera evercamCamera,
			LocalStorageActivity activity)
	{
		this.evercamCamera = evercamCamera;
		this.surfaceView = surfaceView;
		this.activity = activity;
		initSdk();
		surfaceView.getHolder().addCallback(this);
	}

	private boolean initSdk()
	{
		if (!HCNetSDK.getInstance().NET_DVR_Init())
		{
			Log.e(TAG, "HCNetSDK init is failed!");
			return false;
		}

		hikvisionPlayer = Player.getInstance();
		if (hikvisionPlayer == null)
		{
			Log.e(TAG, "PlayCtrl getInstance failed!");
			return false;
		}
		return true;
	}

	public boolean login()
	{
		fillCameraInfo();
		try
		{
			if (loginId < 0)
			{
				// login on the device
				loginId = loginDevice();
				if (loginId < 0)
				{
					Log.e(TAG, "This device logins failed!");
					return false;
				}
				else
				{
					Log.i(TAG,
							"Login sucess ****************************1***************************");
					return true;
				}
			}
			else
			{
				// whether we have logout
				if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(loginId))
				{
					Log.e(TAG, " NET_DVR_Logout is failed!");
					return false;
				}
				loginId = -1;
			}
		}
		catch (Exception err)
		{
			Log.e(TAG, "error: " + err.toString());
		}
		return false;
	}

	private int loginDevice()
	{
		// get instance
		netDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
		if (null == netDvrDeviceInfoV30)
		{
			Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
			return -1;
		}

		int loginId = HCNetSDK.getInstance().NET_DVR_Login_V30(CameraInfo.ip, CameraInfo.sdkPort,
				CameraInfo.username, CameraInfo.password, netDvrDeviceInfoV30);
		Log.d(TAG, "Below is the device information************************");
		Log.d(TAG, "userId=" + loginId);
		Log.d(TAG, "The channel began=" + netDvrDeviceInfoV30.byStartChan);
		Log.d(TAG, "The number of channels=" + netDvrDeviceInfoV30.byChanNum);
		Log.d(TAG, "The device type=" + netDvrDeviceInfoV30.byDVRType);
		Log.d(TAG, "The number of IP channels=" + netDvrDeviceInfoV30.byIPChanNum);
		if (loginId < 0)
		{
			Log.e(TAG, "NET_DVR_Login is failed!Err:"
					+ HCNetSDK.getInstance().NET_DVR_GetLastError());
			return -1;
		}

		Log.i(TAG, "NET_DVR_Login is Successful!");

		return loginId;
	}

	public void startPlayback(NET_DVR_TIME beginTime)
	{
		try
		{
			if (loginId < 0)
			{
				Log.e(TAG, "please login on a device first");
				return;
			}

			if (playBackId < 0)
			{
				play(beginTime);
			}
			else
			{
				stopPlay();
				play(beginTime);
			}
		}
		catch (Exception err)
		{
			Log.e(TAG, "error: " + err.toString());
		}
	}

	private void stopPlay()
	{
		if (!HCNetSDK.getInstance().NET_DVR_StopPlayBack(playBackId))
		{
			Log.e(TAG, "net sdk stop playback failed");
		}
		// player stop play
		if (!hikvisionPlayer.stop(playPort))
		{
			Log.e(TAG, "player_stop is failed!");
		}
		if (!hikvisionPlayer.closeStream(playPort))
		{
			Log.e(TAG, "closeStream is failed!");
		}
		if (!hikvisionPlayer.freePort(playPort))
		{
			Log.e(TAG, "freePort is failed!" + playPort);
		}
		playPort = -1;
		playBackId = -1;
	}

	private void play(NET_DVR_TIME beginTime)
	{
		PlaybackCallBack playbackCallBack = getPlaybackCallback();
		if (playbackCallBack == null)
		{
			Log.e(TAG, "fPlaybackCallBack object is failed!");
			return;
		}

		NET_DVR_TIME endTime = getEndTimeBasedOnBeginTime(beginTime);

		playBackId = HCNetSDK.getInstance().NET_DVR_PlayBackByTime(loginId, 1, beginTime, endTime);
		Log.d(TAG, beginTime.ToString() + " " + endTime.ToString());

		if (playBackId >= 0)
		{
			if (!HCNetSDK.getInstance().NET_DVR_SetPlayDataCallBack(playBackId, playbackCallBack))
			{
				Log.e(TAG, "Set playback callback failed!");
				return;
			}
			NET_DVR_PLAYBACK_INFO struPlaybackInfo = null;
			if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(playBackId,
					HCNetSDK.NET_DVR_PLAYSTART, null, 0, struPlaybackInfo))
			{
				Log.e(TAG, "net sdk playback start failed!");
				return;
			}
		}
		else
		{
			activity.hideProgressView();
			CustomToast.showInCenter(activity, R.string.msg_playback_failed);
			Log.d(TAG, "PLAYBACK ERROR");
			Log.i(TAG, "NET_DVR_PlayBackByTime failed, error code: "
					+ HCNetSDK.getInstance().NET_DVR_GetLastError());
		}
	}

	private PlaybackCallBack getPlaybackCallback()
	{
		PlaybackCallBack callback = new PlaybackCallBack(){
			@Override
			public void fPlayDataCallBack(int iPlaybackHandle, int iDataType, byte[] pDataBuffer,
					int iDataSize)
			{
				processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_FILE);
			}
		};
		return callback;
	}

	public void processRealData(int iPlayViewNo, int iDataType, byte[] pDataBuffer, int iDataSize,
			int iStreamMode)
	{
		// Log.i(TAG, "iPlayViewNo:" + iPlayViewNo + ",iDataType:" + iDataType +
		// ",iDataSize:" + iDataSize);
		if (HCNetSDK.NET_DVR_SYSHEAD == iDataType)
		{
			if (playPort >= 0)
			{
				return;
			}
			playPort = hikvisionPlayer.getPort();
			if (playPort == -1)
			{
				Log.e(TAG, "getPort is failed with: " + hikvisionPlayer.getLastError(playPort));
				return;
			}
			Log.i(TAG, "getPort succ with: " + playPort);
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run()
				{
					activity.hideProgressView();
				}

			});
			if (iDataSize > 0)
			{
				if (!hikvisionPlayer.setStreamOpenMode(playPort, iStreamMode)) // set
																				// stream
																				// mode
				{
					Log.e(TAG, "setStreamOpenMode failed");
					return;
				}
				if (!hikvisionPlayer.setSecretKey(playPort, 1, "ge_security_3477".getBytes(), 128))
				{
					Log.e(TAG, "setSecretKey failed");
					return;
				}
				if (!hikvisionPlayer.openStream(playPort, pDataBuffer, iDataSize, 2 * 1024 * 1024)) // open
																									// stream
				{
					Log.e(TAG, "openStream failed");
					return;
				}
				if (!hikvisionPlayer.play(playPort, surfaceView.getHolder()))
				{
					Log.e(TAG, "play failed");
					return;
				}
			}
		}
		else
		{
			if (!hikvisionPlayer.inputData(playPort, pDataBuffer, iDataSize))
			{
				// Log.e(TAG, "inputData failed with: " +
				// hikvisionPlayer.getLastError(playPort));
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (-1 == playPort)
		{
			return;
		}

		Surface surface = holder.getSurface();
		if (null != hikvisionPlayer && true == surface.isValid())
		{
			if (false == hikvisionPlayer.setVideoWindow(playPort, 0, holder))
			{
				Log.e(TAG, "Player setVideoWindow failed!");
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		if (-1 == playPort)
		{
			return;
		}

		if (null != hikvisionPlayer && true == holder.getSurface().isValid())
		{
			if (false == hikvisionPlayer.setVideoWindow(playPort, 0, null))
			{
				Log.e(TAG, "Player setVideoWindow failed!");
			}
		}
	}

	private void fillCameraInfo()
	{
		if(evercamCamera != null)
		{
			CameraInfo.username = evercamCamera.getUsername();
			CameraInfo.password = evercamCamera.getPassword();
			if (!evercamCamera.getExternalHost().isEmpty())
			{
				CameraInfo.ip = evercamCamera.getExternalHost();
				CameraInfo.sdkPort = calculateSdkPort(evercamCamera.getExternalHttp());
			}
			else if (!evercamCamera.getInternalHost().isEmpty())
			{
				CameraInfo.ip = evercamCamera.getInternalHost();
				CameraInfo.sdkPort = calculateSdkPort(evercamCamera.getInternalHttp());
			}
		}
	}

	// Currently the SDK port is hard coded.
	private int calculateSdkPort(int httpPort)
	{
		if (httpPort > 0)
		{
			return httpPort + 2000;
		}
		return 0;
	}

	private NET_DVR_TIME getEndTimeBasedOnBeginTime(NET_DVR_TIME beginTime)
	{
		NET_DVR_TIME endTime = new NET_DVR_TIME();

		endTime.dwYear = beginTime.dwYear;
		endTime.dwMonth = beginTime.dwMonth;
		endTime.dwDay = beginTime.dwDay;
		endTime.dwHour = beginTime.dwHour + 1;
		endTime.dwMinute = beginTime.dwMinute;
		endTime.dwSecond = 0;
		return endTime;
	}

	public void cleanUp()
	{
		hikvisionPlayer.freePort(playPort);
		playPort = -1;

		// release net SDK resource
		HCNetSDK.getInstance().NET_DVR_Cleanup();
	}

	static class CameraInfo
	{
		static int sdkPort = 0;
		static String ip = "";
		static String username = "";
		static String password = "";
	}
}
