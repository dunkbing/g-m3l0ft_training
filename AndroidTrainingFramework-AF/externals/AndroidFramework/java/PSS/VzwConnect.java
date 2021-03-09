#if PSS_VZW
package APP_PACKAGE.pss;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;
import java.util.Hashtable;
import java.util.Enumeration;
import android.os.Build;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;
import javax.net.SocketFactory;

public final class VzwConnect
{
	/* Errors List  */
	public static final int ERROR_NONE 					= 0;
	public static final int ERROR_SUCESS 				= 1;
	public static final int ERROR_OPEN_CONNECTION 		= 2;
	public static final int ERROR_WAITING				= 3;
	public static final int ERROR_SEND_REQUEST			= 4;
	public static final int ERROR_REQUEST_IN_PROCESS	= 5;
	public static final int ERROR_GET_REQUEST			= 6;
	public static final int ERROR_TIMEOUT				= 7;
	
	/* ----------------------------------------------------------------- */

	private Hashtable ht = null;
    
	private boolean processCanceledByUser = false;

	private boolean useSocket = false;
	
	private String EL = "\r\n";
	
	private Socket socket = null;
	
	private OutputStream out = null;
	
	private BufferedReader rd = null;
	/* Http Connection */
	private static HttpsURLConnection con = null;
	
	/* Input Stream */
	private static InputStream is = null;

	/* Output Stream */
	private static OutputStream os = null;
	
	/* URL */ 
	private static String sUrl = null;
	
	/* is Connected */
	private static boolean isConnected = false;
	
	/* in Process */
	private static boolean inProcess = false;
	
	/* return the last process error */
	
	private static int	lastError = ERROR_NONE;
	
	/* Request Response */
	private static String sResponse = "";
	
	/**
     * Verizon VzwConnect
     *
     * Basic Protocol Headers
     */
    public String X_MOD_RF 			= null;
    public String X_MOD_SC 			= null;
    public String X_MOD_SS 			= null;
    
	VzwGetNpost GNP = new VzwGetNpost();
    
	/*
	 * Constructor
	 *
	 * @param Void
	 * Return Void
	 */
	public VzwConnect ()
	{
	   if(Build.VERSION.SDK_INT >= 11)//Remember to compile with android platform 11 or above
	   {
	       DBG("PSS","Using sockets to connect . FW Version " + Build.VERSION.SDK_INT);
	       useSocket = true;
	   }
	}
	
	/*
	 * Connect to VzwConnect Service
	 * Access Public
	 *
	 * @param	Void
	 * Return	boolean
	 */
	public boolean connect(String sQuery)
    {
		
		if(!useSocket)
         		setsUrl("https://vmedia.verizonwireless.com/vShopWeb/VShop" + "?" + sQuery);
		else
			setsUrl("/vShopWeb/VShop" + "?" + sQuery);
		DBG( "PSS", "**URL**" + getsUrl()); 
		
		/* Close or destroy OS & is */
		close();
	
		try
		{
			/* Connect with the HTTP Service */
      		if(!useSocket)
		{
			URL connectURL = new URL(getsUrl());   
			con = (HttpsURLConnection) connectURL.openConnection();
	      		con.setConnectTimeout(30000);
	     		con.setReadTimeout(7000);
		}
		else
		{
			SocketFactory socketFactory = SSLSocketFactory.getDefault();
			socket = socketFactory.createSocket("vmedia.verizonwireless.com", 443);
			socket.setSoTimeout(30000);
			DBG("billing","Connect");
		}
			/* if the connection was success */
			isConnected = true;
		}
		catch (Exception e)
		{
			DBG( "PSS", "**billing**  billing error in HTTPBilling connect");
			/* Something happened */
			lastError = ERROR_OPEN_CONNECTION;
			setsResponse("<ResponseRootElement><code>-1</code><desc>A network error has occurred.\nPlease try again later.</desc></ResponseRootElement>");
		}
		
		return isConnected;
	}
	
	/*
	 * Return the Current Connection State
	 * Access Public
	 *
	 * @param	Void
	 * Return	boolean	return the current connection value
	 */
	public boolean isConnected(  )
    {
		return isConnected;
	}
	
	/*
	 * Return the Current Process State
	 * Access Public
	 *
	 * @param	Void
	 * Return	boolean	return the current process value
	 */
	public boolean inProcess(  )
    {
		return inProcess;
	}
	
	/*
	 * Return the Last Error in the whole process
	 * Access Public
	 *
	 * @param	Void
	 * Return	int		Last Error Process
	 */
	public int getLastError(  )
    {
		return lastError;
	}
	
