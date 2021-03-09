package APP_PACKAGE.installer;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Integer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.vending.licensing.*;


public class JOnlyDRMPolicy implements Policy 
{
    private PreferenceObfuscator mPreferences;
    static JOnlyDRMPolicy mInstance;

	private static 	final 	String 	DEFAULT_VALIDITY_TIMESTAMP 	= "0";
    private static 	final 	String 	DEFAULT_RETRY_UNTIL 		= "0";
    private static 	final 	String 	DEFAULT_MAX_RETRIES 		= "0";
    private static 	final 	String 	DEFAULT_RETRY_COUNT 		= "0";
	private static 	final 	String 	DEFAULT_RUN_FIRST 			= "true";
	private static	final	long 	VT_EXTRA 					= 60*60*24*7*2; //2 weeks ... divide by 1000 in c code
	private static	final	long 	GT_EXTRA 					= 60*60*24*7*2 + 3*24*60*60; //2 weeks+ 3 days ... again divide by 1000
	private static	final	long 	GR_EXTRA					= 10;
	public 	static			boolean	TRY_AGAIN					= false;
	private static 			long 	lastResponse;
	private static 			long 	validityTimestamp;
	private static 			long 	retryUntil;
	private static 			long 	maxRetries;
	private static 			long 	retryCount;
	private static 			long 	RunFirst;
	private static 			long 	gdrm_r_time;
	private static 			long 	gdrm_l_time;

	private static			int		licenseOk;
	private static			int		licenseNOk;
	private static			int		licenseRetry;

    private int ConvertAnswer(int l)//(LicenseResponse l) was enum LicenseResponse, now using Policy responses
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
	
//DeConvert Method
//Parameters:
//		answer. primitive int, here the hashcode of the value of LicenseResponse is received, now this answer is instead the hashcode of the String of Policy.[Value]
//Process:
		//here we take the hashcode of one of the possible values of the license status, and convert this value to the real value to return it.
    private static Integer DeConvert(int answer)
  {
    	//vulnerable point ! should be done in some other way !
    	int rez2=String.valueOf(Policy.RETRY).hashCode()-answer; //0x0123 (RETRY) - 0x0231    = -270 if answer is NOT_LICENSED
    	int rez= String.valueOf(Policy.NOT_LICENSED).hashCode()-answer; //0x0231 (NOT_LICENSED) - 0x231 = 0
    	int rez1=String.valueOf(Policy.LICENSED).hashCode()-answer; //0x0100 (LICENSED) - 0x231 = -305
    	
    	if (rez1*rez!=0) return Policy.RETRY; //rez2==0
    	else if (rez*rez2!=0) return Policy.LICENSED; //rez1==0
    	else if (rez1*rez2!=0) return Policy.NOT_LICENSED; //rez==0

    	return Integer.valueOf(Policy.NOT_LICENSED);
    }
	
    static void init()
    {
    	licenseOk 			= String.valueOf(Policy.LICENSED).hashCode();
    	licenseNOk 		= String.valueOf(Policy.NOT_LICENSED).hashCode();
    	licenseRetry 		= String.valueOf(Policy.RETRY).hashCode();
		
		DBG("DRM","JOnlyDRMPolicy init() licenseOk: " + licenseOk);      
		DBG("DRM","JOnlyDRMPolicy init() licenseNOk: " + licenseNOk);      
		DBG("DRM","JOnlyDRMPolicy init() licenseRetry: " + licenseRetry);
    }
	
    public JOnlyDRMPolicy(Context context, Obfuscator obfuscator) 
	{
		DBG("DRM","JOnlyDRMPolicy JOnlyDRMPolicy");      
    	
		init();
		
        SharedPreferences sp = context.getSharedPreferences("JOnlyDRMPolicy", Context.MODE_PRIVATE);
        mPreferences = new PreferenceObfuscator(sp, obfuscator);
        lastResponse = String.valueOf(mPreferences.getString("lastResponse", Integer.valueOf(Policy.RETRY).toString())).hashCode();
		DBG("DRM","JOnlyDRMPolicy lastResponse: " + lastResponse);

        validityTimestamp = Long.parseLong(mPreferences.getString("validityTimestamp", DEFAULT_VALIDITY_TIMESTAMP));
		retryUntil = Long.parseLong(mPreferences.getString("retryUntil", DEFAULT_RETRY_UNTIL));
		maxRetries = Long.parseLong(mPreferences.getString("maxRetries", DEFAULT_MAX_RETRIES));
		retryCount = Long.parseLong(mPreferences.getString("retryCount", DEFAULT_RETRY_COUNT));
		String s = mPreferences.getString("RunFirst", DEFAULT_RUN_FIRST);
		RunFirst = (s == "false" ? 0 : 1);
		gdrm_r_time = Long.parseLong(mPreferences.getString("gdrm_r_time", "0"));//real time
		gdrm_l_time = Long.parseLong(mPreferences.getString("gdrm_l_time", "0"));//last time
		
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
		DBG("DRM","JOnlyDRMPolicy processServerResponse() response = " + ConvertAnswer(response));
		DBG("DRM","JOnlyDRMPolicy processServerResponse() response = " + response);
    	// only the one with the first parameter equal to the 2nd one will get processed
    	// this is the most vulnerable point of attack , that's why we're calling 3 times the same function;
    	
    	setTime(System.currentTimeMillis()/1000);

    	processServer(0,String.valueOf(response).hashCode());
    	processServer(1,String.valueOf(response).hashCode());
    	processServer(2,String.valueOf(response).hashCode());
    }
    
