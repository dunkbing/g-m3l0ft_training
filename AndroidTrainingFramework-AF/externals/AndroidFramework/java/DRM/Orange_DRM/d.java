package APP_PACKAGE;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import java.lang.reflect.Method;
import java.lang.Class;
import android.telephony.TelephonyManager;


import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.installer.GameInstaller;

#define DEF_GetString(NAME)	private String getEncryptString##NAME() { java.util.Random mRandom = new java.util.Random( CUSTOMER_CARE_LICENSE ); String str = mEncrypter.encrypt(mRandom.nextLong()+"#"+FIRST_WIFI+"#"+ mRandom.nextInt()+"#"+mRandom.nextInt()); return str; }
#define FNC_GetString(NAME) getEncryptString##NAME()
#define DEF_GetCID(NAME, val) private String getCid##NAME(String val) { if (val == null) return ""; String x = z(); x = val + x + getLast(); return x; }
#define FNC_GetCID(NAME, val) getCid##NAME(val)

public class d
{
	
	private					h					mEncrypter;
	private					Context				context					= null;
	private					int 				state 					= -1;
	private					boolean				firstActivation			= true;
	private	volatile		HttpURLConnection 	m_urlc					= null;
	private volatile		boolean 			inProgress 				= false;
	private volatile		int 				errorCode 				= 0;
	private volatile		String 				respone 				= null;
	private	final			long 				CONN_TIMEOUT 			= 20000;
	private	final			int 				RECEIVEBUFFERSIZE 		= 16;
	private	final			int					UNLIMITED_LICENSE		= 0;
	private	final			int					LIMITED_LICENSE			= 1;
	private	final			int					SPARE_LICENSE			= 2;
	private	final			int					NOT_REACHABLE_LICENSE	= 3;
	private	final			int					CUSTOMER_CARE_LICENSE	= 4;
	public	final	static	int					SERVER_INTERNAL_ERROR	= 1;
	public	final	static	int					SERVER_BAD_PARAMETER	= 2;
	public	final	static	int					SERVER_BAD_CONTENT		= 3;
	public	final	static	int					SERVER_USER_NOT_ALLOWED	= 7;
	public	final	static	int					FIRST_WIFI				= 10;
	public	final	static	int					NO_NETWORK_CONNECTION	= 11;
	public	final	static	int					TIMEOUT					= 12;
	public	final	static	int					OTHER_ERROR				= 13;
	
	public 					int 				errorNumber 			= -1;
	private	final			String				serverURL				= j();
#if ORANGE_DRM_TEST
	private					String				key						= b();//"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4l/K2wpTSsrHUXB6f0452gfCt9eT3jKy+31muSwOwYoqFO5R2Bp0j5/o6mvbduTKPOALINrbWn+1SHWCtjvWPucypolRmoQ6WNwVKDJkd4mj57C4BX4NOb64r9Vk+vjj0A6R4OH+ir5xsb6vtnrrP+/I/AKikIqnwvdCtZAql82ElNWgB7w8KMBp3pzgWpPTopKSOmnbHrMl0TtyX7Qwm7IQo3Ju0itvJhppAjH3z5fZbGWIVoye9GGF+5c8OoRApaq66TLs6U9tdq3+4/7vto5i/YFCMiTFZ9/M+DRYfE5IA4qrZre0NQ011+1JRcBM9376lTmNAkNx5XHauLGKuwIDAQAB";
#else
	private					String				key						= h();//"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvO/PCEuvjCZkN7AoXAiWbxapkWNtiNKzvDJeQL+Ia+AlXJV3O4DsnFnu1C7XCsyvfI3x1MYPn2HiByPVl6UX51lKV5Rlfkz9M8zPobo7zTiK5BZl7sW3BEQzM3x6pEK95PoZy+XVB1j7tbz6FSe31ZSJM3+5pjlzquu/gjId9Rtxks9YVMAIXO/6h2zbTry+dqGKfXnz0yLYXzsqfkb2XIMtdA6ze2peftUf43s3VOmtPLFcMdyPjpDByT5Zu8yIwdhGG446LAAgyyZIlOnVQW2TFe18FYc6OkUjL5hFdDJ40mDeO4IzvrNJc3MxEfL7T2cQV0GZJ7w6OHu7IzQ+XwIDAQAB";
#endif
	private static			boolean				hasWifiBeenStopped		= false;
	
	
	
