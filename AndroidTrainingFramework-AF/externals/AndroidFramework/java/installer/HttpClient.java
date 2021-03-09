package APP_PACKAGE.installer.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import android.util.Log;

public class HttpClient 
//#if USE_DOWNLOAD_MANAGER
implements Defs
//#endif
{
	public static final int CONNECTION_TIME_OUT_START	= 60 * 1000; // 60 secs
	public static final int READING_TIME_OUT_START		= 180 * 1000; // 120 secs

	public static int CONNECTION_TIME_OUT	= CONNECTION_TIME_OUT_START;
	public static int READING_TIME_OUT		= READING_TIME_OUT_START;

	HttpURLConnection conn;

	public InputStream getInputStream(String url) throws IOException, SocketTimeoutException
	{
		url = url.replace(" ", "%20");
		URL aURL = new URL(url);
		
		conn = (HttpURLConnection) aURL.openConnection();
		conn.setConnectTimeout(CONNECTION_TIME_OUT);
		conn.setReadTimeout(READING_TIME_OUT);		
		conn.connect();

		InputStream is = conn.getInputStream();
		return is;
	}	
	
	public String GetRedirectedUrl(String url)
	{
		try{
		url = url.replace(" ", "%20");
		URL aURL = new URL(url);
		
		conn = (HttpURLConnection) aURL.openConnection();
		conn.setConnectTimeout(CONNECTION_TIME_OUT);
		conn.setReadTimeout(READING_TIME_OUT);		
		conn.connect();

		InputStream is = conn.getInputStream();
		url = conn.getURL().toString();
		}catch(Exception e)
		{
			return url;
		}
		
		return url;
	}

	public InputStream getInputStream(String url, long skipBytes, long size) throws IOException, SocketTimeoutException
	{
		return getInputStream(url, skipBytes, 0, size); 
	}
	
	public InputStream getInputStream(String url, long skipBytes, long globalOffset, long size) throws IOException, SocketTimeoutException
	{	
		url = url.replace(" ", "%20");
		URL aURL = new URL(url);       
        
		conn = (HttpURLConnection) aURL.openConnection();
		conn.setConnectTimeout(CONNECTION_TIME_OUT);
		conn.setReadTimeout(READING_TIME_OUT);
		
		if (size > 0)
			conn.setRequestProperty("Range", "bytes=" + (globalOffset + skipBytes) + "-" + (globalOffset + skipBytes + size));
		else
			conn.setRequestProperty("Range", "bytes=" + (globalOffset + skipBytes) + "-");
		conn.connect();	

		InputStream is = conn.getInputStream();
		return is;
	}
	
	public long getFileSize(String url) throws IOException, SocketTimeoutException
	{
		URL aURL = new URL(url);		
		conn = (HttpURLConnection) aURL.openConnection();

		// long size = conn.getContentLength();
		long size = -1;
		try{
			size = Long.parseLong(conn.getHeaderField("Content-Length"));
		}catch(Exception e)
		{
			size = -1;
		}
		
		return size;	
	}
	
	public long getFileSize()
	{
		long size = 0;
		if(conn != null)
		{
			size = -1;
			// size = conn.getContentLength();
			try{
				size = Long.parseLong(conn.getHeaderField("Content-Length"));
			}catch(Exception e)
			{
				size = -1;
			}
		}
		return size;	
	}

	public String getHeaderField(String field)
	{
        String header = conn.getHeaderField(field);
		return (header==null)? "":header;
	}
	
	public String getHeaderField(int id)
	{
        String header = conn.getHeaderField(id);
		return (header==null)? "":header;
	}
	
	public URL getConnectionURL()
	{
		return (conn==null) ? null:conn.getURL();
	}

	private boolean isDisconnected = false;
	public void close()
	{
		isDisconnected = false;
		disconnect();
		while (isDisconnected == false)
		{
		   try {
			  Thread.sleep(50);
		   } catch (Exception e) {
		   }
		}
		conn = null;
	}

	public void disconnect()
	{
		new Thread()
		{
			public void run()
			{
				if(conn != null)
				{
					conn.disconnect();
				}
				isDisconnected = true;
			}
		}.start();
	}
	
	public void incrementConnectionTimeout()
	{
		// Increment timeout time by 20%, up to 2X initial timeout to handle bad coverage areas
		if (CONNECTION_TIME_OUT < 2*CONNECTION_TIME_OUT_START)
			CONNECTION_TIME_OUT += CONNECTION_TIME_OUT_START/5;
		if (READING_TIME_OUT < 2*READING_TIME_OUT_START)
			READING_TIME_OUT += READING_TIME_OUT_START/5;
	}
	
	public int getHeaderInt(String field, int def)
	{
	try{
	
		String head = getHeaderField(field);
		DBG("HttpClient", "Header " + field + ": " + head);
		if (head.compareTo("") != 0)
		{
			DBG("HttpClient", "getHeaderInt " + Integer.parseInt(head.replace(".","")));
			return Integer.parseInt(head.replace(".",""));
		}
		return def;
	}catch(Exception ex){return def;}
	}
	
	public boolean getHeaderBoolean(String field, boolean def)
	{
		String head = getHeaderField(field);
		DBG("GameInstaller", "Header " + field + ": " + head);
		if (getHeaderField(field).compareToIgnoreCase("no") == 0)
			return false;
		else if (getHeaderField(field).compareToIgnoreCase("0") == 0)
			return false;
		else if (getHeaderField(field).compareToIgnoreCase("yes") == 0)
			return true;
		else if (getHeaderField(field).compareToIgnoreCase("1") == 0)
			return true;

		return def;
	}
}
