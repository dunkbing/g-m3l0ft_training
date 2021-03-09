#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.os.Bundle;
import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.text.TextUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import java.lang.String;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import APP_PACKAGE.R;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.Encrypter;

public class IABRequestHandler
{
//////////////////////////////////////////////////////////////
// HTTP Values & Response codes
//////////////////////////////////////////////////////////////
	public static final int HTTP_TIMEOUT 			= 5000;
	public static final int INVALID_RESULT 			= -1;
	public static final int SUCCESS_RESULT 			= 1;
	public static final int FAIL_RESULT 			= 2;
	
	SET_TAG("InAppBilling");
	
	private static IABRequestHandler mThis = null;
	
// Async Tasks bundle params
	final static String KEY_URL		 		= "a1";
	final static String KEY_QUERY			= "a2";
	final static String KEY_HTTP_METHOD_USED 	= "a3";
	final static String KEY_RESPONSE		= "a4";
	final static String KEY_REQUEST_RESULT	= "a5";
	final static String KEY_HTTP_RESPONSE	= "a6";
	final static String KEY_HEADERS			= "a7";
	
	final int HTTP_GET_METHOD					= 0;
	final int HTTP_POST_METHOD					= 1;
	final int HTTP_PUT_METHOD					= 2;

	/*private final int HDW_AUTH 				= 0;
	private final String HIDDEN_WORDS[] = {
											"ZS4FO/jtFxDw40/ZvM4Yh1D1ENkvNoVcrGXq7CvZ1Oo="//HDW_AUTH:auth
										  };
	*/
	
	public IABRequestHandler()
	{
		
	}
	
	static IABRequestHandler getInstance()
	{
		if (mThis == null)
			mThis = new IABRequestHandler();
			
		return mThis;	
	}
	
	/*private String getHDWord(int idx)
	{
		return Encrypter.decrypt(HIDDEN_WORDS[idx]).trim();
	}*/
	

	public String SHA256(String input)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			MessageDigest digest = MessageDigest.getInstance(InAppBilling.GET_STR_CONST(IAB_SHA_256));
			digest.reset();

			digest.update(input.getBytes("UTF-8"));
			byte[] byteData = digest.digest();
			