	public d(Context ct)
	{
		context = ct;
		mEncrypter = new h(key);
		SUtils.setContext(ct);
	}
	
	DEF_GetString(a)
	DEF_GetString(b)
	DEF_GetString(c)
	DEF_GetString(d)
	DEF_GetString(e)
	DEF_GetString(f)
	DEF_GetString(g)
	DEF_GetString(h)
	DEF_GetString(i)
	DEF_GetCID(a, u)
	DEF_GetCID(b, u)
	DEF_GetCID(c, u)
	DEF_GetCID(e, u)
	DEF_GetCID(f, u)
	DEF_GetCID(g, u)
	DEF_GetCID(h, u)
	DEF_GetCID(i, u)
	
	private String j()
	{
	#if ORANGE_DRM_TEST
		byte[] input = // "http://sso.orange.com/amppreprod_server/public/init"
		{ 
			104, 12, 0, -4, -54, -11, 0, 68, 0, -4, -65, 65, 3, -17, 13, -7, -2,
			-55, 53, 12, -2, -62, 50, 12, 3, 0, 2, -13, 11, 2, -3, -11, -5, 20, 
			-14, 13, 4, -17, 13, -67, 65, 5, -19, 10, -3, -6, -52, 58, 5, -5, 11 
		};
		
		for (int i = 0; i < 51; i++)
	#else
		byte[] input = // "http://sso.orange.com/amp_server/public/init"
		{ 
			104, 12, 0, -4, -54, -11, 0, 68, 0, -4, -65, 65, 3, -17, 13, -7, -2, 
			-55, 53, 12, -2, -62, 50, 12, 3, -17, 20, -14, 13, 4, -17, 13, -67, 
			65, 5, -19, 10, -3, -6, -52, 58, 5, -5, 11
		};
		
		for (int i = 0; i < 44; i++)
	#endif
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return (new String(input));
	}
	
	private String getCID()
	{
		DBG("OrangeDRM", "getCID()");
		return z() + mEncrypter.getMiddle() + getLast();
	}
	
