package APP_PACKAGE.DRM.Verizon;

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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpConnection;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.HostnameVerifier;
// import APP_PACKAGE.Billing.Config;
// import APP_PACKAGE.Billing.Model;
// //import APP_PACKAGE.Billing.SUtils;
// import APP_PACKAGE.Billing.ConnectionTimer;

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
public final class HTTP implements Runnable
{

	private XPlayer mXplayer;
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



    public HTTP(XPlayer xplayer)
    {

		mXplayer = xplayer;
    	try
    	{
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new MyTrustManager() }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
    	}catch(Exception expsec){
			d("Exception Occurred on HTTP Constructor Method!!! "+expsec.toString());
		}
    }

    /**
     * The entry point.&nbsp;It starts a HTTP request.
     *
     * @param sUrl URL of the server
     * @param sQuery The query to be sent to the server
     */
    public void sendByGet(String sUrl, String sQuery) {

    	////sQuery+="&d="+SUtils.getLManager().GetSerialKey();//Serial Key Parameter
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
            	m_sUrl = sUrl + "?" + sQuery;
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
        #if USE_INSTALLER || USE_BILLING
        ConnectionTimer.start(XPlayer.CONN_TIMEOUT);
        #endif
        m_bError = false;
        m_thread = new Thread(this);
        m_thread.start();
    }
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
                d("HTTP: run:connecting to [" + m_sUrl + "]");
    			//Carrier carrier = XPlayer.getCarrier();
    			System.setProperty("http.keepAlive", "false");

                URL url = new URL(m_sUrl);

    			//SUtils.log("HTTP: Proxy Enabled: " +carrier.useProxy());
                // if (carrier.useProxy())
    			// {
    				// //SUtils.log("HTTP: Proxy server: " +carrier.getProxyServer());
    				// //SUtils.log("HTTP: Proxy port:   " +carrier.getProxyPort());

    				// InetAddress ia = InetAddress.getByName(carrier.getProxyServer());
    				// InetSocketAddress sa = new InetSocketAddress(ia, carrier.getProxyPort());
    				// Proxy proxy = new Proxy(Proxy.Type.HTTP, sa);
                	// m_urlc = (HttpURLConnection)url.openConnection(proxy);
                // }
                // else
    			{
                	m_urlc = (HttpURLConnection)url.openConnection();
                }

                if (XPlayer.USE_HTTP_POST) {
                	m_urlc.setRequestMethod("POST");
                } else {
                	m_urlc.setRequestMethod("GET");
                }

                m_urlc.setRequestProperty("Connection", "close");

                // m_urlc.setRequestProperty("User-Agent", XPlayer.getDevice().getUserAgent());

                // if (X_UP_SUBNO != null) {
                	// m_urlc.setRequestProperty("x-up-subno", X_UP_SUBNO);
                	// //SUtils.log("****** HTTP Warning: X_UP_SUBNO=" + X_UP_SUBNO);
                // }

                // m_urlc.setRequestProperty("x-gl-d", SUtils.getLManager().GetSerialKey());
            	// //SUtils.log("***** HTTP Warning: Adding "+SUtils.getLManager().GetSerialKey()+" to http headers");

                // m_urlc.setRequestProperty("x-android-os-build-model", android.os.Build.MODEL);
            	// //SUtils.log("***** HTTP Warning: Adding "+android.os.Build.MODEL+" to http headers");

            	// m_urlc.setRequestProperty("x-up-gl-subno", XPlayer.getDevice().getLineNumber());
            	// //SUtils.log("***** HTTP Warning: x-up-gl-subno = "+ XPlayer.getDevice().getLineNumber());

            	// m_urlc.setRequestProperty("x-up-gl-imei", XPlayer.getDevice().getIMEI());
            	// //SUtils.log("***** HTTP Warning: x-up-gl-imei = "+ XPlayer.getDevice().getIMEI());


    			// if (Config.x_up_calling_line_id != null)
    			// {
                	// m_urlc.setRequestProperty("x-up-calling-line-id", Config.x_up_calling_line_id);
                // }

                // if (Config.x_up_uplink != null) {
                	// m_urlc.setRequestProperty("x-up-uplink", Config.x_up_uplink);
                // }

                // if (Config.x_nokia_msisdn != null) {
                	// m_urlc.setRequestProperty("x-Nokia-MSISDN", Config.x_nokia_msisdn);
                // }

                if (XPlayer.USE_HTTP_POST) {
                	m_urlc.setRequestProperty("Content-Type", "text/html");
                	String sQuery = "b=" + m_sQuery;
                	m_urlc.setRequestProperty("Content-Length", String.valueOf(sQuery.length()));

                	m_os = m_urlc.getOutputStream();
                	m_os.write(sQuery.getBytes(), 0, sQuery.length());
                	m_os.flush();
                }


                d("HTTP: run: receive");

                // actually this opens the input connection
                if (m_urlc.getResponseCode() != HttpURLConnection.HTTP_OK) { // check under what condition (when) enters here.
                	d("HTTP RESPONSE CODE RECEIVED = "+m_urlc.getResponseCode());
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

                    // if (ENABLE_DEBUG) {

                        // if (nBytesRead != -1) {
                        // }
                    // }
                	if (nBytesRead != -1) bao.write(abInBuffer, 0, nBytesRead);
                }

                d("HTTP: run: received [\n" + bao.toString() + "\n]");
                d("HTTP: run: total bytes read: [" + bao.size() + "]");

                m_response = bao.toString();
    		} catch (SocketException se) {
    			DBG_EXCEPTION(se);
            	d("HTTP: run: SocketException : " + se.toString());
                m_bError = true;
                m_bInProgress = false;
    			XPlayer.setLastErrorMessage(XPlayer.ERROR_CONNECTION);
            } catch (UnknownHostException uhe) {
    			DBG_EXCEPTION(uhe);
            	d("HTTP: run: UnknownHostException : " + uhe.toString());
                m_bError = true;
                m_bInProgress = false;
    			XPlayer.setLastErrorMessage(XPlayer.ERROR_CONNECTION);
            } catch (Exception e) {
            	DBG_EXCEPTION(e);
            	d("HTTP: run: exception : " + e.toString());
                m_bError = true;
                m_bInProgress = false;
            }
            cancel();
            m_bInProgress = false;
           	onValidationHandled();

    	}
    	else
    	{
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
                d("HTTPS: run:connecting to [" + m_sUrl + "]");
    			//Carrier carrier = XPlayer.getCarrier();
    			System.setProperty("http.keepAlive", "false");



                URL url = new URL(m_sUrl);

    			// //SUtils.log("HTTPS: Proxy Enabled: " +carrier.useProxy());
                // if (carrier.useProxy())
    			// {
    				// //SUtils.log("HTTPS: Proxy server: " +carrier.getProxyServer());
    				// //SUtils.log("HTTPS: Proxy port:   " +carrier.getProxyPort());

    				// InetAddress ia = InetAddress.getByName(carrier.getProxyServer());
    				// InetSocketAddress sa = new InetSocketAddress(ia, carrier.getProxyPort());
    				// Proxy proxy = new Proxy(Proxy.Type.HTTP, sa);
    				// m_surlc = (HttpsURLConnection)url.openConnection(proxy);
                // }
                // else
    			{
                	m_surlc = (HttpsURLConnection)url.openConnection();
                }

                if (XPlayer.USE_HTTP_POST) {
                	m_surlc.setRequestMethod("POST");
                } else {
                	m_surlc.setRequestMethod("GET");
                }

                m_surlc.setRequestProperty("Connection", "close");



                //m_surlc.setRequestProperty("User-Agent", XPlayer.getDevice().getUserAgent());

                // if (X_UP_SUBNO != null) {
                	// m_surlc.setRequestProperty("x-up-subno", X_UP_SUBNO);
                	// //SUtils.log("****** HTTPS Warning: X_UP_SUBNO=" + X_UP_SUBNO);
                // }

                //m_surlc.setRequestProperty("x-gl-d", SUtils.getLManager().GetSerialKey());
            	//SUtils.log("***** HTTPS Warning: Adding "+SUtils.getLManager().GetSerialKey()+" to http headers");

                //m_surlc.setRequestProperty("x-android-os-build-model", android.os.Build.MODEL);
            	//SUtils.log("***** HTTPS Warning: Adding "+android.os.Build.MODEL+" to http headers");

            	//m_surlc.setRequestProperty("x-up-gl-subno", XPlayer.getDevice().getLineNumber());
            	//SUtils.log("***** HTTPS Warning: x-up-gl-subno = "+ XPlayer.getDevice().getLineNumber());

            	//m_surlc.setRequestProperty("x-up-gl-imei", XPlayer.getDevice().getIMEI());
            	//SUtils.log("***** HTTPS Warning: x-up-gl-imei = "+ XPlayer.getDevice().getIMEI());


    			// if (Config.x_up_calling_line_id != null)
    			// {
    				// m_surlc.setRequestProperty("x-up-calling-line-id", Config.x_up_calling_line_id);
                // }

                // if (Config.x_up_uplink != null) {
                	// m_surlc.setRequestProperty("x-up-uplink", Config.x_up_uplink);
                // }

                // if (Config.x_nokia_msisdn != null) {
                	// m_surlc.setRequestProperty("x-Nokia-MSISDN", Config.x_nokia_msisdn);
                // }


                if (XPlayer.USE_HTTP_POST) {
                	m_surlc.setRequestProperty("Content-Type", "text/html");
                	String sQuery = "b=" + m_sQuery;
                	m_surlc.setRequestProperty("Content-Length", String.valueOf(sQuery.length()));

                	m_os = m_surlc.getOutputStream();
                	m_os.write(sQuery.getBytes(), 0, sQuery.length());
                	m_os.flush();
                }

                d("HTTPS: run: receive");

                // actually this opens the input connection
                if (m_surlc.getResponseCode() != HttpsURLConnection.HTTP_OK) { // check under what condition (when) enters here.
                	d("HTTPS RESPONSE CODE RECEIVED = "+m_surlc.getResponseCode());
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

                    // if (ENABLE_DEBUG) {
                        // //SUtils.log("HTTP: run: read [" + nBytesRead + "] bytes from server");

                        // if (nBytesRead != -1) {
                        	// //String str = new String(abInBuffer, 0, nBytesRead);
                        	// //SUtils.log("HTTP: run: read msg [" + str + "] ");
                        // }
                    // }
                	if (nBytesRead != -1) bao.write(abInBuffer, 0, nBytesRead);
                }

               d("HTTPS: run: received [\n" + bao.toString() + "\n]");
                d("HTTPS: run: total bytes read: [" + bao.size() + "]");
               d("HTTP: run: received [" + Encoder.Blob2String(bao.toString()) + "]");

                m_response = bao.toString();
    		} catch (SocketException se) {
    			DBG_EXCEPTION(se);
            	d("HTTPS: run: SocketException : " + se.toString());
                m_bError = true;
                m_bInProgress = false;
    			XPlayer.setLastErrorMessage(XPlayer.ERROR_CONNECTION);
            } catch (UnknownHostException uhe) {
    			DBG_EXCEPTION(uhe);
            	d("HTTPS: run: UnknownHostException : " + uhe.toString());
                m_bError = true;
                m_bInProgress = false;
    			XPlayer.setLastErrorMessage(XPlayer.ERROR_CONNECTION);
            } catch (Exception e) {
            	DBG_EXCEPTION(e);
            	d("HTTPS: run: exception : " + e.toString());
                m_bError = true;
                m_bInProgress = false;
            }
            cancel();
            m_bInProgress = false;
           	onValidationHandled();
    	}

    }

    private void onValidationHandled()
    {
        #if USE_INSTALLER || USE_BILLING
    	ConnectionTimer.stop();
    	#endif

		//mXplayer.
		///Model.onValidationHandled();
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
    //public static String X_UP_SUBNO 			= null;


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




	public static boolean DEBUG = VerizonDRM.DEBUG;
	private static void d(String msg)
	{
		if(DEBUG)
		{
			DBG("VerizonDRM",msg);
		}
	}
	private static void d(Exception e)
	{
		if(DEBUG)
		{
			DBG_EXCEPTION(e);
		}
	}

}