	/*
	 * Send VzwConnect Process
	 * Access Public
	 *
	 * @param	Void
	 * Return	boolean
	 */
	public boolean sendRequest()
    {
		/* check if the output stream is Init*/
		if( !inProcess && ( (!useSocket && con != null) || ( useSocket && socket != null)))
		{
			try
			{
				if(!useSocket)
				{
					con.setRequestMethod("POST");

					/* Verizon Headers */
					con.setRequestProperty("User-Agent", GNP.userAgent);
					con.setRequestProperty("Connection", "Keep-Alive");

					if (X_MOD_SC != null)
					{
//						DBG( "PSS", "x-mod-sc: " + X_MOD_SC);
						con.setRequestProperty("x-mod-sc", X_MOD_SC);
//	        			DBG( "PSS", "x-mod-rf: " + X_MOD_RF);
						con.setRequestProperty("x-mod-rf", X_MOD_RF);
//	        			DBG( "PSS", "x-mod-ss: " + X_MOD_SS);
						con.setRequestProperty("x-mod-ss", X_MOD_SS);
					}
					con.setRequestProperty("X-Method", "POST");
					/* Verizon Headers */

					// Allow Inputs  
					con.setDoInput(true);  
			   
					// Allow Outputs  
					con.setDoOutput(true);

//	 				inProcess = true;
//	 				lastError = 0;
						
					/* 
						This is really important to notice, 
						we can't open the streaming until we finished to send the headers
						once we open the output stream then we should open the input one.
					*/
					is = con.getInputStream();
				}
				else
				{
					String request = "POST " + getsUrl() + " HTTP/1.1" + EL;
					request += "Host: vmedia.verizonwireless.com" + EL;
					request += "User-Agent: " + GNP.userAgent+ EL;
// 					request += "Connection: Keep-Alive" + EL;
					request += "Connection: close" + EL;
					
					if (X_MOD_SC != null)
					{
						request += "x-mod-sc: " + X_MOD_SC + EL;
						request += "x-mod-rf: " + X_MOD_RF + EL;
						request += "x-mod-ss: " + X_MOD_SS + EL;
					}
					
					request += "Cache-Control: no-cache" + EL;
					request += "X-Method: POST" + EL;
					request += "Content-type: application/x-www-form-urlencoded" + EL;
					request += "Content-length: 0" + EL;
					request += EL;
					
					DBG("billing","Send request \n" + request );
					
					out = socket.getOutputStream();
					is = socket.getInputStream();
					rd = new BufferedReader(new InputStreamReader(is));
					if(rd == null)
						DBG("billing","RD IS NULL");					

					out.write(request.getBytes());
					out.flush();
				}
        				
				inProcess = true;
				lastError = 0;      

			}
			catch( Exception e )
			{
			 	INFO("PSS","**billing**  billing error in HTTPBilling sendRequest "+e);
				/* Send Error */
				lastError = ERROR_SEND_REQUEST;
				inProcess = false; //pvu added to fix crash if is = con.getInputStream(); fails
				setsResponse("<ResponseRootElement><code>-1</code><desc>A network error has occurred.\nPlease try again later.</desc></ResponseRootElement>");
			}
		}
		else
		{
			lastError = ERROR_REQUEST_IN_PROCESS;
//			DBG( "PSS", "ERROR REQUEST IN PROCESS");
		}

		return inProcess;
	}
	