	private String z()
	{
		byte[] input = { 71, 26, 12, -8, 7, 3, -9, 14, -21 };
		
		for (int i = 0; i < 9; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return (new String(input));
	}
	
	private String getLast()
	{
		String input = mEncrypter.getLast();
		
		return new String(input.getBytes());
	}
	
	private String c()
	{
		DBG("OrangeDRM", "getTID(): " + Device.getDeviceId());
		return Device.getDeviceId();
	}
	
	public boolean checkLicense()
	{
		DBG("OrangeDRM", "checkLicense()");
		if (hasToken())
			return localCheck();
		
		return onlineCheck();
	}
	
	private boolean hasToken()
	{
		DBG("OrangeDRM", "hasToken()");
		if (load() != null)
			return true;
		return false;
	}
	
	private boolean localCheck()
	{
		DBG("OrangeDRM", "localCheck()");
		if(!isTokenValid())
			return onlineCheck();
		return true;
	}
	
	private boolean isTokenValid()
	{
		DBG("OrangeDRM", "isTokenValid()");
		String value = load();
		
		String tid = value.substring(0, value.indexOf("#"));
		value = value.substring(value.indexOf("#") + 2);
		
		String cid = value.substring(0, value.indexOf("#"));
		value = value.substring(value.indexOf("#") + 2);
		
		int type = Integer.parseInt(value.substring(0, value.indexOf("#")));
		value = value.substring(value.indexOf("#") + 2);
		
		java.util.Calendar date = getDate(value);
		int validity = getValidity(value);

		switch (type)
		{
		case LIMITED_LICENSE:
		case SPARE_LICENSE:
		case NOT_REACHABLE_LICENSE:
			if (!w(date, validity))
				return false;
		case CUSTOMER_CARE_LICENSE:
		case UNLIMITED_LICENSE:
			if (!checkTID(tid))
				return false;
			if (!checkCID(cid))
				return false;
		}
		return true;
	}
	
	private boolean w(java.util.Calendar date, int validity)
	{
		date.add(java.util.Calendar.HOUR, validity);
		java.util.Calendar now = java.util.Calendar.getInstance();
		DBG("OrangeDRM", "checkDate() " + date.getTime() + " " + now.getTime() + " " + now.before(date));
		return now.before(date);
	}
	
	private boolean checkTID(String tid)
	{
		DBG("OrangeDRM", "checkTID(): " + tid + " " + tid.equals(c()));
		return tid.equals(c());
	}
	
	private boolean checkCID(String cid)
	{
		DBG("OrangeDRM", "checkCID(): " + cid + " " + cid.equals(getCID()));
		return cid.equals(getCID());
	}
	
	private java.util.Calendar getDate(String value)
	{
		DBG("OrangeDRM", "getDate(): " + value);
		int year = Integer.parseInt(value.substring(0, 4));
		value = value.substring(5);
		
		int month = Integer.parseInt(value.substring(0, 2));
		value = value.substring(3);
		
		int day = Integer.parseInt(value.substring(0, 2));
		value = value.substring(3);
		
		int hour = Integer.parseInt(value.substring(0, 2));
		value = value.substring(3);
		
		int min = Integer.parseInt(value.substring(0, 2));
		value = value.substring(3);
		
		int sec = Integer.parseInt(value.substring(0, 2));
		value = value.substring(2);
		
		char sign = value.charAt(0);
		value = value.substring(1);
		
		String hourTZ = value.substring(0, 2);
		value = value.substring(2);
		
		String minTZ = value.substring(0, 2);
		value = value.substring(3);
		
		java.util.Calendar tokenCal = new java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("GMT" + (sign + "" + hourTZ + ":" + minTZ)));
		
		tokenCal.set(year, month - 1, day, hour, min, sec);
		DBG("OrangeDRM", "getDate(): " + tokenCal.getTime());
		return tokenCal;
	}
	
	private int getValidity(String value)
	{
		DBG("OrangeDRM", "getValidity() " + Integer.parseInt(value.substring(value.indexOf("#") + 2)));
		return Integer.parseInt(value.substring(value.indexOf("#") + 2));
	}
	
	private boolean onlineCheck()
	{
		errorNumber = -1;
		DBG("OrangeDRM", "onlineCheck()");
		if (!hasNetworkConnection())
			return showErrorMessage(NO_NETWORK_CONNECTION);
		
		if (!isUsingRadioBearer())
		{
		#if !BUILD_FOR_FIRMWARE_1_6
			if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT)
			{
				if (firstActivation && f((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)))
				{
					firstActivation = false;
					return onlineCheck();
				}
				else
					return showErrorMessage(FIRST_WIFI);
			}
		#endif
		}
		
		int code = serverLicense();
		
		if (hasWifiBeenStopped)
		{
			((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
			hasWifiBeenStopped = false;
		}
		
		if (code == 0)
		{
			save(d(respone, 1));
			return true;
		}
		return showErrorMessage(code);
	}
	
	private boolean hasNetworkConnection()
	{
		DBG("OrangeDRM", "hasNetworkConnection()");
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null && cm.getActiveNetworkInfo() != null)
			return cm.getActiveNetworkInfo().isConnected();
		return false;
	}
	
