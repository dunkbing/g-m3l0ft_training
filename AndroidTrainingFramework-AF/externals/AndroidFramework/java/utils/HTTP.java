#if USE_INSTALLER || USE_BILLING || USE_TRACKING_FEATURE_INSTALLER || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM) || USE_PSS
package APP_PACKAGE.GLUtils;

import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.net.Proxy;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpConnection;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.HostnameVerifier;

#if USE_BILLING || USE_IN_APP_BILLING
	#if GAMELOFT_SHOP || USE_BILLING
	import APP_PACKAGE.billing.common.AModel;
	#endif
	#if !USE_IN_APP_BILLING_CRM
	import APP_PACKAGE.billing.common.Constants;
	#endif
	#if (USE_IN_APP_BILLING && USE_IN_APP_GLOT_LOGGING)
		import APP_PACKAGE.iab.IABLogging;
	#endif
	#if USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM
	import APP_PACKAGE.iab.InAppBilling;
	#endif
#endif


#if USE_INSTALLER && USE_TRACKING_FEATURE_INSTALLER
import APP_PACKAGE.installer.utils.Tracker;
#endif


/**
 * HTTP connection class.
 * <p>
 * Provides services for creating and managing HTTP requests. This is
 * non-blocking and runs as a separate thread on the carrier.
 * It also provides a method for canceling an ongoing transaction.
 * <p>
 * How to use:
 * <p>
 * 1. create an instance<br>
 * 2. call <code>sendByGet()</code> to begin a transaction<br>
 * 3. call <code>isInProgress()</code> to find out the status of an on going transaction<br>
 * 4. call <code>cancel()</code> to stop at any time<br>
 * 5. check <code>m_response</code> for return from server<br>
 * 6. call <code>cleanup()</code> to free resources (will free the <code>m_response</code> buffer,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;so use it before cleanup)
 *
 * @author Gameloft Online Team
 * @version 1.4
 * <DT><B>Copyright:</B></DT>
 * <DD>Gameloft SA (c) 2006</DD>
 */