			for (int i = 0; i < byteData.length; i++){
			  sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			
		}
		catch(Exception e)
		{
			ERR(TAG, e.getMessage());
		}
		return sb.toString();
	}
	
	public void doRequestByGet(String url, String qry, IABCallBack cb)
	{
		doRequestByGet(url, qry, null, cb);
	}
	
	public void doRequestByGet(String url, String qry, String jsonHeaders, IABCallBack cb)
	{
		Bundle bundle = new Bundle();
		bundle.putString(KEY_URL, url);
		bundle.putString(KEY_QUERY, qry);
		bundle.putString(KEY_HEADERS, jsonHeaders);
		bundle.putInt(KEY_HTTP_METHOD_USED, HTTP_GET_METHOD);
		
		new AsyncRequestHandler(cb).execute(bundle, null);
	}
	
	public void doRequestByPost(String url, String qry, IABCallBack cb)
	{
		doRequestByPost(url, qry, null, cb);
	}
	
	public void doRequestByPost(String url, String qry, String jsonHeaders, IABCallBack cb)
	{
		Bundle bundle = new Bundle();
		bundle.putString(KEY_URL, url);
		bundle.putString(KEY_QUERY, qry);
		bundle.putString(KEY_HEADERS, jsonHeaders);
		bundle.putInt(KEY_HTTP_METHOD_USED, HTTP_POST_METHOD);
		
		new AsyncRequestHandler(cb).execute(bundle, null);
	}
	
	public void doRequestByPut(String url, String qry, String jsonHeaders, IABCallBack cb)
	{
		Bundle bundle = new Bundle();
		bundle.putString(KEY_URL, url);
		bundle.putString(KEY_QUERY, qry);
		bundle.putString(KEY_HEADERS, jsonHeaders);
		bundle.putInt(KEY_HTTP_METHOD_USED, HTTP_PUT_METHOD);
		
		new AsyncRequestHandler(cb).execute(bundle, null);
	}
	
	class AsyncRequestHandler extends IABAsyncTask
	{
		IABCallBack mCB = null;
		public AsyncRequestHandler(IABCallBack cb)
		{
			mCB = cb;
		}
		
		protected Integer doInBackground(Bundle bundle) {
			Bundle response = doHTTPRequest(bundle);
			if (mCB != null)
			{
				mCB.runCallBack(response);
			}
			return new Integer(0);
		}
	}
	private Bundle doHTTPRequest(Bundle bundle)
	{
		INFO(TAG, "[doing HTTPRequest]");
		int mResponseCode = INVALID_RESULT;
		try {
		
			String sUrl 	= bundle.getString(KEY_URL);
			String sQuery 	= bundle.getString(KEY_QUERY);
			String sHeaders	= bundle.getString(KEY_HEADERS);
			int usePost		= bundle.getInt(KEY_HTTP_METHOD_USED);
			
			
			if (sQuery == null) sQuery = "";
			
			String concat = sUrl.contains("?")?"&":"?";
			
			URL url;
			if ((usePost==HTTP_POST_METHOD || usePost==HTTP_PUT_METHOD) || TextUtils.isEmpty(sQuery))
				url = new URL(sUrl);
			else
				url = new URL(sUrl + concat + sQuery);
			
			DBG(TAG,"Url: [" + sUrl+"]");
			DBG(TAG,"Query: [" + sQuery+"]");
			
			
			
			HttpURLConnection conn = getHttpsURLConnection(url, usePost);
			
			JSONObject jObj		= null;
			if (!TextUtils.isEmpty(sHeaders))
			{
				DBG(TAG, "Adding Extra headers from bundle info...");
				try {
					jObj = new JSONObject(sHeaders);
					Iterator iterator = jObj.keys();
					while (iterator.hasNext()) 
					{
						String key = iterator.next().toString();
						String value = jObj.getString(key);
						if (!TextUtils.isEmpty(value))
						{
							INFO(TAG, "Adding Header '"+key+"' Value '"+value+"'");
							conn.setRequestProperty(key,value);
						}
					}
				} catch (JSONException e) {
					DBG_EXCEPTION(e);
				}
				jObj = null;
			}	
			
			if ((usePost==HTTP_POST_METHOD || usePost==HTTP_PUT_METHOD) && !TextUtils.isEmpty(sQuery))
			{
				DBG(TAG, "Using " + ((usePost==HTTP_POST_METHOD)? "POST":"PUT"));
				conn.setFixedLengthStreamingMode(sQuery.length());
               	OutputStream outputstream = conn.getOutputStream();
               	outputstream.write(sQuery.getBytes(), 0, sQuery.length());
               	outputstream.flush();
			}
			
			String response = "";
			String string = null;
			
			mResponseCode = conn.getResponseCode();
			
			bundle.clear(); 			
			jObj = new JSONObject();
			
			Map map = conn.getHeaderFields();
			if (map != null)
			{
				StringBuffer sb;
				Set set = map.entrySet();
				Iterator iterator = set.iterator();
				while (iterator.hasNext()) 
				{
					Map.Entry<String, List<String>> entry = (Map.Entry<String, List<String>>)iterator.next();
					try
					{					
						if (entry.getKey() == null)
							continue;
						
						Iterator iterl = entry.getValue().iterator();
						sb = new StringBuffer();
						sb.append(iterl.next());
						while (iterl.hasNext()) 
						{
							sb.append(" ");
							sb.append(iterl.next());
						}
						String key = entry.getKey().toString();
						String value = sb.toString();
						
						INFO(TAG, "Server Header '"+key+"' Value '"+value+"'");
						
						jObj.put(key,value);
					} catch (JSONException e) 
					{
						ERR(TAG, e.getMessage());
						DBG_EXCEPTION(e);
					}
				}
			}
			
			JDUMP(TAG, mResponseCode);
			bundle.putInt(KEY_HTTP_RESPONSE,mResponseCode);
			bundle.putInt(KEY_REQUEST_RESULT, FAIL_RESULT);
			if (mResponseCode == HttpURLConnection.HTTP_OK || mResponseCode == HttpURLConnection.HTTP_CREATED)
			{
				InputStream inputstream 			= conn.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader 		= new BufferedReader(inputstreamreader);
				while ((string = bufferedreader.readLine()) != null) 
				{
					response += string;
				}
				bundle.putInt(KEY_REQUEST_RESULT, SUCCESS_RESULT);
				bundle.putString(KEY_RESPONSE,!TextUtils.isEmpty(response)?response:"");
				if (jObj != null)
				{
					bundle.putString(KEY_HEADERS,jObj.toString());
				}
				
				JDUMP(TAG,response);
			}
		} catch (Exception e) {		
		
			DBG_EXCEPTION(e);
			bundle.clear(); 
			bundle.putInt(KEY_REQUEST_RESULT, INVALID_RESULT);
			bundle.putString(KEY_RESPONSE, e.getMessage());
		}
		return bundle;
	}
	
	public String encodeString(String data)
	{
		try {
			String encodedString = URLEncoder.encode(data, "UTF-8");
			return encodedString;
		} catch (Exception e) {
			DBG_EXCEPTION(e);
			return "";
		}
	}
	
	private boolean isHTTPS(String url)
    {
    	return(url.indexOf("https")!=-1);
    }
	private HttpURLConnection getHttpsURLConnection(URL url, int usePost)
	{
		HttpURLConnection connection = null;
			
		try
		{
			/////// SSL //////////////
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[] {
				new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) {}
				public void checkServerTrusted(X509Certificate[] chain, String authType) {}
				public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
				}
			}, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());			
			///////////////////////////
			if (isHTTPS(url.toString()))
			{
				connection = (HttpsURLConnection)url.openConnection();
				//connection.setHostnameVerifier(new AllowAllHostnameVerifier());
			}
			else
				connection = (HttpURLConnection)url.openConnection();
			
			connection.setConnectTimeout(HTTP_TIMEOUT);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setFollowRedirects(true); 
			connection.setRequestProperty("Connection", "Keep-Alive");
			
			if(usePost==HTTP_POST_METHOD)
			{
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
			} else if (usePost == HTTP_GET_METHOD)
			{
				connection.setRequestMethod("GET");
			} else if (usePost == HTTP_PUT_METHOD)
			{
				connection.setDoOutput(true);
				connection.setRequestMethod("PUT");
			}
			
		}catch(Exception e){
			JDUMP(TAG,e.getMessage());
			DBG_EXCEPTION(e);
		}
		
		return connection;
	}

}
#endif //#if USE_IN_APP_BILLING