	private boolean isUsingRadioBearer()
	{
		DBG("OrangeDRM", "isUsingRadioBearer()");
		boolean isRadio = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null && cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI) != null)
		{
			long start = System.currentTimeMillis();
			WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (wm.isWifiEnabled())
			{
				wm.setWifiEnabled(false);
				hasWifiBeenStopped = true;
			}
			
			DBG("OrangeDRM", cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI) + "\n" + (System.currentTimeMillis() - start) + "<" + CONN_TIMEOUT);
			if (!cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).isConnected())
				try { Thread.sleep(3000); } catch (Exception e) {}
			while (
				(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState() == android.net.NetworkInfo.State.CONNECTING)
				&& (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState() == android.net.NetworkInfo.State.DISCONNECTED)
				&& (System.currentTimeMillis() - start < CONN_TIMEOUT))
			{
				try { Thread.sleep(100); } catch (Exception e) {}
			}
			DBG("OrangeDRM", cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).isConnected() + " " + cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI) );
			isRadio = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).isConnected();
		}
		if (isRadio)
			return true;
		
		return false;
	}
	
	private void save(String value)
	{
		String var = value.trim() + "#";
		DBG("OrangeDRM", "save() " + value);
		SUtils.setPreference("OD", FNC_GetString(a), "OLHA");
		SUtils.setPreference("OD", FNC_GetString(b), "OLHB");
		SUtils.setPreference("OD", FNC_GetString(c), "OLHC");
		SUtils.setPreference("OD", var.substring(0,var.length() - 1), "OLHD");
		SUtils.setPreference("OD", FNC_GetString(e), "OLHE");
		SUtils.setPreference("OD", FNC_GetString(f), "OLHF");
		SUtils.setPreference("OD", FNC_GetString(g), "OLHG");
		SUtils.setPreference("OD", FNC_GetString(h), "OLHH");
		SUtils.setPreference("OD", FNC_GetString(i), "OLHI");
	}
	
	private String getCidd(String val)
	{
		if (val == null)
			return "";
		return val;
	}
	
	private String load()
	{
		try
		{
			DBG("OrangeDRM", "load() " + mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHD")));
			String value2 = FNC_GetCID(a, mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHA")));
			String value3 = FNC_GetCID(b, mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHB")));
			String value1 = FNC_GetCID(c, mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHC")));
			String value = getCidd(mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHD")));
			String value4 = FNC_GetCID(e, mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHE")));
			String value5 = FNC_GetCID(f, mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHF")));
			String value6 = FNC_GetCID(g, mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHG")));
			String value7 = FNC_GetCID(h, mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHH")));
			String value8 = FNC_GetCID(i, mEncrypter.decrypt(SUtils.getPreferenceString("OD", "OLHI")));
			try
			{
				if (value2 == "OD" && value6 == "UT" && value4 == "12")
					return value6;
			}
			catch (Exception e) { } 
			if (value == "")
				return null;
			return value;
		}
		catch (Exception e) 
		{
			return null;
		}
		
	}

#if !BUILD_FOR_FIRMWARE_1_6
	private boolean f(ConnectivityManager cm)
	{
		DBG("OrangeDRM", "switchToMobile()");
		int resultInt = cm.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
		DBG("OrangeDRM", "startUsingNetworkFeature for enableHIPRI result: " + resultInt);
		try { Thread.sleep(1000); } catch (Exception e) {}
		if (-1 == resultInt) 
		{
			DBG("OrangeDRM", "Wrong result of startUsingNetworkFeature, maybe problems");
			return false;
		}
		
		DBG("OrangeDRM", "Using Mobile connection.");
		return true;
	}
#endif

	private boolean showErrorMessage(int error)
	{
		DBG("OrangeDRM", "showErrorMessage(" + error + ")");
		errorNumber = error;
		return false;
	}
	
	public int getErrorCode()
	{
		int result = errorNumber;
		errorNumber = -1;
		return result;
	}
	
	private int serverLicense()
	{
		DBG("OrangeDRM", "serverLicense()");
		long callstarttime = System.currentTimeMillis();
		inProgress = true;
		errorCode = 0;
		new Thread()
		{
			public void run()
			{
				respone = null;
				DBG("OrangeDRM", "HTTP: run:connecting to [" + serverURL + "]");
				try
				{
					URL url = new URL(serverURL);
					
					m_urlc = (HttpURLConnection)url.openConnection();
					m_urlc.setRequestMethod("GET");
					m_urlc.setRequestProperty("Connection", "close");
					
					m_urlc.setRequestProperty("X_AMP_CID", mEncrypter.encrypt(getCID()));
					DBG("OrangeDRM", "Send CID:" + getCID() + " | Encoded: " + mEncrypter.encrypt(getCID()));
					m_urlc.setRequestProperty("X_AMP_TID", mEncrypter.encrypt(c()));
					DBG("OrangeDRM", "Send TID:" + c() + " | Encoded: " + mEncrypter.encrypt(c()));
					
					if (m_urlc.getResponseCode() != HttpURLConnection.HTTP_OK)
					{
						errorCode = m_urlc.getResponseCode();
						DBG("OrangeDRM", "HTTP RESPONSE CODE RECEIVED = " + errorCode);
					}
					
					java.io.InputStream m_is = m_urlc.getInputStream();
					java.io.ByteArrayOutputStream bao = new java.io.ByteArrayOutputStream();
					byte[] abInBuffer = new byte[RECEIVEBUFFERSIZE];
					int nBytesRead = 0;
					while (nBytesRead != -1) 
					{
						nBytesRead = m_is.read(abInBuffer, 0, RECEIVEBUFFERSIZE);
						if (nBytesRead != -1)
							bao.write(abInBuffer, 0, nBytesRead);
					}
				 DBG("OrangeDRM", "HTTP: run: received [\n" + bao.toString() + "\n]");
				 DBG("OrangeDRM", "HTTP: run: total bytes read: [" + bao.size() + "]");
				 
				 respone = bao.toString();
				 m_urlc.disconnect();
				 errorCode = Integer.parseInt(d(respone, 0));
				 inProgress = false;
				}
				catch (java.net.SocketException se)
				{
					DBG_EXCEPTION(se);
					ERR("OrangeDRM", "HTTP: run: SocketException : " + se.toString());
					errorCode = OTHER_ERROR;
					inProgress = false;
				}
				catch (java.net.UnknownHostException uhe)
				{
					DBG_EXCEPTION(uhe);
					ERR("OrangeDRM", "HTTP: run: UnknownHostException : " + uhe.toString());
					errorCode = OTHER_ERROR;
					inProgress = false;
				}
				catch (Exception e)
				{
					DBG_EXCEPTION(e);
					ERR("OrangeDRM", "HTTP: run: exception : " + e.toString());
					errorCode = OTHER_ERROR;
					inProgress = false;
				}
			}
		}.start();
		
		do
		{
			DBG("OrangeDRM", "HTTP: wait...");
			try { Thread.sleep(100); } catch (Exception e) {}
		} while (inProgress && (System.currentTimeMillis() - callstarttime < CONN_TIMEOUT));
		
		if (inProgress && (System.currentTimeMillis() - callstarttime >= CONN_TIMEOUT))
			return TIMEOUT;
		
		return errorCode;
	}
	
	private String d(String text, int choise)
	{
		DBG("OrangeDRM", "parse(" + text + ", " + choise + ")");
		
		try
		{
			if (choise == 0)
			{
				DBG("OrangeDRM", (text.indexOf(f()) + 12) + " " + (text.indexOf(q())));
				String result = text.substring(text.indexOf(f()) + 12, (text.indexOf(q())));
				DBG("OrangeDRM", f() + result + q());
				return result;
			}
			else
			{
				DBG("OrangeDRM", (text.indexOf(r()) + 7) + " " + (text.indexOf(o())));
				String result = text.substring(text.indexOf(r()) + 7, (text.indexOf(o())));
				DBG("OrangeDRM", r() + result + o());
				DBG("OrangeDRM", r() + mEncrypter.decrypt(result) + o());
				return result;
			}
		}
		catch (Exception e)
		{
			ERR("OrangeDRM", "parse(EXCEPTION)");
		}
		return "";
		 
	}
	
	private String h()
	{
		byte[] input = {
			77, -4, 0, -7, 7, 33, -41, 13, -12, 37, 4, 6, -9, 3, -2, -34, -14, 62,
			-71, 18, -1, 16, -12, 1, -5, 0, 14, -12, -2, 16, -25, 9, 12, -4, 0, -7,
			1, 36, -28, -8, -2, 16, -12, -4, 53, -39, -32, 33, -13, 2, 48, 1, -12,
			-39, 23, 17, -29, -23, 10, 46, -23, -23, 40, -18, 11, 22, -23, 15, -5,
			-20, -9, 38, -11, -27, -3, 47, -4, -50, 6, 27, -20, -5, -33, 30, 24, -54,
			22, 43, -20, -14, 12, -35, 28, -27, 16, 47, -5, -40, 40, 7, -68, 18, -12,
			33, -21, 48, 6, -3, -16, -29, -22, 69, -71, 28, 12, -9, 30, -60, 22, 33,
			-39, 55, -41, 6, 22, -54, 31, 3, -35, -4, 59, -33, 11, -33, 29, 26, -6,
			5, 15, -65, 20, -21, 66, -42, 31, -13, 13, -56, 67, -38, 21, -30, -22,
			13, 24, 18, -53, 60, -28, -36, 15, 3, 12, 41, -45, -26, 69, -66, 58, -43,
			6, -18, -4, 27, 31, -21, 31, -78, 45, -2, -20, -17, 57, -51, 61, -18, 24,
			-68, 16, 13, 18, -50, -2, 41, -7, -9, 3, -26, -8, 10, 59, -6, 2, 14, -9,
			4, 0, -70, 56, 3, -33, 27, -43, 25, 34, 4, -13, 8, -58, 32, -3, -9, -12,
			8, 15, -9, -32, 7, 50, -54, 72, -24, -14, 30, 7, -78, 57, 13, -42, 4, 27,
			-14, 22, 12, -74, 73, -45, 13, -1, 34, -7, -2, -11, 5, -9, -48, 38, -15, 
			4, 39, -16, -35, -11, 68, -21, -51, 62, -11, 1, 14, -31, 17, -50, -1, 64,
			-64, 35, -7, 30, 7, -36, -4, -6, 29, -22, 23, 21, -41, 26, 6, -44, -2, 55,
			-37, -31, 37, 27, -61, 65, -48, 46, -19, 4, -33, 0, -19, 0, 2, 22, -11, 0,
			38, 18, 0, -31, -17, 35, -29, 31, -24, -5, 6, -37, 34, -14, 31, -52, 7, 14,
			19, 10, -45, 25, 28, -22, 21, -30, -23, 51, -34, 30, -32, 6, -22, -4, 61,
			-41, 33, -22, -27, 21, 49, -4, -4, -36, -4, 25, -48, 26, 43, -51, 33, -26,
			-21, 29, -34, 49, -18, 5, -38, 23, 19, -16, -19, 64, -65, 25, -7, 45, -62,
			18, 49, -41, -38, 45, 31, -46, -5, -3, 16, -16, 1
		};
		
		for (int i = 0; i < 0x188; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return new String(input);
	}
	
	private String f() // returns <respstatus>
	{
		byte[] input = { 60, 54, -13, 14, -3, 3, 1, -19, 19, 1, -2, -53 };
		
		for (int i = 0; i < 12; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return new String(input);
	}
	private String q() // returns </respstatus>
	{
		byte[] input = { 60, -13, 67, -13, 14, -3, 3, 1, -19, 19, 1, -2, -53};
		
		for (int i = 0; i < 13; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return new String(input);
	}
	private String r() // returns <token>
	{
		byte[] input = { 60, 56, -5, -4, -6, 9, -48 };
		
		for (int i = 0; i < 7; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return new String(input);
	}
	private String o() // returns </token>
	{
		byte[] input = { 60, -13, 69, -5, -4, -6, 9, -48 };
		
		for (int i = 0; i < 8; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return new String(input);
	}
	
	private String b()
	{
		byte[] input = {
			77, -4, 0, -7, 7, 33, -41, 13, -12, 37, 4, 6, -9, 3, -2, -34, -14, 62, -71,
			18, -1, 16, -12, 1, -5, 0, 14, -12, -2, 16, -25, 9, 12, -4, 0, -7, 1, 36,
			-28, -8, -2, 16, -12, -4, -13, 56, -61, 28, -25, 69, -7, -28, -1, 32, -1,
			-42, 13, 3, -22, -12, 48, -54, 4, 1, -3, 53, -1, -35, 49, -59, 44, -17, -33,
			55, -31, 46, -78, 8, -2, 60, 8, -34, 36, -40, 40, -30, 22, 2, -43, 9, -26,
			29, -32, 16, 46, -64, 58, -53, -6, 64, -57, 55, 9, -20, 2, 17, -33, -9, 5,
			-1, -14, 11, -3, 5, 36, -16, -11, 23, -67, 6, 34, -11, 15, -20, 49, -10, 12,
			-31, -7, 37, -18, 22, -9, -1, -3, -26, 27, 2, -30, -27, 33, -9, 41, -33, -11,
			-7, 6, 33, -7, -48, 57, -3, -53, 2, 12, -15, 14, 22, -36, 26, 1, 19, -44, -2,
			62, -57, 29, 21, -64, 75, -12, 0, -58, 17, -11, 28, -30, 27, -7, -29, 62, 9,
			-61, 67, -5, -17, -44, 64, -2, -6, 4, 0, -34, -37, 4, 26, -26, 18, 10, 30, 2,
			-34, 40, -3, 9, -1, -18, -33, 49, -26, -25, 48, -5, -52, -6, 19, 39, -30, 9,
			16, -37, -11, 64, -63, 19, 2, -11, 46, -61, 61, 10, -19, -16, 25, -32, 4, 27,
			1, -37, 8, -4, 30, 1, -12, -26, 42, -37, 31, -60, 36, 32, 5, -33, -33, 26,
			38, -10, -54, 18, 8, 30, -60, 23, 43, -69, 57, 11, 2, -44, 30, 8, 0, -47, 41,
			-34, -21, 71, -69, 49, -12, 8, -27, 16, -14, 13, 25, 10, -20, -44, 14, 0, -1,
			-27, 10, 46, -43, 23, 32, -29, -17, 47, -15, 16, -59, 0, 30, -8, 39, -61, 31,
			-28, 59, -16, 13, -62, -8, 9, -5, 8, 63, -2, -5, -58, 52, -58, 42, -19, -3,
			10, 28, -21, -14, 20, -33, -10, 30, -34, 25, 14, 7, 13, -33, -16, 20, -8,
			-13, 61, 1, -24, 24, -13, -53, 30, 3, -33, 1, 0, -6, 6, 25, 8, 17, -33, 11,
			-20, -6, 4, -1, 54, -24, 25, -31, -13, 42, -29, 42, -67, 35, -16, 25, 20,
			-41, -5, 4, 42, 2, -46, -5, -3, 16, -16, 1
		};
		
		for (int i = 0; i < 0x188; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return new String(input);
	}
}