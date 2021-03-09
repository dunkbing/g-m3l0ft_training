package APP_PACKAGE.installer;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.vending.licensing.*;

public class GDRMPolicy implements Policy 
{    
    private PreferenceObfuscator mPreferences;
    static GDRMPolicy mInstance;
	
	private static final String DEFAULT_VALIDITY_TIMESTAMP = "0";
    private static final String DEFAULT_RETRY_UNTIL = "0";
    private static final String DEFAULT_MAX_RETRIES = "0";
    private static final String DEFAULT_RETRY_COUNT = "0";
	private static final String DEFAULT_RUN_FIRST = "true";
	//private static final String PREF_LAST_TIME = "gdrm_l_time";
	//private static final String PREF_REAL_TIME = "gdrm_r_time";
	
    /**
     * @param context The context for the current application
     * @param obfuscator An obfuscator to be used with preferences.
     */
    private int ConvertAnswer(int l)
    {
    	//vulnerable point ! should be done in some other way !
    	if (l == Policy.RETRY) //0x0123
    		return 2;
    	if (l == Policy.LICENSED) //0x0100
    		return 1;
    	if (l == Policy.NOT_LICENSED) //0x0231
    		return 0;
    	return 0;
    }
	
    private static Integer DeConvert(int answer)
    {
    	//vulnerable point ! should be done in some other way !
    	int rez2=String.valueOf(Policy.RETRY).hashCode()-answer; //0x0123 (RETRY)
    	int rez= String.valueOf(Policy.NOT_LICENSED).hashCode()-answer; //0x0231 (NOT_LICENSED)
    	int rez1=String.valueOf(Policy.LICENSED).hashCode()-answer; //0x0100 (LICENSED)
    	
    	if (rez1*rez!=0) return Policy.RETRY; //rez2==0
    	else if (rez*rez2!=0) return Policy.LICENSED; //rez1==0
    	else if (rez1*rez2!=0) return Policy.NOT_LICENSED; //rez==0

    	return Integer.valueOf(Policy.NOT_LICENSED);
    }
	
    static void init()
    {
    	initNativeAP(String.valueOf(Policy.LICENSED).hashCode(), String.valueOf(Policy.NOT_LICENSED).hashCode(), String.valueOf(Policy.RETRY).hashCode());
    }
	
    public GDRMPolicy(Context context, Obfuscator obfuscator) 
	{
		DBG("GDRMPolicy","GDRMPolicy()");      
    	
		init();
		
        SharedPreferences sp = context.getSharedPreferences(getConstString(0), Context.MODE_PRIVATE);
        mPreferences = new PreferenceObfuscator(sp, obfuscator);
		
		/*int lastResponse = ConvertAnswer(LicenseResponse.valueOf(mPreferences.getString(getConstString(1), LicenseResponse.RETRY.toString())));
        setConst(1, lastResponse);
		DBG("GDRMPolicy","LAST_RESPONSE = " + lastResponse);
		
		int vt = (int)Long.parseLong(mPreferences.getString(getConstString(2), DEFAULT_VALIDITY_TIMESTAMP));
		setConst(2, vt);		
		DBG("GDRMPolicy","VT_STAMP = " + vt);
		
		int rt = (int)Long.parseLong(mPreferences.getString(getConstString(3), DEFAULT_RETRY_UNTIL));
        setConst(3, rt);
		DBG("GDRMPolicy","RT_UNTIL = " + rt);
		
		int rt_max = (int)Long.parseLong(mPreferences.getString(getConstString(4), DEFAULT_MAX_RETRIES));
        setConst(4, rt_max);
		DBG("GDRMPolicy","RT_MAX = " + rt_max);
		
		int rt_count = (int)Long.parseLong(mPreferences.getString(getConstString(5), DEFAULT_RETRY_COUNT));
        setConst(5, rt_count);
		DBG("GDRMPolicy","RT_COUNT = " + rt_count);
		
		String s = mPreferences.getString(getConstString(6), DEFAULT_RUN_FIRST);
		setConst(6,s=="false" ? 0 : 1);		*/
		
		// Using this method of calling so the code will be as ambigous as possible. The code does the exact same thing as the one above
		//
		DBG("GDRMPolicy","Getting Preference Key: " + getConstString(1) + " Value: " + String.valueOf(mPreferences.getString(getConstString(1), Integer.valueOf(Policy.RETRY).toString())).hashCode());
		setConst(1,String.valueOf(mPreferences.getString(getConstString(1), Integer.valueOf(Policy.RETRY).toString())).hashCode());
		
		DBG("GDRMPolicy","Getting Preference Key: " + getConstString(2) + " Value: " + mPreferences.getString(getConstString(2), DEFAULT_VALIDITY_TIMESTAMP));
		setLongConst(2,Long.parseLong(mPreferences.getString(getConstString(2),
			DEFAULT_VALIDITY_TIMESTAMP)));

		DBG("GDRMPolicy","Getting Preference Key: " + getConstString(3) + " Value: " + mPreferences.getString(getConstString(3), DEFAULT_RETRY_UNTIL));
        setLongConst(3,Long.parseLong(mPreferences.getString(getConstString(3), DEFAULT_RETRY_UNTIL)));
		
		DBG("GDRMPolicy","Getting Preference Key: " + getConstString(4) + " Value: " + mPreferences.getString(getConstString(4), DEFAULT_MAX_RETRIES));
        setLongConst(4,Long.parseLong(mPreferences.getString(getConstString(4), DEFAULT_MAX_RETRIES)));
		
		DBG("GDRMPolicy","Getting Preference Key: " + getConstString(5) + " Value: " + mPreferences.getString(getConstString(5), DEFAULT_RETRY_COUNT));
        setLongConst(5,Long.parseLong(mPreferences.getString(getConstString(5), DEFAULT_RETRY_COUNT)));
		
		String s = mPreferences.getString(getConstString(6), DEFAULT_RUN_FIRST);
		setConst(6,s=="false" ? 0 : 1);
		DBG("GDRMPolicy","Getting Preference Key: " + getConstString(6) + " Value: " + s);

		DBG("GDRMPolicy","Getting Preference Key: " + getConstString(7) + " Value: " + Long.parseLong(mPreferences.getString(getConstString(7), "0")));
		setLongConst(7,Long.parseLong(mPreferences.getString(getConstString(7), "0")));//real time
		
		DBG("GDRMPolicy","Getting Preference Key: " + getConstString(8) + " Value: " + Long.parseLong(mPreferences.getString(getConstString(8), "0")));
		setLongConst(8,Long.parseLong(mPreferences.getString(getConstString(8), "0")));//last time
					
		mInstance = this;
    }