    private void setTime(long time)
    {
    	if (gdrm_r_time == 0)
    	{
    		DBG("DRM","JOnlyDRMPolicy first time update!");
    		gdrm_r_time = time;
    		gdrm_l_time = time;
    	}
    	if (time <= gdrm_l_time)
    	{
    		DBG("DRM","JOnlyDRMPolicy real time not incremented");
    		gdrm_l_time = time;
    	}
    	else
    	{
    		DBG("DRM","JOnlyDRMPolicy real time incremented");
    		gdrm_r_time += time - gdrm_l_time;
    		gdrm_l_time  = time;
    	}
    	UpdatePreferences("gdrm_r_time", gdrm_r_time, 4);
    	UpdatePreferences("gdrm_l_time", gdrm_l_time, 8);
    }
    
    private void processServer(int attempt, int resp)
    {
    	DBG("DRM","JOnlyDRMPolicy  processing server");
    	if (attempt == 0)
    		attempt  = licenseNOk;
    	if (attempt == 1)
    		attempt  = licenseOk;
    	if (attempt == 2)
    		attempt  = licenseRetry;
    	if (attempt == resp)
    	{
    		DBG("DRM","JOnlyDRMPolicy  found response");

    		if (resp == licenseNOk)
    		{		// not licensed
    			DBG("DRM","JOnlyDRMPolicy  response is fail");
    			lastResponse		= licenseNOk;
    			validityTimestamp	= 0;
    			retryUntil			= 0;
    			maxRetries			= 0;
    			retryCount			= 0;
    			updatePrefs();
    		}
    		else if (resp == licenseOk)
    		{

    			DBG("DRM","JOnlyDRMPolicy  response is good");
    			lastResponse 		= licenseOk;
    			validityTimestamp	= gdrm_r_time + VT_EXTRA;
    			retryUntil			= gdrm_r_time + GT_EXTRA;
    			maxRetries			= GR_EXTRA;
    			retryCount			= 0;
    			updatePrefs();
    		}
    		else if (resp == licenseRetry)
    		{

    			DBG("DRM","JOnlyDRMPolicy  response is retry");
    			lastResponse		= licenseRetry;
    			UpdatePreferences("lastResponse", lastResponse, 1);
    		}
    	}
    }
    
    private static void updatePrefs()
    {
    	UpdatePreferences("lastResponse", 		lastResponse, 		1);
    	UpdatePreferences("validityTimestamp", 	validityTimestamp, 	8);
    	UpdatePreferences("retryUntil", 		retryUntil, 		4);
    	UpdatePreferences("maxRetries", 		maxRetries, 		7);
    	UpdatePreferences("retryCount", 		retryCount, 		9);
    	UpdatePreferences("RunFirst", 			RunFirst, 			6);
    	UpdatePreferences("gdrm_r_time", 		gdrm_r_time, 		0);
    	UpdatePreferences("gdrm_l_time", 		gdrm_l_time, 		4);
    }

    public static void UpdatePreferences(String key,long value,int idx)
    {
    	DBG("DRM","JOnlyDRMPolicy UpdatePreferences \t\t\t " + key + value);
    	if (idx==1)//hardcode from native ! means it's license response;
    	{
    		mInstance.mPreferences.putString(key, DeConvert((int)value).toString()); // here, we take the real value of one of the possible value status of the License using DeConvert, and then obtain the String of such value to save in the preferences
	    	mInstance.mPreferences.commit();
    	}
    	else if(idx==6) // hardcode from native ! means if it's first run !
    	{
    		mInstance.mPreferences.putString(key, (value == 0 ? "false" : "true"));    	
	    	mInstance.mPreferences.commit();
    	}
    	else
    	{
	    	mInstance.mPreferences.putString(key, Long.valueOf(value).toString());    	
	    	mInstance.mPreferences.commit();
    	}
    }
  
    public boolean allowAccess() 
	{
    	DBG("DRM","JOnlyDRMPolicy allowAccess()");
    	TRY_AGAIN = true;
    	setTime(System.currentTimeMillis() / 1000);
    	if (lastResponse == licenseOk && gdrm_r_time <= validityTimestamp) //valid
    	{
    		DBG("DRM","JOnlyDRMPolicy valid policy , performing check");
    		RunFirst = 0;
    		UpdatePreferences("RunFirst", RunFirst, 6);
    		return true;
    	}
    	else if ((lastResponse == licenseRetry || lastResponse == licenseOk)) //&& time < g_nTime + MILLIS_PER_MINUTE) //retry
    	{
    		DBG("DRM","JOnlyDRMPolicy  grace time period license");
    		if (gdrm_r_time <= retryUntil && retryCount < maxRetries)
    		{
    			DBG("DRM","JOnlyDRMPolicy  retry ok");
    			RunFirst=0;
    			UpdatePreferences("RunFirst", RunFirst, 6);
    			retryCount++;
    			UpdatePreferences("retryCount", retryCount, 9);
    			return true;
    		}
    	}
    	if (lastResponse == licenseRetry)
    	{
    		TRY_AGAIN = true;
    	}
    	return false;
    }
}

