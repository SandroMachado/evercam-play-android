//package io.evercam.androidapp.utils;
//
//import io.evercam.androidapp.dto.MailInputDto;
//import io.evercam.androidapp.email.GMailSender;
//
//import com.google.android.gcm.GCMRegistrar;
//
//import android.bluetooth.BluetoothAdapter;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.net.wifi.WifiInfo;
//import android.net.wifi.WifiManager;
//import android.os.AsyncTask;
//import android.preference.PreferenceManager;
//import android.util.Log;
//
//public class CLog extends java.lang.Object
//{
//
//	public static void email(Context cont, String Message, Throwable ex)
//	{
//		try
//		{
//			MailInputDto dto = new MailInputDto(cont, Message, ex);
//			new AsyncTask<MailInputDto, String, String>(){
//
//				@Override
//				protected String doInBackground(MailInputDto... params)
//				{
//
//					try
//					{
//						Context context = params[0].cont;
//						Throwable exception = params[0].ex;
//						String exceptionMessage = params[0].Message;
//
//						String regId = GCMRegistrar.getRegistrationId(context); // registration
//																				// id
//																				// for
//																				// this
//						String AppUserEmail = null;
//						String AppUserPassword = null;
//						String Manufacturer = null;
//						String Model = null;
//						String SerialNo = null;
//						String ImeiNo = null;
//						String Fingureprint = null;
//						String MacAddress = null;
//						String BlueToothName = null;
//						String AppVersion = null;
//
//						try
//						{
//
//							SharedPreferences sharedPrefs = PreferenceManager
//									.getDefaultSharedPreferences(context);
//							AppUserEmail = sharedPrefs.getString("AppUserEmail", null);
//							AppUserPassword = sharedPrefs.getString("AppUserPassword", null);
//							Manufacturer = android.os.Build.MANUFACTURER;
//							Model = android.os.Build.MODEL;
//							SerialNo = android.os.Build.SERIAL;
//							ImeiNo = ((android.telephony.TelephonyManager) context
//									.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
//							Fingureprint = android.os.Build.FINGERPRINT;
//							WifiManager manager = (WifiManager) context
//									.getSystemService(Context.WIFI_SERVICE);
//							WifiInfo info = manager.getConnectionInfo();
//							MacAddress = info.getMacAddress();
//							BlueToothName = BluetoothAdapter.getDefaultAdapter().getName();
//							AppVersion = (context.getPackageManager().getPackageInfo(
//									context.getPackageName(), 0)).versionName;
//						}
//						catch (Exception ee)
//						{
//						}
//
//						StringBuilder Subject = new StringBuilder("Exception: User [")
//								.append(AppUserEmail).append("], Serial Number [").append(SerialNo)
//								.append("], AppVersion [").append(AppVersion).append("] Device [")
//								.append(Manufacturer).append("-").append(Model).append("-")
//								.append(Fingureprint).append("]");
//
//						StringBuilder Body = new StringBuilder("User Email [").append(AppUserEmail)
//								.append("]<br />").append(", User Password [")
//								.append(AppUserPassword).append("]<br />")
//								.append(", CambaseVendor [").append(Manufacturer).append("]<br />")
//								.append(", Model [").append(Model).append("]<br />")
//								.append(", Serial No [").append(SerialNo).append("]<br />")
//								.append(", Imei No [").append(ImeiNo).append("]<br />")
//								.append(", Device Fingureprint [").append(Fingureprint)
//								.append("]<br />").append(", Mac Address [").append(MacAddress)
//								.append("]<br />").append(", Bluetooth Name [")
//								.append(BlueToothName).append("]<br />")
//								.append(", GCM Device Registration ID [").append(regId)
//								.append("]<br />").append(", App Version [").append(AppVersion)
//								.append("]<br />").append("<br /><br /><br />");
//
//						Body = Body.append(exceptionMessage).append("<br />")
//								.append(exception.toString().replace("\n", "<br />"))
//								.append("<br /><br /><br />")
//								.append(Log.getStackTraceString(exception).replace("\n", "<br />"));
//
//						GMailSender sender = new GMailSender("cambatv.noreply@gmail.com",
//								"123marco123");
//						sender.sendMail(Subject.toString(), Body.toString(),
//								"cambatv.noreply@gmail.com",
//								"cambatv.noreply@gmail.com,sajjad.mahmood.khan@gmail.com");
//					}
//					catch (Exception e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//
//					return null;
//				}
//			}.execute(dto);
//		}
//		catch (Error e)
//		{
//		}
//		catch (Exception e)
//		{
//		}
//	}
//}