public final class HTTP implements Runnable, Config
#if USE_BILLING
, Constants
#endif
{

    /**
     * Provides information whether this class' instance is currently doing a
     * transfer or not.
     *
     * @return <code>true</code> if a call is in progress,
     * <code>false</code> otherwise.
     */
    public boolean isInProgress()
    {
        return m_bInProgress;
    }


    /**
     * Tells whether an error occurred during the transaction.
     *
     * @return <code>true</code> if an error has been encountered,
     * <code>false</code> otherwise.
     */
    public boolean isErrorOccurred()
    {
        return m_bError;
    }

    /**
     * Cancels the ongoing transaction.
     * <p>
     * It is safe to call at any time, but keep in mind that the request may
     * have already reached the server, triggering any changes it implied. Those
     * changes will not be discarded by the server and will be saved.
     */
    public void cancel() {
        if (XPlayer.HTTP_NO_CANCEL) {
            return;
        } else {
            if (m_c != null) {
                try {
                    synchronized (m_c) {
                        m_is.close();
                    }
                } catch (Exception e) {}
                try {
                    synchronized (m_c) {
                        m_c.close();
                    }
                } catch (Exception e) {}

                if (XPlayer.USE_HTTP_POST) {
                    try {
                        synchronized (m_os) {
                            m_os.close();
                        }
                    } catch (Exception e) {}

                    m_os = null;
                }
            }
            m_is = null;
            m_c = null;

            m_thread = null;

            // Run the garbage collector.
            System.gc();
            m_bInProgress = false;
        }
    }

    /**
     * Frees any resources used by this class.
     * <p>
     * It is OK to call at any time, but it should be called after using any
     * data returned from the server as it also deletes <code>m_response</code>.
     * <p>
     * <DD><DL>
     * <DT><B>Side effect:</B></DT>
     * <DD>It will cancel the current transaction, if any.</DD>
     * </DL>
     * </DD>
     * @see #m_response
     */
    public void cleanup()
    {
        cancel();
        m_response = null;
    }

    public HTTP()
    {

    	try
    	{
    	SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[] { new MyTrustManager() }, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
    	}catch(Exception expsec){DBG("http","Exception Occurred on HTTP Constructor Method!!! "+expsec.toString());}
    }
	
	
	public String getResponseHeader(String headerKey)
	{
		String responseHeader = null;
		try{
			if(!IsHTTPS())
				responseHeader = m_urlc.getHeaderField(headerKey);
			else
				responseHeader = m_surlc.getHeaderField(headerKey);
		}catch(Exception e)
		{
			responseHeader = "0";//if error, not mandatory
		}
		return responseHeader;

	}
	
	
    /**
     * The entry point.&nbsp;It starts a HTTP request.
     *
     * @param sUrl URL of the server
     * @param sQuery The query to be sent to the server
     */
    public void sendByGet(String sUrl, String sQuery) {

        // blocking wait on previous request end
        while (m_bInProgress) {
            try {
                if (XPlayer.ENABLE_TIMEOUT) {
                    // check if enough time has passed
                    if (System.currentTimeMillis() - XPlayer.callstarttime > XPlayer.CONN_TIMEOUT)
                    {  // Clean up connections and sets m_bInProgress to false.
                       cancel();
                    }
                }
                synchronized (this)
                {
                    wait(50);
                }
            } catch (Exception e) {}
        }
        // in the middle of transaction
        m_bInProgress = true;

        // store where and what to send
        if (XPlayer.USE_HTTP_POST)
        {
            // for HTTP get, we need to store separately the server address and the request body.
            m_sUrl = sUrl;
            m_sQuery = sQuery;
        }
        else
        {
            	//m_sUrl = sUrl + "?" + sQuery;
				m_sUrl = sUrl + ((sUrl.indexOf("?")>0)?"&":"?") + sQuery;
        }
        // force the server's response Content-Type
        if((XPlayer.ForceContentType.equals("TextHtml")) || (XPlayer.ForceContentType.equals("texthtml")) || (XPlayer.ForceContentType.equals("TEXTHTML")))
        {
        	m_sUrl += "&texthtml=1";
        }
        else if((XPlayer.ForceContentType.equals("TextPlain")) || (XPlayer.ForceContentType.equals("textplain")) || (XPlayer.ForceContentType.equals("TEXTPLAIN")))
        {
        	m_sUrl += "&textplain=1";
        }

        // wait for the previous transaction
        if (m_thread != null)
        {
            try
            {
                m_thread.join();
            }
            catch (Exception e)
            {}
        }
        // begin a thread which will do the transaction
        // m_timeBegin = System.currentTimeMillis();
        #if USE_INSTALLER || USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
        ConnectionTimer.start(XPlayer.CONN_TIMEOUT);
        #endif
        m_bError = false;
        m_thread = new Thread(this);
        m_thread.start();
    }
	
#if USE_IN_APP_GLOT_LOGGING 
    public void sendByGet(String sUrl, String sQuery, String requestName)
	{
		m_requestName = requestName;
		sendByGet(sUrl, sQuery);
	}
#endif

    /**
     * This is the Function to determinte the type of connection. HTTP or HTTPS
     *
     * @return <code>true</code> if HTTPS of <code>false</code> if not
     */
    private boolean IsHTTPS()
    {
    	return(m_sUrl.indexOf("https")!=-1);
    }

    /**
     * The main thread handling the HTTP request. It is started by the
     * <code>sendByGet(String, String)</code> function.
     *
     * @see #sendByGet(String, String)
     */
    public void run() {

    	if(!IsHTTPS())
    	{
    		DBG("HTTP", "********* NORMAL HTTP **********");
    		if (XPlayer.USE_HTTP_POST)
            {   // for HTTP post, need to test the query too.
                if (m_sQuery == null) {
                    cancel();
                    m_bError = true;
                    m_bInProgress = false;
                    onValidationHandled();
                    return;
                }
            }

            try {
                m_bError = false;

                //open connection
               DBG("http","HTTP: run:connecting to [" + m_sUrl + "]");
    			Carrier carrier = XPlayer.getCarrier();
    			System.setProperty("http.keepAlive", "false");



                URL url = new URL(m_sUrl);

    			DBG("http","HTTP: Proxy Enabled: " +carrier.useProxy());
                if (carrier.useProxy())
    			{
    				DBG("http","HTTP: Proxy server: " +carrier.getProxyServer());
    				DBG("http","HTTP: Proxy port:   " +carrier.getProxyPort());

    				InetAddress ia = InetAddress.getByName(carrier.getProxyServer());
    				InetSocketAddress sa = new InetSocketAddress(ia, carrier.getProxyPort());
    				Proxy proxy = new Proxy(Proxy.Type.HTTP, sa);
                	m_urlc = (HttpURLConnection)url.openConnection(proxy);
                }
                else
    			{
                	m_urlc = (HttpURLConnection)url.openConnection();
                }

                if (XPlayer.USE_HTTP_POST) {
                	m_urlc.setRequestMethod("POST");
                } else {
                	m_urlc.setRequestMethod("GET");
                }

                m_urlc.setRequestProperty("Connection", "close");
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("Connection", "close", m_requestName);


                	m_urlc.setRequestProperty("User-Agent", XPlayer.getDevice().getUserAgent());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("User-Agent", XPlayer.getDevice().getUserAgent(), m_requestName);

                if (X_UP_SUBNO != null) {
                	m_urlc.setRequestProperty("x-up-subno", X_UP_SUBNO);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-subno", X_UP_SUBNO, m_requestName);
					
                	DBG("http","****** HTTP Warning: X_UP_SUBNO=" + X_UP_SUBNO);
                }

                m_urlc.setRequestProperty("x-gl-d", SUtils.GetSerialKey());
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-gl-d", SUtils.GetSerialKey(), m_requestName);
            	DBG("http","***** HTTP Warning: Adding "+SUtils.GetSerialKey()+" to http headers");

              #if AUTO_UPDATE_HEP
                String build_model=null;
                try{
                	build_model = android.os.Build.MODEL;
                }catch(Exception e1){}
                
                if (build_model == null)
                	build_model = "default-model";
                else{
                  build_model = java.net.URLEncoder.encode(build_model.trim(), "UTF-8");
                }
                if (m_sUrl.startsWith(APP_PACKAGE.installer.GameInstaller.UPDATE_HEP_URL))
				{
                	m_urlc.setRequestProperty("x-android-os-build-model", build_model);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-android-os-build-model", build_model, m_requestName);
				}
                else
              #endif
				{
					m_urlc.setRequestProperty("x-android-os-build-model", android.os.Build.MODEL);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-android-os-build-model", android.os.Build.MODEL, m_requestName);
					DBG("http","***** HTTP Warning: Adding "+android.os.Build.MODEL+" to http headers");
				}

              #if AUTO_UPDATE_HEP
                String lineNumber=null;
                try{
                	lineNumber = XPlayer.getDevice().getLineNumber();
                }catch(Exception e2){}
                
                if (lineNumber == null)
                	lineNumber = "00";
                else{
                  lineNumber = java.net.URLEncoder.encode(lineNumber.trim(),"UTF-8");
                }
                if (m_sUrl.startsWith(APP_PACKAGE.installer.GameInstaller.UPDATE_HEP_URL))
				{
                	m_urlc.setRequestProperty("x-up-gl-subno", lineNumber);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-subno", lineNumber, m_requestName);
				}	
                else
              #endif
				{
					m_urlc.setRequestProperty("x-up-gl-subno", XPlayer.getDevice().getLineNumber());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-subno", XPlayer.getDevice().getLineNumber(), m_requestName);
					DBG("http","***** HTTP Warning: x-up-gl-subno = "+ XPlayer.getDevice().getLineNumber());
				}	

            #if AUTO_UPDATE_HEP
				if (m_sUrl.startsWith(APP_PACKAGE.installer.GameInstaller.UPDATE_HEP_URL)){
					DBG("http","***** HTTP AutoUpdate Request using safe imei");
					String tmp_imei="00000000000000";
					try{
						tmp_imei = XPlayer.getDevice().getDeviceId();
					}catch(Exception ee){
						tmp_imei="00000000000000";
					}
					if (tmp_imei == null)
						tmp_imei="00000000000000";
					m_urlc.setRequestProperty("x-up-gl-imei", tmp_imei);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-imei", tmp_imei, m_requestName);
					DBG("http","***** HTTP Warning: x-up-gl-imei = "+ tmp_imei);
				}else{
					m_urlc.setRequestProperty("x-up-gl-imei", XPlayer.getDevice().getIMEI());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-imei", XPlayer.getDevice().getIMEI(), m_requestName);
					DBG("http","***** HTTP Warning: x-up-gl-imei = "+ XPlayer.getDevice().getIMEI());
				}
			#else
				#if (HDIDFV_UPDATE != 2)
					m_urlc.setRequestProperty("x-up-gl-imei", XPlayer.getDevice().getIMEI());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-imei", XPlayer.getDevice().getIMEI(), m_requestName);
					DBG("http","***** HTTP Warning: x-up-gl-imei = "+ XPlayer.getDevice().getIMEI());
				#endif
				#if HDIDFV_UPDATE
					m_urlc.setRequestProperty("x-up-gl-hdidfv", XPlayer.getDevice().getHDIDFV());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-hdidfv", XPlayer.getDevice().getHDIDFV(), m_requestName);
					DBG("A_S"+HDIDFV_UPDATE,XPlayer.getDevice().getHDIDFV());
					
					m_urlc.setRequestProperty("x-up-gl-gldid", XPlayer.getDevice().getGLDID());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-gldid", XPlayer.getDevice().getGLDID(), m_requestName);
				#endif
				
			#endif

			#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
				DBG("http","***** HTTPS Warning: x-up-gl-ggi = \'" + GGI + "\'");
				m_urlc.setRequestProperty("x-up-gl-ggi", ""+GGI);
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-ggi", ""+GGI, m_requestName);
					
				DBG("http","***** HTTPS Warning: x-up-gl-gamecode = \'" + XPlayer.getDevice().getDemoCode() + "\'");
				m_urlc.setRequestProperty("x-up-gl-gamecode", XPlayer.getDevice().getDemoCode());
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-gamecode", XPlayer.getDevice().getDemoCode(), m_requestName);

				if (XPlayer.mGLLiveUid != null)
				{
					DBG("http","***** HTTPS Warning:  x-up-gl-acnum = \'" + XPlayer.mGLLiveUid + "\'");
					m_urlc.setRequestProperty("x-up-gl-acnum", XPlayer.mGLLiveUid);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-acnum", XPlayer.mGLLiveUid, m_requestName);
				}
				if (XPlayer.mUserCreds != null)
				{
					DBG("http","***** HTTPS Warning:  x-up-gl-fed-credentials = \'" + XPlayer.mUserCreds + "\'");
					m_urlc.setRequestProperty("x-up-gl-fed-credentials", XPlayer.mUserCreds);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-fed-credentials", XPlayer.mUserCreds, m_requestName);
					
					DBG("http","***** HTTPS Warning:  x-up-gl-fed-client-id = \'" + CLIENTID + "\'");
					m_urlc.setRequestProperty("x-up-gl-fed-client-id", CLIENTID);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-fed-client-id", CLIENTID, m_requestName);
				}
            	if (XPlayer.mPurchaseID != "")
				{
					DBG("http","***** HTTPS Warning:  x-up-gl-purchaseid = \'" + XPlayer.mPurchaseID + "\'");
					m_urlc.setRequestProperty("x-up-gl-purchaseid", XPlayer.mPurchaseID);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-purchaseid", XPlayer.mPurchaseID, m_requestName);
				}
				if (XPlayer.mDataCenter != null && !android.text.TextUtils.isEmpty(XPlayer.mDataCenter))
				{
					DBG("http","***** HTTPS Warning:  " + InAppBilling.GET_STR_CONST(IAB_HEADER_FEDERATION_DC) + " = \'" + XPlayer.mDataCenter + "\'");
					m_urlc.setRequestProperty(InAppBilling.GET_STR_CONST(IAB_HEADER_FEDERATION_DC), XPlayer.mDataCenter);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV(InAppBilling.GET_STR_CONST(IAB_HEADER_FEDERATION_DC), XPlayer.mDataCenter, m_requestName);
				}
				
				DBG("http","***** HTTPS Warning:  Accept = \'application/com.gameloft.ecomm.android.iap-v1.1+plain\'");
				m_urlc.setRequestProperty("Accept", "application/com.gameloft.ecomm.android.iap-v1.1+plain");
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("Accept", "application/com.gameloft.ecomm.android.iap-v1.1+plain", m_requestName);
			#endif

		if(m_sUrl.contains("gettimestamprequest=1"))
		{
			DBG("http","***** HTTPS Warning:  adding time = 1 to the headers");
			m_urlc.setRequestProperty("time", "1");
			LOGGING_APPEND_REQUEST_HEADERS_BY_NV("time", "1", m_requestName);
		}
		#if SKT_STORE
		if(m_sUrl.contains("&txid="))
		{
			String[] args = m_sUrl.split("&");
			if (args.length > 0)
			{
				for (int x=0;x<args.length;x++)
				{
					if (args[x].contains("txid="))
					{
						String transactionId = args[x].substring(5);
						DBG("http","***** HTTPS Warning:  adding x-up-txid=" + transactionId +" to the headers");
						m_urlc.setRequestProperty("x-up-txid", transactionId);
						LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-txid", transactionId, m_requestName);
						if (APP_PACKAGE.iab.SktIabActivity.signData != null && APP_PACKAGE.iab.SktIabActivity.signData.length() > 0)
						{
							DBG("http","***** HTTPS Warning:  adding x-up-signdata to the headers");
							m_urlc.setRequestProperty("x-up-signdata", APP_PACKAGE.iab.SktIabActivity.signData);
							LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-signdata", APP_PACKAGE.iab.SktIabActivity.signData, m_requestName);
						}
					}
				}
			}
		}
		#endif

    			if (Config.x_up_calling_line_id != null)
    			{
                	m_urlc.setRequestProperty("x-up-calling-line-id", Config.x_up_calling_line_id);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-calling-line-id", Config.x_up_calling_line_id, m_requestName);
                }

                if (Config.x_up_uplink != null) {
                	m_urlc.setRequestProperty("x-up-uplink", Config.x_up_uplink);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-uplink", Config.x_up_uplink, m_requestName);
                }

                if (Config.x_nokia_msisdn != null) {
                	m_urlc.setRequestProperty("x-Nokia-MSISDN", Config.x_nokia_msisdn);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-Nokia-MSISDN", Config.x_nokia_msisdn, m_requestName);
                }

                if (XPlayer.USE_HTTP_POST) {
                	m_urlc.setRequestProperty("Content-Type", "text/html");
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("Content-Type", "text/html", m_requestName);
                	String sQuery = "b=" + m_sQuery;
                	m_urlc.setRequestProperty("Content-Length", String.valueOf(sQuery.length()));
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("Content-Length", String.valueOf(sQuery.length()), m_requestName);

                	m_os = m_urlc.getOutputStream();
                	m_os.write(sQuery.getBytes(), 0, sQuery.length());
                	m_os.flush();
                }


               DBG("http","HTTP: run: receive");
			#if USE_IN_APP_GLOT_LOGGING
				if (m_requestName != null)
				LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO(m_requestName));
			#endif
                // actually this opens the input connection
                if (m_urlc.getResponseCode() != HttpURLConnection.HTTP_OK) { // check under what condition (when) enters here.
                	DBG("http","HTTP RESPONSE CODE RECEIVED = "+m_urlc.getResponseCode());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR,IAP_LOG_STATUS_ERROR,"http Response code = "+m_urlc.getResponseCode());
                	cancel();
                	m_bError = true;
                	m_bInProgress = false;
                	onValidationHandled();
                	return;
                }

                synchronized (m_urlc) {
                	m_is = m_urlc.getInputStream();
                }

                ByteArrayOutputStream bao = new ByteArrayOutputStream();

                byte[] abInBuffer = new byte[RECEIVEBUFFERSIZE];
                int nBytesRead = 0;
                while (nBytesRead != -1) {
                	nBytesRead = m_is.read(abInBuffer, 0, RECEIVEBUFFERSIZE);

                    if (ENABLE_DEBUG) {

                        if (nBytesRead != -1) {
                        }
                    }
                	if (nBytesRead != -1) bao.write(abInBuffer, 0, nBytesRead);
                }

			#if !RELEASE_VERSION || USE_IN_APP_GLOT_LOGGING
			    java.util.Map map = m_urlc.getHeaderFields();	
				if (map != null)
				{
					java.util.Set set = map.entrySet();
					java.util.Iterator iterator = set.iterator();
					StringBuffer sb;
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
							LOGGING_APPEND_RESPONSE_HEADERS_BY_NV(key, value, m_requestName);
							INFO("http", "Server Header '"+key+"' Value '"+value+"'");
						} catch (Exception e) 
						{	
							DBG_EXCEPTION(e);
						}
					}
				}
			#endif
			
               DBG("http","HTTP: run: received [\n" + bao.toString() + "\n]");
               DBG("http","HTTP: run: total bytes read: [" + bao.size() + "]");

                m_response = bao.toString();
			#if USE_IN_APP_GLOT_LOGGING
				if (m_requestName != null)
				{
					LOGGING_APPEND_RESPONSE_PARAM(m_response, m_requestName);
					LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO(m_requestName));
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, m_requestName+": "+ LOGGING_REQUEST_GET_TIME_ELAPSED(m_requestName) +" seconds" );
					LOGGING_REQUEST_REMOVE_REQUEST_INFO(m_requestName);
				}
			#endif
				
    		} catch (SocketException se) {
    			DBG_EXCEPTION(se);
            	DBG("http","HTTP: run: SocketException : " + se.toString());
                m_bError = true;
                m_bInProgress = false;
    			XPlayer.setLastErrorMessage(XPlayer.ERROR_CONNECTION);
            } catch (UnknownHostException uhe) {
    			DBG_EXCEPTION(uhe);
            	DBG("http","HTTP: run: UnknownHostException : " + uhe.toString());
                m_bError = true;
                m_bInProgress = false;
    			XPlayer.setLastErrorMessage(XPlayer.ERROR_CONNECTION);
            } catch (Exception e) {
            	DBG_EXCEPTION(e);
            	DBG("http","HTTP: run: exception : " + e.toString());
                m_bError = true;
                m_bInProgress = false;
            }
            cancel();
            m_bInProgress = false;
           	onValidationHandled();

    	}
    	else
    	{
    		// THIS CODE MUST USE HTTPS (HttpsURLConnection m_surlc)
    		DBG("HTTP", "********* SECURED HTTPS **********");

        	if (XPlayer.USE_HTTP_POST)
            {   // for HTTP post, need to test the query too.
                if (m_sQuery == null) {
                    cancel();
                    m_bError = true;
                    m_bInProgress = false;
                    onValidationHandled();
                    return;
                }
            }
			System.setProperty("https.keepAlive", "false");
			System.setProperty("http.keepAlive", "false");
			LOGGING_APPEND_REQUEST_HEADERS_BY_NV("https.keepAlive", "false", m_requestName);
			LOGGING_APPEND_REQUEST_HEADERS_BY_NV("http.keepAlive", "false", m_requestName);

            try {
                m_bError = false;

                //open connection
               DBG("http","HTTPS: run:connecting to [" + m_sUrl + "]");
    			Carrier carrier = XPlayer.getCarrier();
				

                URL url = new URL(m_sUrl);

    			DBG("http","HTTPS: Proxy Enabled: " +carrier.useProxy());
                if (carrier.useProxy())
    			{
    				DBG("http","HTTPS: Proxy server: " +carrier.getProxyServer());
    				DBG("http","HTTPS: Proxy port:   " +carrier.getProxyPort());

    				InetAddress ia = InetAddress.getByName(carrier.getProxyServer());
    				InetSocketAddress sa = new InetSocketAddress(ia, carrier.getProxyPort());
    				Proxy proxy = new Proxy(Proxy.Type.HTTP, sa);
    				m_surlc = (HttpsURLConnection)url.openConnection(proxy);
                }
                else
    			{
                	m_surlc = (HttpsURLConnection)url.openConnection();
                }

                if (XPlayer.USE_HTTP_POST) {
                	m_surlc.setRequestMethod("POST");
                } else {
                	m_surlc.setRequestMethod("GET");
                }

				final int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
				if (sdkVersion != android.os.Build.VERSION_CODES.FROYO) {
					m_surlc.setRequestProperty("Connection", "close");
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("Connection", "close", m_requestName);
				}


                m_surlc.setRequestProperty("User-Agent", XPlayer.getDevice().getUserAgent());
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("User-Agent", XPlayer.getDevice().getUserAgent(), m_requestName);

                if (X_UP_SUBNO != null) {
                	m_surlc.setRequestProperty("x-up-subno", X_UP_SUBNO);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-subno", X_UP_SUBNO, m_requestName);
                	DBG("http","****** HTTPS Warning: X_UP_SUBNO=" + X_UP_SUBNO);
                }

                m_surlc.setRequestProperty("x-gl-d", SUtils.GetSerialKey());
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-gl-d", SUtils.GetSerialKey(), m_requestName);
            	DBG("http","***** HTTPS Warning: Adding "+SUtils.GetSerialKey()+" to http headers");

              #if AUTO_UPDATE_HEP
              	String build_model = null;
              	try{
              		build_model = android.os.Build.MODEL;
              	}catch(Exception e){}
                
                if (build_model == null)
              		build_model = "default-model";
              	else{
                  build_model = java.net.URLEncoder.encode(build_model.trim(), "UTF-8");
                }
              	if (m_sUrl.startsWith(APP_PACKAGE.installer.GameInstaller.UPDATE_HEP_URL))
				{
              		m_surlc.setRequestProperty("x-android-os-build-model", build_model);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-android-os-build-model", build_model, m_requestName);
				}
              	else
              #endif
			   {
					m_surlc.setRequestProperty("x-android-os-build-model", android.os.Build.MODEL);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-android-os-build-model", android.os.Build.MODEL, m_requestName);
					DBG("http","***** HTTPS Warning: Adding "+android.os.Build.MODEL+" to http headers");
				}

              #if AUTO_UPDATE_HEP
              	String lineNumber = null;
              	try{
              		lineNumber = XPlayer.getDevice().getLineNumber();
              	}catch(Exception e1){}
                
                if (lineNumber == null)
              		lineNumber = "00";
              	else{
                  lineNumber = java.net.URLEncoder.encode(lineNumber.trim(), "UTF-8");
                }
              	if (m_sUrl.startsWith(APP_PACKAGE.installer.GameInstaller.UPDATE_HEP_URL))
				{
              		m_surlc.setRequestProperty("x-up-gl-subno", lineNumber);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-subno", lineNumber, m_requestName);
				}	
              	else
              #endif
				{
					m_surlc.setRequestProperty("x-up-gl-subno", XPlayer.getDevice().getLineNumber());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-subno", XPlayer.getDevice().getLineNumber(), m_requestName);
					DBG("http","***** HTTPS Warning: x-up-gl-subno = "+ XPlayer.getDevice().getLineNumber());
				}	

            #if AUTO_UPDATE_HEP
				if (m_sUrl.startsWith(APP_PACKAGE.installer.GameInstaller.UPDATE_HEP_URL)){
					DBG("http","***** HTTP AutoUpdate Request using safe imei");
					String tmp_imei="00000000000000";
					try{
						tmp_imei = XPlayer.getDevice().getDeviceId();
					}catch(Exception ee){
						tmp_imei="00000000000000";
					}
					if (tmp_imei == null)
						tmp_imei="00000000000000";
					m_surlc.setRequestProperty("x-up-gl-imei", tmp_imei);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-imei", tmp_imei, m_requestName);
					DBG("http","***** HTTPS Warning: x-up-gl-imei = "+ tmp_imei);
				}else{
					m_surlc.setRequestProperty("x-up-gl-imei", XPlayer.getDevice().getIMEI());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-imei", XPlayer.getDevice().getIMEI(), m_requestName);
					DBG("http","***** HTTPS Warning: x-up-gl-imei = "+ XPlayer.getDevice().getIMEI());
				}
			#else
				#if (HDIDFV_UPDATE != 2)
					m_surlc.setRequestProperty("x-up-gl-imei", XPlayer.getDevice().getIMEI());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-imei", XPlayer.getDevice().getIMEI(), m_requestName);
					DBG("http","***** HTTPS Warning: x-up-gl-imei = "+ XPlayer.getDevice().getIMEI());
				#endif
				#if HDIDFV_UPDATE
					m_surlc.setRequestProperty("x-up-gl-hdidfv", XPlayer.getDevice().getHDIDFV());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-hdidfv", XPlayer.getDevice().getHDIDFV(), m_requestName);
					DBG("A_S"+HDIDFV_UPDATE,XPlayer.getDevice().getHDIDFV());
					
					m_surlc.setRequestProperty("x-up-gl-gldid", XPlayer.getDevice().getGLDID());
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-gldid", XPlayer.getDevice().getGLDID(), m_requestName);
				#endif
			#endif

			#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
				DBG("http","***** HTTPS Warning: x-up-gl-ggi = \'" + GGI + "\'");
				m_surlc.setRequestProperty("x-up-gl-ggi", ""+GGI);
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-ggi", ""+GGI, m_requestName);
					
				DBG("http","***** HTTPS Warning: x-up-gl-gamecode   = \'" + XPlayer.getDevice().getDemoCode() + "\'");
				m_surlc.setRequestProperty("x-up-gl-gamecode  ", XPlayer.getDevice().getDemoCode());
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-gamecode  ", XPlayer.getDevice().getDemoCode(), m_requestName);

            	if (XPlayer.mGLLiveUid != null)
				{
					DBG("http","***** HTTPS Warning:  x-up-gl-acnum = \'" + XPlayer.mGLLiveUid + "\'");
					m_surlc.setRequestProperty("x-up-gl-acnum", XPlayer.mGLLiveUid);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-acnum", XPlayer.mGLLiveUid, m_requestName);
				}
				if (XPlayer.mUserCreds != null)
				{
					DBG("http","***** HTTPS Warning:  x-up-gl-fed-credentials = \'" + XPlayer.mUserCreds + "\'");
					m_surlc.setRequestProperty("x-up-gl-fed-credentials", XPlayer.mUserCreds);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-fed-credentials", XPlayer.mUserCreds, m_requestName);
					
					DBG("http","***** HTTPS Warning:  x-up-gl-fed-client-id = \'" + CLIENTID + "\'");
					m_surlc.setRequestProperty("x-up-gl-fed-client-id", CLIENTID);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-fed-client-id", CLIENTID, m_requestName);
				}
            	if (XPlayer.mPurchaseID != "")
				{
					DBG("http","***** HTTPS Warning:  x-up-gl-purchaseid = \'" + XPlayer.mPurchaseID + "\'");
					m_surlc.setRequestProperty("x-up-gl-purchaseid", XPlayer.mPurchaseID);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-gl-purchaseid", XPlayer.mPurchaseID, m_requestName);
				}
				if (XPlayer.mDataCenter != null && !android.text.TextUtils.isEmpty(XPlayer.mDataCenter))
				{
					DBG("http","***** HTTPS Warning:  " + InAppBilling.GET_STR_CONST(IAB_HEADER_FEDERATION_DC) + " = \'" + XPlayer.mDataCenter + "\'");
					m_surlc.setRequestProperty(InAppBilling.GET_STR_CONST(IAB_HEADER_FEDERATION_DC), XPlayer.mDataCenter);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV(InAppBilling.GET_STR_CONST(IAB_HEADER_FEDERATION_DC), XPlayer.mDataCenter, m_requestName);
				}
				
				DBG("http","***** HTTPS Warning:  Accept = \'application/com.gameloft.ecomm.android.iap-v1.1+plain\'");
				m_surlc.setRequestProperty("Accept", "application/com.gameloft.ecomm.android.iap-v1.1+plain");
				LOGGING_APPEND_REQUEST_HEADERS_BY_NV("Accept", "application/com.gameloft.ecomm.android.iap-v1.1+plain", m_requestName);
			#endif               

		if(m_sUrl.contains("gettimestamprequest=1"))
		{
			DBG("http","***** HTTPS Warning:  adding time = 1 to the headers");
			m_surlc.setRequestProperty("time", "1");
			LOGGING_APPEND_REQUEST_HEADERS_BY_NV("time", "1", m_requestName);
		}
		#if SKT_STORE
		if(m_sUrl.contains("&txid="))
		{
			String[] args = m_sUrl.split("&");
			if (args.length > 0)
			{
				for (int x=0;x<args.length;x++)
				{
					if (args[x].contains("txid="))
					{
						String transactionId = args[x].substring(5);
						DBG("http","***** HTTPS Warning:  adding x-up-txid="+transactionId+" to the headers");
						m_surlc.setRequestProperty("x-up-txid", transactionId);
						LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-txid", transactionId, m_requestName);
						if (APP_PACKAGE.iab.SktIabActivity.signData != null && APP_PACKAGE.iab.SktIabActivity.signData.length() > 0)
						{
							DBG("http","***** HTTPS Warning:  adding x-up-signdata to the headers [\n"+ APP_PACKAGE.iab.SktIabActivity.signData + "\n]");
							m_surlc.setRequestProperty("x-up-signdata", APP_PACKAGE.iab.SktIabActivity.signData);
							LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-signdata", APP_PACKAGE.iab.SktIabActivity.signData, m_requestName);
						}
					}
				}
			}
		}
		#endif

    			if (Config.x_up_calling_line_id != null)
    			{
    				m_surlc.setRequestProperty("x-up-calling-line-id", Config.x_up_calling_line_id);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-calling-line-id", Config.x_up_calling_line_id, m_requestName);
                }

                if (Config.x_up_uplink != null) {
                	m_surlc.setRequestProperty("x-up-uplink", Config.x_up_uplink);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-up-uplink", Config.x_up_uplink, m_requestName);
                }

                if (Config.x_nokia_msisdn != null) {
                	m_surlc.setRequestProperty("x-Nokia-MSISDN", Config.x_nokia_msisdn);
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("x-Nokia-MSISDN", Config.x_nokia_msisdn, m_requestName);
                }


                if (XPlayer.USE_HTTP_POST) {
                	m_surlc.setRequestProperty("Content-Type", "text/html");
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("Content-Type", "text/html", m_requestName);
                	String sQuery = "b=" + m_sQuery;
                	m_surlc.setRequestProperty("Content-Length", String.valueOf(sQuery.length()));
					LOGGING_APPEND_REQUEST_HEADERS_BY_NV("Content-Length", String.valueOf(sQuery.length()), m_requestName);

                	m_os = m_surlc.getOutputStream();
                	m_os.write(sQuery.getBytes(), 0, sQuery.length());
                	m_os.flush();
                }

               DBG("http","HTTPS: run: receive");
			#if USE_IN_APP_GLOT_LOGGING
				if (m_requestName != null)
				LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO(m_requestName));
			#endif
                // actually this opens the input connection
                if (m_surlc.getResponseCode() != HttpsURLConnection.HTTP_OK) { // check under what condition (when) enters here.
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR,IAP_LOG_STATUS_ERROR,"https Response code = "+m_urlc.getResponseCode());
                	DBG("http","HTTPS RESPONSE CODE RECEIVED = "+m_surlc.getResponseCode());
                	cancel();
                	m_bError = true;
                	m_bInProgress = false;
                	onValidationHandled();
                	return;
                }

                synchronized (m_surlc) {
                	m_is = m_surlc.getInputStream();
                }

                ByteArrayOutputStream bao = new ByteArrayOutputStream();

                byte[] abInBuffer = new byte[RECEIVEBUFFERSIZE];
                int nBytesRead = 0;
                while (nBytesRead != -1) {
                	nBytesRead = m_is.read(abInBuffer, 0, RECEIVEBUFFERSIZE);

                    if (ENABLE_DEBUG) {
                        //DBG("http","HTTP: run: read [" + nBytesRead + "] bytes from server");

                        if (nBytesRead != -1) {
                        	//String str = new String(abInBuffer, 0, nBytesRead);
                        	//DBG("http","HTTP: run: read msg [" + str + "] ");
                        }
                    }
                	if (nBytesRead != -1) bao.write(abInBuffer, 0, nBytesRead);
                }

			#if !RELEASE_VERSION || USE_IN_APP_GLOT_LOGGING
			    java.util.Map map = m_surlc.getHeaderFields();	
				if (map != null)
				{
					java.util.Set set = map.entrySet();
					java.util.Iterator iterator = set.iterator();
					StringBuffer sb;
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
							LOGGING_APPEND_RESPONSE_HEADERS_BY_NV(key, value, m_requestName);
							INFO("http", "Server Header '"+key+"' Value '"+value+"'");
						} catch (Exception e) 
						{	
							DBG_EXCEPTION(e);
						}
					}
				}
			#endif
				
					
				DBG("http","HTTPS: run: total bytes read: [" + bao.size() + "]");
               
                m_response = bao.toString();
			#if USE_IN_APP_GLOT_LOGGING
				if (m_requestName != null)
				{
					LOGGING_APPEND_RESPONSE_PARAM(m_response, m_requestName);
					LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO(m_requestName));
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, m_requestName+": "+ LOGGING_REQUEST_GET_TIME_ELAPSED(m_requestName) +" seconds" );
					LOGGING_REQUEST_REMOVE_REQUEST_INFO(m_requestName);
				}
			#endif
				
				String responseA[] = m_response.split("\n");//DDMS cuts large strings 
				DBG("http","HTTPS: run: received [\n");
				for (int i = 0; i<responseA.length; i ++) {
					DBG("http",responseA[i]);
				}
				DBG("http","]");
    		} catch (SocketException se) {
    			DBG_EXCEPTION(se);
            	DBG("http","HTTPS: run: SocketException : " + se.toString());
                m_bError = true;
                m_bInProgress = false;
    			XPlayer.setLastErrorMessage(XPlayer.ERROR_CONNECTION);
            } catch (UnknownHostException uhe) {
    			DBG_EXCEPTION(uhe);
            	DBG("http","HTTPS: run: UnknownHostException : " + uhe.toString());
                m_bError = true;
                m_bInProgress = false;
    			XPlayer.setLastErrorMessage(XPlayer.ERROR_CONNECTION);
            } catch (Exception e) {
            	DBG_EXCEPTION(e);
            	DBG("http","HTTPS: run: exception : " + e.toString());
                m_bError = true;
                m_bInProgress = false;
            }
			m_surlc.disconnect();
            cancel();
            m_bInProgress = false;
           	onValidationHandled();
    	}

    }
