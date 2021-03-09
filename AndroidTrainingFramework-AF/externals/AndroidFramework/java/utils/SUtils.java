package APP_PACKAGE.GLUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.Vector;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Iterator;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import android.os.StatFs;
import android.os.Bundle;

#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
import APP_PACKAGE.billing.common.Constants;
import APP_PACKAGE.billing.common.LManager;
#endif
#if USE_INSTALLER
import APP_PACKAGE.installer.GameInstaller;
#endif
import APP_PACKAGE.R;

import 	android.net.ConnectivityManager;
import	android.net.NetworkInfo;

import APP_PACKAGE.GLUtils.NetworkStateReceiver;

import android.text.TextUtils;
import android.content.pm.PackageManager;
#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
import APP_PACKAGE.GLUtils.DefReader;
#endif

public class SUtils implements Config
#if USE_BILLING
	, Constants
#endif
{
	
    private static Context	currentContext 	= null;

	//type of the current device's connection
	public final static int NO_CONNECTIVITY 			= 0;
	public final static int CONNECTIVITY_WIFI 			= 1;
	public final static int CONNECTIVITY_BLUETOOTH		= 2;
	public final static int CONNECTIVITY_DUMMY 			= 3;
	public final static int CONNECTIVITY_ETHERNET 		= 4;
	public final static int CONNECTIVITY_WIMAX 			= 5;
	public final static int CONNECTIVITY_2G 			= 6;
	public final static int CONNECTIVITY_3G 			= 7;
	public final static int CONNECTIVITY_4G 			= 8;
	public final static int CONNECTIVITY_UNKNOWN		= 9;

	//*************************************************************************************************************
	//* METHODS
	//*************************************************************************************************************
    
    /**
     * Set the current and save it into currentContext
     * @param  context the context from the application
     */
    public static void setContext(Context context)
    {
		DBG("SUtils","Set context");
        currentContext = context;
    }

    /**
     * Get the current context
     * @param  context the context from the application
     * @return an Context object
     */
    public static Context getContext()
    {
        if (currentContext == null)
        {
        	ERR("SUtils", "getContext currentContext NOT PREVIOUSLY SET!!!");
        }
        return currentContext;
    }
     
	public static void release()
	{
		DBG("SUtils","Release context");
		currentContext = null;
	}
	
	public static void release(Context context)
	{
		DBG("SUtils","Trying to release context");
		if( currentContext == context)
		{
			DBG("SUtils","Releasing context for BootCompleted");
			currentContext = null;
		}
	}
	
	public static void runOnUiThread(Runnable thread)
	{
		Activity a = (Activity) getContext();
		a.runOnUiThread(thread);
	}
	
	public static boolean isAirplaneModeOn()
    {
		return Settings.System.getInt(currentContext.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	
    public static void setAirplaneMode(boolean status)
	{
		boolean isAirplaneModeOn = isAirplaneModeOn();
		
		if(isAirplaneModeOn && status)
        {
			return;
        }else if(!isAirplaneModeOn && !status)
        {
			return;
        }
		
        if(isAirplaneModeOn && !status)
        {
			Settings.System.putInt(currentContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", 0);
            currentContext.sendBroadcast(intent);
			return;
        }
        if(!isAirplaneModeOn && status)
        {
			Settings.System.putInt(currentContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", 1);
            currentContext.sendBroadcast(intent);
            return;
        }
    }

   	//private static String serialkey = "";
	public static String GetSerialKey()
    {
    	InputStream reader;
		String serialkey = null;
    	try
    	{
		#if USE_BILLING || USE_INSTALLER
    		InputStream inputstream = SUtils.getContext().getResources().openRawResource(R.raw.serialkey);
    		BufferedReader bReader = new BufferedReader(new InputStreamReader(inputstream,Charset.forName("UTF-8")));
   		  	serialkey = bReader.readLine();
		#endif
   		  	if (serialkey == null)
   		  		serialkey="null";
    	}catch(Exception ex){serialkey = "null";}
	
    	return serialkey;
    }
	
#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	private static LManager mLMnger = null;
	public static LManager getLManager()
	{
		if (mLMnger == null)
			mLMnger = new LManager();
		
		return mLMnger;
	}
#endif
	
    /**
     * Open an asset file using a streaming mode.
     * @param  fileName the asset to open
     * @return an InputStream
     */
    public static InputStream getResourceAsStream(String fileName)
    {
        AssetManager am = currentContext.getAssets();
        InputStream is = null;

        try
        {
            is = am.open(fileName);
        }
        catch (IOException e)
        {
            DBG_EXCEPTION(e);
        }
        return is;
    }

    /**
     * Read a text file in the specified encoding, and return a String array
     * with each line as a new element.
     * @param  fileName the asset to open
     * @return an InputStream
     */
    protected static String[] readTextFile(String fileName, String encoding)
    {
        String strTextFileInOneString = null;
        String [] strTextFileInStringArray = null;

        InputStream is = SUtils.getResourceAsStream(fileName);
        try
        {
            byte[] data = new byte[is.available()];
            is.read(data);
            strTextFileInOneString = new String(data, encoding);
            is.close();
        }
        catch (IOException e)
        {
            DBG_EXCEPTION(e);
        }
        catch (Exception e)
        {
            DBG_EXCEPTION(e);
        }

        if (strTextFileInOneString != null)
        {
            Vector<String> vtrTextFileLines = new Vector<String>();
            String line = "";

            for (int i = 0; i < strTextFileInOneString.length(); i++)
            {
                char c = strTextFileInOneString.charAt(i);
                if (c == '\n')
                {
                    vtrTextFileLines.addElement(line);
                    line = "";
                }
                else
                {
                    line += c;
                }
            }
            strTextFileInStringArray = new String[vtrTextFileLines.size()];
            vtrTextFileLines.copyInto(strTextFileInStringArray);
        }
        return strTextFileInStringArray;
    }
	
	public static boolean WriteFile(String filename,String data)
	{
		FileOutputStream fOut = null;
		OutputStreamWriter osw = null;
		try
		{
			try
			{
				DBG("SUtils", "WriteFile(), File = " + filename);
				File file = new File(filename);
				if(!file.exists())
				{
					File parent = new File(file.getParent());
					parent.mkdirs();
					parent = null;
					
					file.createNewFile();
				}
				file = null;
			} catch (Exception ex) {}

			fOut = new FileOutputStream(filename);
			osw = new OutputStreamWriter(fOut);
			osw.append(data);
			osw.flush();
			osw.close();
			fOut.close();
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}		
		return true;
	}
	
	public static boolean WriteFile(String filename,byte[] data)
	{
		FileOutputStream fOut = null;
		DataOutputStream osw = null;
		try
		{
			try
			{
				DBG("SUtils", "WriteFile(), File = " + filename);
				File file = new File(filename);
				if(!file.exists())
				{
					File parent = new File(file.getParent());
					parent.mkdirs();
					parent = null;
					
					file.createNewFile();
				}
				file = null;
			} catch (Exception ex) {}
			fOut = new FileOutputStream(filename);
			osw = new DataOutputStream(fOut);
			osw.write(data, 0, data.length);
			osw.flush();
			osw.close();
			fOut.close();
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}		
		return true;
	}
	
	public static String ReadFile(String filename)
	{		
		String data = null;
		try
		{
			File file = new File(filename);
			byte fileContent[] = new byte[(int)file.length()];
			
			FileInputStream fin = new FileInputStream(file);
			fin.read(fileContent);			
			fin.close();
			data = new String(fileContent);
		}
		catch (Exception e){DBG_EXCEPTION(e);}		
		return data;
	}
	
	public static byte[] ReadFileByte(String filename)
	{		
		String data = null;
		try
		{
			File file = new File(filename);
			byte fileContent[] = new byte[(int)file.length()];
			
			FileInputStream fin = new FileInputStream(file);
			fin.read(fileContent);			
			fin.close();
			return fileContent;
		}
		catch (Exception e){DBG_EXCEPTION(e);}		
		return null;
	}

	public static String ReadFile(int filename)//read file from raw resource
	{
		InputStream raw = null;
		raw = currentContext.getResources().openRawResource(filename);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		int i;
		try
		{
			i = raw.read();
			while (i != -1)
			{
				byteArrayOutputStream.write(i);
				i = raw.read();
			}
			raw.close();
		}
		catch (IOException e)
		{
			DBG_EXCEPTION(e);
		}
		return byteArrayOutputStream.toString();
	}
	
	public static byte[] ReadFileByte(int filename)//read file from raw resource
	{
		InputStream raw = null;
		raw = currentContext.getResources().openRawResource(filename);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		int i;
		try
		{
			i = raw.read();
			while (i != -1)
			{
				byteArrayOutputStream.write(i);
				i = raw.read();
			}
			raw.close();
		}
		catch (IOException e)
		{
			DBG_EXCEPTION(e);
		}
		return byteArrayOutputStream.toByteArray();
	}
	
	/**
	 * Returns a custom setting from a specified file
	 * @param filename the filename that contains the configurations
	 * @param tag the configuration's tag
	 */
	public static String getOverriddenSetting(String filename, String tag)
	{
		String data = null;
		try
		{
			FileReader file = new FileReader(filename);
			BufferedReader br = new BufferedReader(file);
			while ((data = br.readLine()) != null)
			{
				if (data.startsWith(tag))
				{
					data = (data.substring(data.indexOf("=") + 1)).trim();
					break;
				}
			}
			br.close();
			file.close();
			return data;
		}
		catch (Exception e){DBG("SUtils", "Testing configuration file not found.It's path: "+filename);}
		return null;
	}
	
	public static void setOverriddenSetting(String filename, String tag, String value)
	{
		String data = null;
		String data_read = null;
		try
		{
			FileReader file = new FileReader(filename);
			BufferedReader br = new BufferedReader(file);
			int line_read = 0;
			while ((data_read = br.readLine()) != null)
			{
				if (data_read.startsWith(tag))
				{
					DBG("SUtils", "Data read:"+data_read);
					data_read = (data_read.substring(0, data_read.indexOf("=")+1)).trim();
					DBG("SUtils", "Data read:"+data_read);
					line_read ++;
					break;
				}
			}
			br.close();
			file.close();
			
			DBG("SUtils", "now writting...");
			
			FileWriter fstream = null;
			BufferedWriter out = null;
			if(data_read != null)
			{
				fstream = new FileWriter(filename);
				out = new BufferedWriter(fstream);
				//DBG("SUtils", "d l= "+data_read.length());
				//DBG("SUtils", "v l= "+value.length());
				//out.write(value, data_read.length(), value.length());
			}
			else
			{
				fstream = new FileWriter(filename, true);
				out = new BufferedWriter(fstream);
				//out.newLine();
				//out.write(tag+"="+value);
			}
			while(line_read >= 0)
			{
				out.newLine();
				line_read--;
			}
			out.write(tag+"="+value);
			
			DBG("SUtils", "done writting");
			
			out.close();
			fstream.close();
		}
		catch (Exception e){DBG_EXCEPTION(e);DBG("SUtils", "Exception"+ e.getMessage());}
	}

#if USE_INSTALLER && GOOGLE_MARKET_DOWNLOAD
	public static void resetValusForMAinAndPatch()
	{
		try
		{
			SUtils.setPreference("PatchPath", "", "ExpansionPrefs");
			SUtils.setPreference("PatchSize", 0L, "ExpansionPrefs");
			SUtils.setPreference("MainPath", "", "ExpansionPrefs");
			SUtils.setPreference("MainSize", 0L, "ExpansionPrefs");
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}
	public static boolean doesZipContains(String fileName, String zipPath)
	{
		try 
		{
			final java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zipPath);
			final java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();
			java.util.zip.ZipInputStream zipInput = null;
			while (entries.hasMoreElements())
			{
				final java.util.zip.ZipEntry zipEntry = entries.nextElement();
				if (!zipEntry.isDirectory())
				{
					final String name = zipEntry.getName();
					if (name.endsWith(fileName))
					{
						return true;
					}
				}
			}
			zipFile.close();
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
		return false;
	}
	private static void saveLocation(SharedPreferences.Editor editor, String zipPath)
	{
		try 
		{
			final java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zipPath);
			final java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();
			java.util.zip.ZipInputStream zipInput = null;
			while (entries.hasMoreElements())
			{
				final java.util.zip.ZipEntry zipEntry = entries.nextElement();
				if (!zipEntry.isDirectory())
				{
					final String name = zipEntry.getName();
					editor.putString(name, zipPath);
				}
			}
			zipFile.close();
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}
	public static void initializeTheFilesList()
	{
		SharedPreferences settings = SUtils.getContext().getSharedPreferences("FilesLocation", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		String extraPath = getPreferenceString("SDFolder", SD_FOLDER, GameInstaller.mPreferencesName) + File.separator + SUtils.getPreferenceString("ExtraFile", "", GameInstaller.mPreferencesName);
		String patchPath = android.os.Environment.getExternalStorageDirectory() + "/Android/obb/" + STR_APP_PACKAGE + File.separator + SUtils.getPreferenceString("PatchFileName", "", "ExpansionPrefs");
		String mainPath = android.os.Environment.getExternalStorageDirectory() + "/Android/obb/" + STR_APP_PACKAGE + File.separator + SUtils.getPreferenceString("MainFileName", "", "ExpansionPrefs");
		File file = new File (mainPath);
		if (file.exists() && !file.isDirectory())
			saveLocation(editor, mainPath);
		file = new File (patchPath);
		if (file.exists() && !file.isDirectory())
			saveLocation(editor, patchPath);
		file = new File (extraPath);
		if (file.exists() && !file.isDirectory())
			saveLocation(editor, extraPath);
		editor.commit();
	}
	public static String getZipPathForFile(String fileName)
	{
		SharedPreferences settings = SUtils.getContext().getSharedPreferences("FilesLocation", 0);
		String res = settings.getString(fileName, "");
		if (res == "")
			res = null;
		return res;
	}
	public static Vector<Pair<String, String>> getAllSoFilesPairs()
	{
		SharedPreferences settings = SUtils.getContext().getSharedPreferences("FilesLocation", 0);
		HashMap<String,String> haspMap = (HashMap<String,String>)settings.getAll();
		Iterator itr = haspMap.entrySet().iterator();
		Vector<Pair<String, String>> result = new Vector<Pair<String, String>>();
		while(itr.hasNext())
		{
			Map.Entry pairs = (Map.Entry)itr.next();
			Pair<String, String> item = new Pair(pairs.getKey(), pairs.getValue());
			if (item.first.endsWith(".so"))
				result.add(item);
			itr.remove();
		}
		return result;
	}
	public static String extractSOFile(Pair<String, String> soPair)
	{
		try 
		{
			ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(soPair.second));;
			java.util.zip.ZipEntry zipEntry = zipinputstream.getNextEntry();
			while (zipEntry != null)
			{
				if (!zipEntry.isDirectory())
				{
					final String name = zipEntry.getName();
					if (name.endsWith(soPair.first))
					{
						byte[] buf = new byte[1024];
						DBG("SUtils", "Extract so: " + GameInstaller.LIBS_PATH + "/" + soPair.first);
						File file = new File(GameInstaller.LIBS_PATH);
						file.mkdirs();
						FileOutputStream fileoutputstream = new FileOutputStream( GameInstaller.LIBS_PATH + "/" + soPair.first);
						int n;
						while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
							fileoutputstream.write(buf, 0, n);
						GameInstaller.makeLibExecutable(GameInstaller.LIBS_PATH + "/" + soPair.first);
						return soPair.first;
					}
				}
				zipEntry = zipinputstream.getNextEntry();
			}
			zipinputstream.close();
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
		return null;
	}
#endif
	/**
	 * Returns a boolean value from custom setting from a specified file
	 * @param filename the filename that contains the configurations
	 * @param tag the configuration's tag
	 */
	public static boolean getOverriddenSettingBoolean(String filename, String tag)
	{
		String value = getOverriddenSetting(filename,tag);
		if(value != null && value.length()>0)
			return Boolean.valueOf(value);
			
		return false;
	}
    /**
     *  Returns a random number in a range between minNumber and maxNumber
     *  @param minNumber the minimum value that the random can be
     *  @param maxNumber the maximum value that the random can be
     */
    protected static int getUniqueCode(int minNumber, int maxNumber)
    {
        double rnd = new Random().nextDouble();
        int randomNumber = (int)(((maxNumber - minNumber + 1) * rnd ) + minNumber );

        return randomNumber;
    }

    /**
     * Converts an array of Objects to a String array
     * @param List to convert
     * @return	An array of Strings
     */
    protected static String[] objectArrayToStringArray(Object[] o)
    {
        String[] strArray = new String[o.length];
        for (int i = 0; i < strArray.length; i++)
        {
            strArray[i] = o[i].toString();
        }
        return strArray;
    }

    /**
     * Show a message on the device with a short toast
     * @param baseContext context of the activity from witch the message will be shown
     * @param string The message to show
     */
    protected static void logWindowMessage(Context baseContext, String string) {
        if(ENABLE_DEBUG){
            Toast.makeText(baseContext, string, Toast.LENGTH_SHORT).show();
        }
    }
	/**/
	
    /**
     * Set a Shared Preference from the application
     * @param key the key in a String representation
     * @param value the value to store in an Object instance
     * @param pname the preference name
     */
	public static void setPreference(String key, Object value, String pname)
    {
		DBG("SUtils", "setPreferences(" + key + ", " + value + ", " + pname + ")");
    	SharedPreferences settings = SUtils.getContext().getSharedPreferences(pname, 0);
        SharedPreferences.Editor editor = settings.edit();

        if (value instanceof String)
        {
            editor.putString(key, (String) value);
        }
        else if (value instanceof Integer)
        {
            editor.putInt(key, (Integer) value);
        }
        else if (value instanceof Boolean)
        {
            editor.putBoolean(key, (Boolean) value);
        }
        else if(value instanceof Long)
        {
        	editor.putLong(key, (Long)value);
        }
        editor.commit();
    }

    /**
     * To read a string from the preferences file.
     * @param key The key that identifies the preferences file.
     * @param pname the preference name
     * */
    public static String getPreferenceString(String key, String pname)
    {
        SharedPreferences settings = SUtils.getContext().getSharedPreferences(pname, 0);
        String res = settings.getString(key, "");
		DBG("SUtils", "getPreferenceString(" + key + ", " + res  + ", " + pname + ")");
        return res;

    }
	
	/**
     * To read a string from the preferences file.
     * @param key The key that identifies the preferences file.
     * @param defaultValue The default value to be returned if the preference does not exist.
     * @param pname the preference name
     * */
    public static String getPreferenceString(String key, String defaultValue, String pname)
    {
        SharedPreferences settings = SUtils.getContext().getSharedPreferences(pname, 0);
        String res = settings.getString(key, defaultValue);
		DBG("SUtils", "getPreferenceString(" + key + ", " + defaultValue + " " + res  + ", " + pname + ")");
        return res;

    }

    /**
     * To read an int from the preferences file.
     * @param key The key that identifies the preferences file.
     * @param defaultValue The default value to be returned if the preference does not exist.
     * @param pname the preference name
     * */
    public static int getPreferenceInt(String key, int defaultValue, String pname)
    {
        SharedPreferences settings = SUtils.getContext().getSharedPreferences(pname, 0);
        int res = settings.getInt(key, defaultValue);
		DBG("SUtils", "getPreferenceInt(" + key + ", " + defaultValue + " " + res  + ", " + pname + ")");
        return res;
    }

    /**
     * To read a long from the preferences file.
     * @param key The key that identifies the preferences file.
     * @param defaultValue The default value to be returned if the preference does not exist.
     * @param pname the preference name
     * */
    public static boolean getPreferenceBoolean(String key, boolean defaultValue, String pname)
    {
        try
		{
		SharedPreferences settings = SUtils.getContext().getSharedPreferences(pname, 0);
        boolean res = settings.getBoolean(key, defaultValue);
		DBG("SUtils", "getPreferenceBoolean(" + key + ", " + defaultValue + " " + res  + ", " + pname + ")");
        return res;
		}catch(Exception ex){
		DBG("SUtils", "getPreferenceBoolean(" + key + ", " + defaultValue + " " + false  + ", " + pname + ")");
		return false;}
    }
    
    /**
     * To read a long from the preferences file.
     * @param key The key that identifies the preferences file.
     * @param defaultValue The default value to be returned if the preference does not exist.
     * @param pname the preference name
     * */
    public static long getPreferenceLong(String key, long defaultValue, String pname)
    {
        SharedPreferences settings = SUtils.getContext().getSharedPreferences(pname, 0);
        Long res = settings.getLong(key, defaultValue);
		DBG("SUtils", "getPreferenceLong(" + key + ", " + defaultValue + " " + res + ", " + pname + ")");
        return res; 
    }
	
	/**
     * To remove a preference from the preferences file.
     * @param key The key that identifies the preferences file.
     * @param pname the preference name
     * */
    public static void removePreference(String key, String pname)
    {
		DBG("SUtils", "removePreference(" + key +  ", " + pname + ")");
        SharedPreferences settings = SUtils.getContext().getSharedPreferences(pname, 0);
		SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
		editor.commit();
    }
	
	/**
     * Function to get Package value used for compile java code.
	 * Used internally by jni code to check missmatches.
     * */
	public static String getPackage()
	{
		return STR_APP_PACKAGE;
	}

	/**
     * Returns the path to location of the obb files.
     * */
#if GOOGLE_MARKET_DOWNLOAD
	public static String getOBBFolder()
	{
		return android.os.Environment.getExternalStorageDirectory() + "/Android/obb/" + STR_APP_PACKAGE;
	}
#endif

	/**
     * Returns the path to location of the app's data folder
     * */
	public static String getSDFolder()
	{
#if USE_INSTALLER 
		String result = SUtils.getPreferenceString("SDFolder", GameInstaller.mPreferencesName);
		if (result != "")
			return result;
		else
#endif
			return SD_FOLDER;
	}

	/**
     * Function that return the internal path used for save.
	 * Usually: data/data/app.package/files
     * */
	public static String getSaveFolder()
	{
		try {
			return currentContext.getFilesDir().toString();
		}catch(Exception e) {}
		return "";
	}
	
    //#if AUTO_UPDATE_HEP
    public static String getVersionInstalled(){
    	try{
    	   android.content.pm.PackageInfo tmp = currentContext.getPackageManager().getPackageInfo(STR_APP_PACKAGE, 	android.content.pm.PackageManager.GET_META_DATA);
    	   return tmp.versionName;
      }catch(Exception e){}
      return GAME_VERSION_NAME;//must be defined
    }
    //#endif
	public static void shareInfo(String p_Title, String p_Message, String p_ChooserTitle)
    {

       Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

       shareIntent.setType("text/plain");

       shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, p_Title);

       shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, p_Message);

	   DBG("SUtils", "Share intent ");
       DBG("SUtils", "Share intent " + p_Title + p_Message + p_ChooserTitle);

	   if(currentContext != null)
		currentContext.startActivity(Intent.createChooser(shareIntent, p_ChooserTitle));
		else
		 DBG("SUtils", "Error no context Call Sutils.setContext");
	   
	   DBG("SUtils", "Done Share intent");

    }
	
	public static String getGameName()
	{
		try
		{
			String package_name = getPackage();
			int index = package_name.indexOf("com.gameloft.android.");
			if (index == -1)
				return package_name;
			else
			{
				index = "com.gameloft.android.".length();
				return package_name.substring(index);
			}
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public static void init ()
	{
		#if !USE_INSTALLER
		#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
		checkNewVersionInstalled();
		//getInjectedIGP();//initialize embed info for Google
		//getInjectedSerialKey();
		#endif
		#endif

	}
	
	public static long getFreeSpace(String sd_folder)
	{
		try
		{
			File path;
			if (sd_folder.contains(STR_APP_PACKAGE))
				path = new File(sd_folder.substring(0, sd_folder.indexOf(STR_APP_PACKAGE)));
			else if (sd_folder == SD_FOLDER)
				path = new File("/sdcard/");
			else
				path = new File(sd_folder);
				
			if (!path.exists())
				path.mkdirs();
				
			StatFs stat = new StatFs(path.getAbsolutePath());
			long bytesAvailable;
			
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
				bytesAvailable = stat.getAvailableBytes();
			} else { 
				// These are deprecated in API Level 18.
				// Replaced by getAvailableBlocksLong, getBlockSizeLong.
				bytesAvailable = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
			}
			
			return bytesAvailable;
		}
		catch (Exception e)
		{
			return 0;
		}
	}
#if USE_HEP_EXT_IGPINFO
	public static String[] getIGPInfo()
	{
		String temp = ReadFile(R.raw.igp_info);
		String arrInfo[] =  temp.split("\n");

	#if USE_IGP_CODE_FROM_FILE
		arrInfo[0] = Device.getDemoCode();// IGP Code
	#else
		arrInfo[0] = arrInfo[0].substring(arrInfo[0].indexOf(':') + 1).trim();// IGP Code
	#endif
		arrInfo[1] = arrInfo[1].substring(arrInfo[1].indexOf(':') + 1).trim();// IGP Portal
		arrInfo[2] = arrInfo[2].substring(arrInfo[2].indexOf(':') + 1).trim();// Game code
		return arrInfo;
	}
#endif

	public static int hasConnectivity()
    {
		if(currentContext == null)
		{
			return 0;
		}
        ConnectivityManager mConnectivityManager     = (ConnectivityManager) currentContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(mConnectivityManager == null)
            return 0;
       
        NetworkInfo mNetInfo = mConnectivityManager.getActiveNetworkInfo();
            if(mNetInfo == null)
                return 0;
        return mNetInfo.isConnected()?1:0;
    }

	public static int initCheckConnectionType()
	{
		if(currentContext == null)
		{
			return NO_CONNECTIVITY;
		}
		NetworkStateReceiver.androidOSInitialized = true;
		return CheckConnectionType();
	}
	
	//check fot the current connection type and return it as an int
	public static int CheckConnectionType()
    {
		if(currentContext == null || hasConnectivity() == 0)
		{
			return NO_CONNECTIVITY;
		}
		int connectionType = CONNECTIVITY_UNKNOWN;
        
		ConnectivityManager mConnectivityManager = (ConnectivityManager) currentContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetInfo = mConnectivityManager.getActiveNetworkInfo();
		int networkType = mNetInfo.getType();
		
		switch (networkType) 
		{
			case ConnectivityManager.TYPE_BLUETOOTH:
			{
				connectionType = CONNECTIVITY_BLUETOOTH;
				break;
			}
			case ConnectivityManager.TYPE_DUMMY:
			{
				connectionType = CONNECTIVITY_DUMMY;
				break;
			}
			case ConnectivityManager.TYPE_ETHERNET:
			{
				connectionType = CONNECTIVITY_ETHERNET;
				break;
			}
			case ConnectivityManager.TYPE_WIFI:
			{
				connectionType = CONNECTIVITY_WIFI;
				break;
			}
			case ConnectivityManager.TYPE_WIMAX:
			{
				connectionType = CONNECTIVITY_WIMAX;
				break;
			}
		}
		
		if( connectionType == CONNECTIVITY_UNKNOWN )
		{
			TelephonyManager mTelephonyManager = (TelephonyManager) currentContext.getSystemService(Context.TELEPHONY_SERVICE);
			int mobileNetworkType = mTelephonyManager.getNetworkType();
			
			switch (mobileNetworkType) 
			{
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN:
				{
					connectionType = CONNECTIVITY_2G;
					break;
				}
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
				case TelephonyManager.NETWORK_TYPE_EHRPD:
				case TelephonyManager.NETWORK_TYPE_HSPAP:
				{
					connectionType = CONNECTIVITY_3G;
					break;
				}
				case TelephonyManager.NETWORK_TYPE_LTE:
				{
					connectionType = CONNECTIVITY_4G;
					break;
				}
			}
		}
        if(connectionType == CONNECTIVITY_DUMMY)
		{
			connectionType = CONNECTIVITY_UNKNOWN;//use connection Unknown for Dummy.
		}
        return connectionType;
    }
	
	private static String injectedSerialKey = null;
	public static String getInjectedSerialKey()
    {
		#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3		
    	if(injectedSerialKey == null)
		{
			try
			{
				String localserialkey = getPreferenceString("injectedSerialKey", "", "injectedIGP"+getGameName());
				if(TextUtils.isEmpty(localserialkey))
				{
					if(canCheckInjectedSerialKey())
					{
						localserialkey = ReadFile(R.raw.serialkey);
						if(	TextUtils.isEmpty(localserialkey) )
						{
							if((SUtils.getContext().getApplicationInfo().flags & (android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0)
							{
								DBG("SUtils", "SerialKey not found on local APK, read the file from original APK");
								DBG("SUtils", "Checking on system/app");
								File appsDir = new File("/system/app");
								String[] files = appsDir.list();
								DBG("SUtils", "app folder size "+files.length);
								for (int i = 0 ; i < files.length ; i++ )
								{
									if(files[i].endsWith(".apk"))
									{
										DBG("SUtils", "Search injectedSerialKey in "+"/system/app/"+files[i]);
										String apkPath = "/system/app/"+files[i];
										PackageManager pm = currentContext.getPackageManager();
										android.content.pm.PackageInfo info = pm.getPackageArchiveInfo(apkPath,0);
										if(info.packageName.equals(getPackage()))
										{
											dalvik.system.PathClassLoader loader = new dalvik.system.PathClassLoader(apkPath, null,dalvik.system.PathClassLoader.getSystemClassLoader());
											InputStream f = loader.getResourceAsStream("res/raw/serialkey.txt");
											if(f != null)
											{
												localserialkey = inputStreamToString(f,"UTF-8");
												if(!TextUtils.isEmpty(localserialkey))
												{
													setPreference("injectedSerialKey", localserialkey, "injectedIGP"+getGameName());
													DBG("SUtils", "SerialKey saved into sharedPrefs");
												}
											}
											break;
										}
									}
								}

								//not found on system/app. We will check on system/priv-app for newest firmwares.
								//if the device doesn't contains priv-app, files.length will be 0
								if(	TextUtils.isEmpty(localserialkey) )
								{
									DBG("SUtils", "Checking on system/priv-app");
									appsDir = new File("/system/priv-app");
									files = appsDir.list();
									DBG("SUtils", "priv-app folder size "+files.length);
									for (int i = 0 ; i < files.length ; i++ )
									{
										if(files[i].endsWith(".apk"))
										{
											DBG("SUtils", "Search injectedSerialKey in "+"/system/priv-app/"+files[i]);
											String apkPath = "/system/priv-app/"+files[i];
											PackageManager pm = currentContext.getPackageManager();
											android.content.pm.PackageInfo info = pm.getPackageArchiveInfo(apkPath,0);
											if(info.packageName.equals(getPackage()))
											{
												dalvik.system.PathClassLoader loader = new dalvik.system.PathClassLoader(apkPath, null,dalvik.system.PathClassLoader.getSystemClassLoader());
												InputStream f = loader.getResourceAsStream("res/raw/serialkey.txt");
												if(f != null)
												{
													localserialkey = inputStreamToString(f,"UTF-8");
													if(!TextUtils.isEmpty(localserialkey))
													{
														setPreference("injectedSerialKey", localserialkey, "injectedIGP"+getGameName());
														DBG("SUtils", "SerialKey saved into sharedPrefs");
													}
												}
												break;
											}
										}
									}
								}
							}
							else
							{
								DBG("SUtils", "Not an UPDATED SYSTEM APP, avoid searching into system/app folder for serialKey");
							}
						}
						else
						{
							setPreference("injectedSerialKey", localserialkey, "injectedIGP"+getGameName());
							DBG("SUtils", "SerialKey google saved into sharedPrefs");
						}
						increaseRetryCountSerialKey();
					}
				}
				injectedSerialKey = localserialkey;
				
			}catch(Exception ex){
				injectedSerialKey = "";
				increaseRetryCountSerialKey();
			}
		}
		if(	TextUtils.isEmpty(injectedSerialKey) )
			injectedSerialKey = "";
    	return injectedSerialKey;
		#else
			#if GAMELOFT_SHOP
			String localserialkey = ReadFile(R.raw.serialkey);
			if(	!TextUtils.isEmpty(localserialkey))
			{
				return localserialkey;
			}
			#endif
			return "";
		#endif
    }
	
	private static String injectedIGP = null;
	public static String getInjectedIGP()
	{
		#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
		if(currentContext == null)
		{
			DBG("SUtils", "Context null, return empty string in getInjectedIGP");
			return "";
		}
		if (injectedIGP == null)
		{
			String tmpEmbed = retrieveSavedInjectedIGP();
			if (!TextUtils.isEmpty(tmpEmbed))
			{
				try{
					injectedIGP = DefReader.getInstance().getPlainDef(tmpEmbed, GGC_GAME_CODE);
				}catch(Exception e){
					injectedIGP = "";
				}
			}
		}
		if (TextUtils.isEmpty(injectedIGP))
			injectedIGP = "";
		return injectedIGP;
		#else
		return "";
		#endif
	}
	
	#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
	public static boolean canCheckInjectedIGP()
	{
		int retryNum = getPreferenceInt("retryNumIGP", 0, "injectedIGP"+getGameName());
		if(retryNum <= 1)
		{
			return true;
		}
		return false;
	}
	
	public static boolean canCheckInjectedSerialKey()
	{
		int retryNum = getPreferenceInt("retryNumSerialKey", 0, "injectedIGP"+getGameName());
		if(retryNum <= 1)
		{
			return true;
		}
		return false;
	}
	
	public static void increaseRetryCountIGP()
	{
		int retryNum = getPreferenceInt("retryNumIGP", 0, "injectedIGP"+getGameName()) + 1;
		if(retryNum < 3)//if something happens, don't increase more than 3 times
			setPreference("retryNumIGP", retryNum, "injectedIGP"+getGameName());
	}
	
	public static void increaseRetryCountSerialKey()
	{
		int retryNum = getPreferenceInt("retryNumSerialKey", 0, "injectedIGP"+getGameName()) + 1;
		if(retryNum < 3)//if something happens, don't increase more than 3 times
			setPreference("retryNumSerialKey", retryNum, "injectedIGP"+getGameName());
	}
	
	private static String retrieveSavedInjectedIGP()
	{
		String localInjectedIGP = "";
		try{
			localInjectedIGP = getPreferenceString("injectedIGP", "", "injectedIGP"+getGameName());
			if(TextUtils.isEmpty(localInjectedIGP))
			{
				if(canCheckInjectedIGP())
				{
					DBG("SUtils", "Read the file from own APK first");
					localInjectedIGP = ReadFile(R.raw.crc);
					if(!TextUtils.isEmpty(localInjectedIGP))
					{
						setPreference("injectedIGP", localInjectedIGP, "injectedIGP"+getGameName());
						DBG("SUtils", "Injected IGP saved into sharedPrefs");
					}
					else
					{
						if((SUtils.getContext().getApplicationInfo().flags & (android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0)
						{
							DBG("SUtils", "IGP not found on local APK, read the file from original APK");
							DBG("SUtils", "Checking on system/app");
							File appsDir = new File("/system/app");
							String[] files = appsDir.list();
							DBG("SUtils", "app folder size "+files.length);
							for (int i = 0 ; i < files.length ; i++ )
							{
								if(files[i].endsWith(".apk"))
								{
									DBG("SUtils", "Search injectedIGP in "+"/system/app/"+files[i]);
									String apkPath = "/system/app/"+files[i];
									PackageManager pm = currentContext.getPackageManager();
									android.content.pm.PackageInfo info = pm.getPackageArchiveInfo(apkPath,0);
									if(info.packageName.equals(getPackage()))
									{
										dalvik.system.PathClassLoader loader = new dalvik.system.PathClassLoader(apkPath, null,dalvik.system.PathClassLoader.getSystemClassLoader());
										InputStream f = loader.getResourceAsStream("res/raw/crc.bin");
										if(f != null)
										{
											localInjectedIGP = inputStreamToString(f,"UTF-8");
											if(!TextUtils.isEmpty(localInjectedIGP))
											{
												setPreference("injectedIGP", localInjectedIGP, "injectedIGP"+getGameName());
												DBG("SUtils", "Injected IGP saved into sharedPrefs");
											}
										}
										break;
									}
								}
							}

							//not found on system/app. We will check on system/priv-app for newest firmwares.
							//if the device doesn't contains priv-app, files.length will be 0
							if(	TextUtils.isEmpty(localInjectedIGP) )
							{
								DBG("SUtils", "Checking on system/priv-app");
								appsDir = new File("/system/priv-app");
								files = appsDir.list();
								DBG("SUtils", "priv-app folder size "+files.length);
								for (int i = 0 ; i < files.length ; i++ )
								{
									if(files[i].endsWith(".apk"))
									{
										DBG("SUtils", "Search injectedIGP in "+"/system/priv-app/"+files[i]);
										String apkPath = "/system/priv-app/"+files[i];
										PackageManager pm = currentContext.getPackageManager();
										android.content.pm.PackageInfo info = pm.getPackageArchiveInfo(apkPath,0);
										if(info.packageName.equals(getPackage()))
										{
											dalvik.system.PathClassLoader loader = new dalvik.system.PathClassLoader(apkPath, null,dalvik.system.PathClassLoader.getSystemClassLoader());
											InputStream f = loader.getResourceAsStream("res/raw/crc.bin");
											if(f != null)
											{
												localInjectedIGP = inputStreamToString(f,"UTF-8");
												if(!TextUtils.isEmpty(localInjectedIGP))
												{
													setPreference("injectedIGP", localInjectedIGP, "injectedIGP"+getGameName());
													DBG("SUtils", "Injected IGP saved into sharedPrefs");
												}
											}
											break;
										}
									}
								}
							}
						}
						else
						{
							DBG("SUtils", "Not an UPDATED SYSTEM APP, avoid searching into system/app folder for Injected IGP");
						}
					}
					if(TextUtils.isEmpty(localInjectedIGP))
						localInjectedIGP = "";
					increaseRetryCountIGP();
				}
			}
			return localInjectedIGP;
		}catch(Exception e){
			increaseRetryCountIGP();
			return "";
		}
	}
	private static String inputStreamToString(InputStream inputStream, String encoding)
        throws IOException {
    return new String(inputStreamToString(inputStream), encoding);
	}

	private static byte[] inputStreamToString(InputStream inputStream) throws IOException {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}
			return baos.toByteArray();
	}
	
	public static void checkNewVersionInstalled()
	{
		String actualVersion = getPreferenceString("versionInstalled", "", "injectedIGP"+getGameName());
		if(TextUtils.isEmpty(actualVersion))
		{
			setPreference("versionInstalled", GAME_VERSION_CODE, "injectedIGP"+getGameName());
		}
		else
		{
			if(!actualVersion.equals(GAME_VERSION_CODE))
			{
				setPreference("retryNumIGP", 0, "injectedIGP"+getGameName());
				setPreference("retryNumSerialKey", 0, "injectedIGP"+getGameName());
				setPreference("versionInstalled", GAME_VERSION_CODE, "injectedIGP"+getGameName());	
			}
		}
	}
	
	#endif
	
    // lets you enable/disable skip for InGameVideo
    public static void inGameVideoSetSkipEnabled(boolean skipEnabled)
    {
    	#if USE_IN_GAME_VIDEO
    	DialogVideo.setSkipEnabled(skipEnabled);
    	#endif
    }

 	/*
    	* Play InGameVideo
    */
    public static boolean playVideo(String videoName, boolean skipEnabled)
    {

    	#if USE_IN_GAME_VIDEO
    	final String subtitlesFile = videoName; //ignored if you are playing from SDCard
    	final boolean canSkip = skipEnabled;
    #if IN_GAME_VIDEO_READ_FROM_APK
    	java.lang.reflect.Field field = null;
    	int fValue = 0;
    	try{
	    	DBG("SUtils","VIDEO - will attempt to access raw value:" + videoName.substring(0,videoName.length()-4));
	    	Class myClass = Class.forName(getPackage() + ".R$raw");
			field = myClass.getDeclaredField(videoName.substring(0,videoName.length()-4));
			fValue = ((Integer)field.get(null)).intValue();

		} catch (NoSuchFieldException e)
		{
			ERR("SUtils","The video you are attempting to play does not exists in your APK");
			return false;
		} catch (Exception e)
		{
			ERR("SUtils","Error attempting to play video from APK");
			DBG_EXCEPTION(e);
			return false;
		}

		final String mVideoPath = "android.resource://"+getPackage()+ "/" + fValue ;

    #else
    	final String mVideoPath = getSDFolder() + "/" + videoName;

    	//check if video exists
    	File file = new File(mVideoPath);
		if(!file.exists())
		{	
			ERR("SUtils", "You are attempting to play an inexistent video!");
			return false;
		}

    #endif
    	DBG("SUtils","full videoPath is:"+ mVideoPath);
    	DBG("SUtils", "Is SKIP enabled:" + canSkip);


    	if(getContext() == null)
    		WARN("SUtils","getContext() returns null");


    	
    	Thread t = new Thread(new Runnable()
		{
    		public void run()
    		{
    			DialogVideo.playVideo(getContext(), mVideoPath, subtitlesFile, canSkip);
    		}
    	}
    	);

    	runOnUiThread(t);
    	
    	return true;
    	#else
    	return false;
    	#endif


    }


    public static void stopVideo()
    {
    	#if USE_IN_GAME_VIDEO
    	DialogVideo.stopVideo();
    	#endif
    }

	/*
    	* Pause InGameVideo
    */
    public static void inGameVideoOnPause()
    {
    	#if USE_IN_GAME_VIDEO
    	DialogVideo.onPause();
    	#endif
    }

	/*
    	* Resume previously paused InGameVideo
    */
    public static void inGameVideoOnResume()
    {
    	#if USE_IN_GAME_VIDEO
    	DialogVideo.resumePlayback();
    	#endif
	} 

	public static native void onVideoFinished();



	
	
	private final static int GU_PREF_TYPE_INT 		= 0;
	private final static int GU_PREF_TYPE_LONG 	= 1;
	private final static int GU_PREF_TYPE_BOOLEAN 	= 2;
	private final static int GU_PREF_TYPE_STRING 	= 3;

	private final static String GU_PREF_DATA	= "npData";
	private final static String GU_NP_DATA_TYPE	= "npDataType";
	private final static String GU_NP_DEF_VALUE	= "npDefaultValue";
	private final static String GU_NP_DEF_RESULT= "npResult";


    /**
     * Set a Shared Preference from the application's native code
     * @param key the key in a String representation
     * @param value the value to store in an Object instance
     * @param pname the preference name
     */
	public static void nativeSetPreference(Bundle bundle)
	{
		int dataType = 	bundle.getInt(GU_NP_DATA_TYPE);
		String key 	=	bundle.getString("npKey");
		String pname =	bundle.getString("npPrefName");

		Object dataObj = null;
		
		switch(dataType)
		{
			case GU_PREF_TYPE_INT:
			dataObj = new Integer(bundle.getInt(GU_PREF_DATA));
			break;
			
			case GU_PREF_TYPE_LONG:
			dataObj = new Long(bundle.getLong(GU_PREF_DATA));
			break;
			
			case GU_PREF_TYPE_BOOLEAN:
			dataObj = new Boolean(bundle.getBoolean(GU_PREF_DATA));
			break;
			
			case GU_PREF_TYPE_STRING:
			dataObj = new String(bundle.getString(GU_PREF_DATA));
			break;


		}
	
		setPreference(key, dataObj, pname);
	}

	//no javadoc comment
	//this function retrieves a text file from the asset folder
	public static byte[] getAssetAsString(String path)
    {
        try 
		{
            if(path.startsWith("."))
			{
                path = path.substring(1);
			}

            DataInputStream is = new DataInputStream(((Activity)getContext()).getAssets().open(path, AssetManager.ACCESS_STREAMING));
            
            if(is != null) 
			{
                byte[] buffer = new byte[is.available()];
                is.readFully(buffer);
                is.close();
                return buffer;
            }
        }
		catch(IOException e) 
		{
            e.printStackTrace();
        }
        return null;
    }

	
	/**
	 * setPreference a Shared Preference from the application's native code.
	 * @param bundle object containing type of data and where we store the result;
	 * @param 
	*/

	public static Bundle nativeGetPreference(Bundle bundle)
	{

		final int op 		= bundle.getInt(GU_NP_DATA_TYPE);
		
		String key =	bundle.getString("npKey");
		String pname =	bundle.getString("npPrefName");

		switch(op)
		{
			case GU_PREF_TYPE_INT:
			{
				bundle.putInt( GU_NP_DEF_RESULT, getPreferenceInt(key, bundle.getInt(GU_NP_DEF_VALUE), pname) );
			}
			break;

			case GU_PREF_TYPE_LONG:
			{
				bundle.putLong( GU_NP_DEF_RESULT, getPreferenceLong(key, bundle.getLong(GU_NP_DEF_VALUE), pname) );
			}
			break;

			case GU_PREF_TYPE_BOOLEAN:
			{
				bundle.putBoolean( GU_NP_DEF_RESULT, getPreferenceBoolean(key, bundle.getBoolean(GU_NP_DEF_VALUE), pname) );
			}
			break;

			case GU_PREF_TYPE_STRING:
			{
				bundle.putString( GU_NP_DEF_RESULT, getPreferenceString(key, bundle.getString(GU_NP_DEF_VALUE), pname) );
			}
			break;
		}


		return bundle;
	}
	
	       
    /**
	 * genericUnzip the zip file(as in path) to the destination directory supposing the destination 
     * directory already exists, you need to create the destination directory before calling this method.
	 * @param path String specifies the full path to the zip file;
	 * @param destination String specifies the destination directory where to unzip the files.
     * @return true if unzip successful.
     */
    
    public static boolean genericUnzipArchive(String path, String destination) {       
		 InputStream is;
		 ZipInputStream zis;
		 try {
             String filename;
             is = new FileInputStream(path);
             zis = new ZipInputStream(new BufferedInputStream(is));          
             ZipEntry ze;
             byte[] buffer = new byte[4096];
             int count;

             while ((ze = zis.getNextEntry()) != null) {
                 filename = ze.getName();

                 // Need to create directories if not exists, or
                 // it will generate an Exception...
                 if (ze.isDirectory()) {
                    File fmd = new File(destination + filename);
                    fmd.mkdirs();
                    continue;
                 }

                 FileOutputStream fout = new FileOutputStream(destination + filename);

                 while ((count = zis.read(buffer)) != -1) {
                     fout.write(buffer, 0, count);             
                 }

                 fout.close();               
                 zis.closeEntry();
             }

             zis.close();
         } 
         catch(IOException e) {
            return false;
         }
		return true;
	}
    
    /**
	 * delete the file specified by path
	 * @param path String specifies the full path to the file need to be deleted;
     */
    
    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
    
    /**
	 * Remove the specified directory recursively.
     * This method takes String as input; and has C++ interface as well.
     * Do not use this function for large folders.
	 * @param path specifies the full path to the directory need to be removed.
     */
    
    public static boolean removeDirectoryRecursively(String path) {
        File dir = new File(path);
        return removeFileDirectoryRecursively(dir);
    }
    
    /**
	 * Remove the specified file directory recursively.
     * This method takes File as input.
     * Do not use this function for large folders.
	 * @param dir specifies the full path to the directory need to be removed.
     */
    
	public static boolean removeFileDirectoryRecursively(File dir) {
	    if (dir.exists()) {
	        File[] files = dir.listFiles();
	        
	        if (null == files) {
	            return true;
	        }
	        
	        for (int i = 0; i < files.length; ++i) {
	            if (files[i].isDirectory()) {
	                removeFileDirectoryRecursively(files[i]);
	            } else {
	                files[i].delete();
	            }
	        }
	    }
	    return dir.delete();
	}

	private static Toast cantGoBackToast = null;
	public static void showCantGoBackPopup(final int duration)
	{
		if(SUtils.getContext() == null)
			return;
		new Thread(new Runnable() { public void run() 
		{	
			runOnUiThread(new Runnable() { public void run()
			{
				try{
					if(cantGoBackToast == null)
					{
						cantGoBackToast = Toast.makeText(SUtils.getContext(), R.string.CAN_GO_BACK, duration);
					}
					cantGoBackToast.setText(R.string.CAN_GO_BACK);
					cantGoBackToast.setDuration(duration);
					android.view.View toastView = cantGoBackToast.getView();
					if(toastView != null)
					{
						if(toastView.isShown())
						{
							return;
						}
					}
					cantGoBackToast.show();
				}catch(Exception e){};
			}});
		}}).start();
	}

	// Gets signature
	private static int[] barrel = new int [4];
	public static int[] retrieveBarrels () {
		// Reset'em
		barrel[0] = barrel[1] = barrel[2] = barrel[3] = 0;
		// !!
		try {
			// Check
			if (SUtils.getContext() != null) {
				android.content.pm.Signature[] raw = SUtils.getContext().getPackageManager ().getPackageInfo(
						SUtils.getContext().getPackageName(), 
						android.content.pm.PackageManager.GET_SIGNATURES
					).signatures;
				int n = 4;
				// Check
				if (raw.length < 4) {
					n = raw.length;
				}
				// For
				for (int i = 0; i < n; ++i) {
					barrel[i] = raw[i].hashCode ();
				}
				// Return
				return barrel;
			}
		} catch (Exception e) {
			// Nothing
		}
		// Return
		return barrel;
	}
	public static int[] getGLUID(String gldId)
	{
		int ints[] = new int[4];
		try
		{
			String packageName = (SUtils.class.getPackage().getName()).replace(".GLUtils","");
			String text = gldId + packageName;
			
			java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(text.getBytes());
			byte messageDigest[] = digest.digest();
			
			ints[0] = getInt(messageDigest, 0);
			ints[1] = getInt(messageDigest, 4);
			ints[2] = getInt(messageDigest, 8);
			ints[3] = getInt(messageDigest, 12);
			
			return ints;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ints;
	}
		
	public static int getInt(byte[] value, int start)
	{
		int result = 0;
		
		result = ((result << 8) + (value[start] & 0x00FF));
		result = ((result << 8) + (value[start+1] & 0x00FF));
		result = ((result << 8) + (value[start+2] & 0x00FF));
		result = ((result << 8) + (value[start+3] & 0x00FF));
		
		return result;
	}
	
	public static String getMetaDataValue(String name)
	{
		try
		{
			android.content.pm.ApplicationInfo appInfo = SUtils.getContext().getPackageManager().getApplicationInfo(SUtils.getContext().getPackageName(),PackageManager.GET_META_DATA);
			Bundle bundle = appInfo.metaData;
			String metaValue = bundle.get(name).toString();
			if( TextUtils.isEmpty(metaValue))
				return "";
			else
				return metaValue;
		}catch(Exception e){}
		return "";
	}
}