	/*
	 * Get Response from VzwConnect Process
	 * Access Public
	 *
	 * @param	Void
	 * Return	boolean
	 */
	public int handleProcess( )
	{
		int result = -2;
		
		if( inProcess )
		{
  		try
  		{
			if(!useSocket)
	  		{
				result = con.getResponseCode();
			}
			else
			{
			     
				String line = null;
				if(rd == null)
					DBG("billing","rd is null 1");
				ht = new Hashtable();
				int lineNumber =0;
				DBG("billing","INICIO*****************************");
				while ((line = rd.readLine()) != null) 
				{
					if(line.equals(""))
					{
						line = rd.readLine();
						ht.put("xml-body",new String(line));
						DBG("billing",line);
					}
					else
					{
						if(lineNumber == 0)
							ht.put("result",new String(line));
						else
						{
							String lineResponse = new String(line);
							String[] lineResponseArray = lineResponse.split(": ");                        	  				     
							ht.put(lineResponseArray[0],lineResponseArray[1]);
						}    	  					
						DBG("billing",line);
					}
					lineNumber++;
					DBG("billing","lineNumber="+lineNumber);    	  					
				}
				DBG("billing","FINAL*****************************");
				String theResult = (String)ht.get("result");    	  				
				result = Integer.parseInt(theResult.substring(9,12));				
           		}
  		}
  		catch (Exception e) {
			DBG( "PSS", "**billing**  billing error in HTTPBilling handleProcess get request 0");
  			lastError = ERROR_GET_REQUEST;
  		}
  		
  		DBG( "PSS", "Handling "+ result);
  		setsResponse("Handling "+ result);
		

			switch( result )
			{
				case HttpURLConnection.HTTP_OK:
					if( readRequest() > 0 )
					{
						DBG( "PSS", "HTTP Response "+ getsResponse());
						lastError = ERROR_SUCESS;
						if(!useSocket)
						{
							for (int i=0; ; i++) 
					    		{
							String name = con.getHeaderFieldKey(i);
						        String value = con.getHeaderField(i);
						        if (name == null && value == null){
						        	break;         
						        }
						        if (name == null){
						        	DBG( "PSS", "Server HTTP version, Response code:");
						            DBG( "PSS", value);
						            //System.out.print("\n");
						        }
						        else{
						        	if(name.equals("x-mod-sc"))
						        		X_MOD_SC = value;
						        	//DBG( "PSS", name + "=" + value);
						        }
							}
						}
						else
						{
                					X_MOD_SC = (String)ht.get("x-mod-sc");
            					}
					}
					else
					{
					  DBG( "PSS", "**billing**  billing error in HTTPBilling handleProcess get request");
						lastError = ERROR_GET_REQUEST;
					}
					inProcess = false;
					break;
					
				case HttpURLConnection.HTTP_PARTIAL:
				case HttpURLConnection.HTTP_ACCEPTED:
					lastError = ERROR_NONE;
					break;
					
				case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
				case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
				   DBG( "PSS", "**billing**  billing error in HTTPBilling handleProcess timeout");
					lastError = ERROR_TIMEOUT;
					inProcess = false;
					break;
					
				default:				
					DBG( "PSS", "**billing**  billing error in HTTPBilling handleProcess default");
					lastError = ERROR_GET_REQUEST;
					inProcess = false;
					setsResponse("<ResponseRootElement><code>-1</code><desc>A network error has occurred.\nPlease try again later.</desc></ResponseRootElement>");
					break;
			}
		}
		
		return lastError;
	}
	
	private int readRequest(  )
	{
		int ch = -1;

		try
		{
		 	if(!useSocket)
		 	{
				/* Create Byte Stream */
				ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
				
				/* Read while still data on the array */
				while ((ch = is.read()) != -1)
				{
					bytestream.write(ch);
				}
				
				/* convert the result into string */
				setsResponse(new String(bytestream.toByteArray()));
				
				bytestream.close();
			}
			else
			{
          			setsResponse((String)ht.get("xml-body"));
      			}
		}
		catch( IOException e )
		{
			DBG( "PSS", "**billing**  billing error in HTTPBilling readRequest");
			setsResponse("");
			setsResponse("<ResponseRootElement><code>-1</code><desc>A network error has occurred.\nPlease try again later.</desc></ResponseRootElement>");
			return 0;
		}
		if(getsResponse() == null)
			return 0;
		return getsResponse().length();
	}
	
	public void close()
	{
		try
		{
			if( con != null )
			{
				cancelRequest();
						
				if( is != null )
				{
					is.close();
					is = null;
				}
				
					con = null;
				
				isConnected = false;
			}
			
			if(socket != null )
			{	
				cancelRequest();
				if( is != null )
			   	{
           				is.close();
           				is = null;
				}
				if( out != null )
			 	{
           				out.close();
            				out = null;
         			}
				//socket.close();
				socket = null;
				isConnected = false;
      			}
			
		}
		catch( Exception e )
		{
			DBG( "PSS", "Close Error: " + e.toString() );
		}
	}
	
	public void cancelRequest()
	{
		/* clean Last Response */
		inProcess = false;
		lastError = ERROR_NONE;
		setsResponse("");

		try
		{
			/* restart output stream */
			if( os != null )
			{
				os.close();
				os = null;
			}
		}
		catch( Exception e )
		{
			DBG( "PSS", "Close Error: " + e.toString() );
		}
	}

	public void setsResponse(String sResponse) {
		VzwConnect.sResponse = sResponse;
	}

	public String getsResponse() {
		return sResponse;
	}

	public String md5(String s) {
		try {
			// Create MD5 Hash  
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");  
		    digest.update(s.getBytes());  
		    byte messageDigest[] = digest.digest();  

		    // Create Hex String  
		    StringBuffer hexString = new StringBuffer();  
		    for (int i=0; i<messageDigest.length; i++)
		    	hexString.append(Integer.toHexString(0xFF & messageDigest[i]));  
		    return hexString.toString();  

		} catch (NoSuchAlgorithmException e) {
			DBG_EXCEPTION(e);
		}  
		return "";  
	}

	public void setsUrl(String sUrl) {
		VzwConnect.sUrl = sUrl;
	}

	public String getsUrl() {
		return sUrl;
	}
}
#endif