    /**
     * Process a new response from the license server.
     * <p>
     * This data will be used for computing future policy decisions. The
     * following parameters are processed:
     * <ul>
     * <li>VT: the timestamp that the client should consider the response
     *   valid until
     * <li>GT: the timestamp that the client should ignore retry errors until
     * <li>GR: the number of retry errors that the client should ignore
     * </ul>
     *
     * @param response the result from validating the server response
     * @param rawData the raw server response data
     */
    public void processServerResponse(int response, ResponseData rawData) 
	{
		DBG("GDRMPolicy","processServerResponse() response = " + ConvertAnswer(response));
		DBG("GDRMPolicy","processServerResponse() response = " + response);
    	// only the one with the first parameter equal to the 2nd one will get processed
    	// this is the most vulnerable point of attack , that's why we're calling 3 times the same function;
    	
    	/*long curr_time = System.currentTimeMillis()/1000;
		long l_time = 0;
		long r_time = 0;
		
		String last_time = mPreferences.getString(PREF_LAST_TIME, "" + 0);
		String real_time = mPreferences.getString(PREF_REAL_TIME, "" + 0);
		try
		{
			l_time = Long.parseLong(last_time);
			r_time = Long.parseLong(real_time);
		}
		catch(Exception ex){DBG_EXCEPTION(ex);}
		
		if(r_time == 0)
		{
			r_time = curr_time;
			l_time = curr_time;
			mPreferences.putString(PREF_REAL_TIME, "" + r_time);    	
			mPreferences.commit();			
		}
		
		if(curr_time <= l_time)
		{
			l_time=curr_time;
			mPreferences.putString(PREF_LAST_TIME, "" + curr_time);    	
			mPreferences.commit();
		}
		else
		{
			r_time += (curr_time - l_time);
			l_time = curr_time;
			mPreferences.putString(PREF_REAL_TIME, "" + r_time);    	
			mPreferences.commit();
			
			mPreferences.putString(PREF_LAST_TIME, "" + l_time);    	
			mPreferences.commit();
		}
    	setTime(r_time);*/
		
    	setTime(System.currentTimeMillis()/1000);
    	processServer(0,String.valueOf(response).hashCode());
    	processServer(1,String.valueOf(response).hashCode());
    	processServer(2,String.valueOf(response).hashCode());

    }

    public static void UpdatePreferences(String key,String value,int idx)
    {
    	DBG("GDRMPolicy","UpdatePreferences(String,String,int) \t\t\t " + key + value);
    	String val=value;
    	if (idx==1)//hardcode from native ! means it's license response;
    	{
    		val = DeConvert(Integer.parseInt(value)).toString();
    	}
    	else if(idx==6) // hardcode from native ! means if it's first run !
    	{
    		val = Integer.parseInt(value)==0 ? "false" : "true";
    	}
    	mInstance.mPreferences.putString(key,val);    	
    	mInstance.mPreferences.commit();
    }
	public static void UpdatePreferences2(String key,long value, int idx)
	{
		DBG("GDRMPolicy","Committing Preference Key: " + key + " Value: " + value);
		mInstance.mPreferences.putString(key,Long.valueOf(value).toString());
		mInstance.mPreferences.commit();
	}
  
    public boolean allowAccess() 
	{
		DBG("GDRMPolicy","allowAccess()");
    	return nativeAllow(System.currentTimeMillis()/1000) == 1;     
    }

/*#if !ENABLE_DOWNLOAD_NATIVE    
	static 
	{
		System.loadLibrary(SO_LIB_FILE);
	}
#endif*/
	
    public native String getPrefFile();
    public native String getConstString(int strId);
    public native void setConst(int idx,int value);
	public native void setLongConst(int idx,long value);
    public native void processServer(int val,int trueVal);
    public native void setTime(long time);    
    public native int nativeAllow(long time);    
    public static native void initNativeAP(int valid, int invalid, int retry);    
}