#if GAMELOFT_SHOP || USE_BILLING
	private AModel mModel = null;
	public AModel getModel ()
	{
		return mModel;
	}
	
	public void setModel (AModel model)
	{
		mModel = model;
	}
#endif	
	
    private void onValidationHandled()
    {
        #if USE_INSTALLER || USE_BILLING || GAMELOFT_SHOP
    	ConnectionTimer.stop();
    	#endif
#if USE_BILLING || GAMELOFT_SHOP
	#if USE_INSTALLER && USE_TRACKING_FEATURE_INSTALLER
	if (!Tracker.isUsingTracking)
	#endif
	{
		if (mModel != null)
			mModel.onValidationHandled();
	}	
#endif
    }

    /**
     * Read this much from server at a time
     * */
    private final int RECEIVEBUFFERSIZE = 16;

    /**
     * Pointer to current thread doing transfer, used to wait for previous transfer to end
     * */
    private Thread m_thread = null;

    /**
     * Where to send
     * */
    private String m_sUrl;

#if USE_IN_APP_GLOT_LOGGING 
    private String m_requestName = null;
#endif

    /**
     * What to send
     * Used only with POST requests.
     * */
    private String m_sQuery;

    //**************************************************************************
    //      Network Objects.
    //**************************************************************************

    /**
     * The variable that will make the connection.
     * */
    private HttpConnection m_c = null;

    /**
     * Network Objects.
     * */
    private HttpURLConnection m_urlc = null;

    private HttpsURLConnection m_surlc = null;


    /**
     * To save the info brought by <code>m_c</code>
     * */
    private InputStream m_is = null;

    /**
     * Used only with POST requests (USE_HTTP_POST == true).
     */
    private OutputStream m_os = null;

    /**
     * The received message buffer.
     * <p>
     * Check this after a transaction completes to get the received data.
     */
    public String m_response;

    /**
     * Indicates if a transaction is already being done.
     * */
    boolean m_bInProgress = false;

    /**
     * Tells whether an error occurred during the transaction. It can be <code>true</code> if an error has
     * been encountered, <code>false</code> otherwise.
     */
    public boolean m_bError = false;

    /**
     * Stores the emulated value of the X_UP_SUBNO when running on an
     * emulator.
     * <p>
     * Each Cingular Orange Network phone comes with a factory preset unique
     * X_UP_SUBNO. This is not true for the emulator though, which does not
     * have such a value associated with it.
     * <p>
     * Because this value is requested by the server, for testing on emulator
     * this needs to be emulated somehow. To do this, set the
     * <b>X_UP_SUBNO</b> property in the .jad file to something like
     * "SZZYourName01@cingular.com" to enable sending of a X_UP_SUBNO
     * while debugging in emulator. The property value will be read from the
     * .jad, set accordingly in here and used throughout the running of the
     * transaction.
     * <p>
     * DO NOT set this in the .jad on a phone release though. The phone's
     * X_UP_SUBNO will be automatically added by the carrier.
     * <p>
     * Default is <code>null</code>.
     */
    public static String X_UP_SUBNO 			= null;

//#if USE_BILLING
    class MyHostnameVerifier implements HostnameVerifier
    {

     //@Override
         public boolean verify(String hostname, SSLSession session)
          {
    	 	return true;
          }

    }

    class MyTrustManager implements X509TrustManager
    {
            //@Override
            public void checkClientTrusted(X509Certificate[] chain, String authType)
            {
            }
            // @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType)
            {
            }
            //@Override
            public X509Certificate[] getAcceptedIssuers()
            {
             return null;
            }
    }
//#endif //#if USE_BILLING
}

#endif //#if USE_INSTALLER || USE_BILLING || USE_TRACKING_FEATURE_INSTALLER