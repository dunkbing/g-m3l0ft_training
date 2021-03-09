#if USE_OPTUS_DRM
package com.msap.store.drm.android.util;

import java.io.*;
import java.net.*;
import java.util.*;


/**
 * This class contains some convenient functions for calling HTTP servers.
 * @author Edison Chan
 */
public class HttpUtility {
	public final static String ENCODING = "UTF-8";

	/**
	 * Send a HTTP POST request to the given URL with the given post data.
	 * @param url location to be requested.
	 * @param type MIME type of the posted data.
	 * @param data posted data.
	 * @return connection for the post request.
	 * @throws IOException when the request cannot be initated.
	 */
	public static final HttpURLConnection startPost(URL url, String type, byte[] data) 
			throws IOException {
		HttpURLConnection connection = null;

   	try {
			connection = (HttpURLConnection) url.openConnection();
 			connection.setRequestMethod("POST");
			connection.setInstanceFollowRedirects(true);
  		connection.setDoInput(true);
			connection.setDoOutput(true);
  		connection.setUseCaches(false);
 			connection.setRequestProperty("Content-Type", type);
 			connection.setRequestProperty("Content-Length", Integer.toString(data.length));
		    		
			OutputStream out = connection.getOutputStream();
  		out.write(data);
  		out.flush();
  		out.close();

			return connection;	
		} catch (IOException ex) {
			if (connection != null) {
				connection.disconnect();
			}

			throw ex;
		}
	}

	/**
	 * Send a HTTP POST request to the given URL with the given post data.
	 * @param url location to be requested.
	 * @param type MIME type of the posted data.
	 * @param data posted data.
	 * @return connection for the post request.
	 * @throws IOException when the request cannot be initated.
	 */
	public static final HttpURLConnection startPost(URL url, String type, String data) 
			throws IOException {
		return startPost(url, type, data.getBytes(ENCODING));
	}

	/**
	 * Read all response data from a connection as binary data.
	 * @param connection an open connection to a remote site.
	 * @return binary data downloaded from the connection.
	 * @throws IOException when data cannot be fetched from the connection.
	 */
	public static final byte[] readBinaryResponseData(HttpURLConnection connection) 
			throws IOException {
		InputStream in = connection.getInputStream();
		ByteArrayOutputStream cache = new ByteArrayOutputStream();

		try {
			byte[] buffer = new byte[10240];
			int size = 0;
	    		
			while ((size = in.read(buffer)) > 0) {
	 			cache.write(buffer, 0, size);
		 	}
	
			return cache.toByteArray();
		} finally {
			in.close();
		}
	}

	/**
	 * Read all response data from a connection as textual data.
	 * @param connection an open connection to a remote site.
	 * @return text data downloaded from the connection.
	 * @throws IOException when data cannot be fetched from the connection.
	 */
	public static final String readTextualResponseData(HttpURLConnection connection) 
			throws IOException {
		return new String(readBinaryResponseData(connection), ENCODING);
	}
};

#endif	//USE_OPTUS_DRM
