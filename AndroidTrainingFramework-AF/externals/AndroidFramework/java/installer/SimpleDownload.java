package APP_PACKAGE.installer.utils;

import android.util.Log;

import android.os.Build;
import java.io.DataOutputStream;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.DataFormatException;
import java.util.Vector;
import APP_PACKAGE.installer.Utils;
import APP_PACKAGE.installer.GameInstaller;
import APP_PACKAGE.installer.ToastMessages;

public class SimpleDownload extends Thread
{
	private			final	int 					SECTION_RETRY_SLEEP		= 3000; // 3 * 1000
	private			final	int 					FILE_READ_SIZE			= 32768; // 32 * 1024
	private 				Thread 					blinker					= null;
	private 				FileOutputStream 		m_out 					= null;
	private 				HttpClient 				m_clientHTTP			= null;
	private 				boolean 				m_isFinished			= false;
	private 				boolean 				m_isDisconnected		= false;
	private					boolean					m_isDownloadFailed		= false;
	private 				long 					m_DownloadedSize 		= 0;
	private 				long 					m_FileDownloadedSize 	= 0;
	private 				String 					m_strDataLink			= "";
	private 				String 					m_strOutputPath			= "";
	private 				int 					CurrPackFile			= 0;
	private					byte[]					m_temp 					= null;
	private 				DataInputStream			m_DIStream;
	private 				Vector<PackFile>		rRes					= null;
	private					long					mSizeToSkip				= 0;
	private					int						MAX_RETRIES				= 0;
	private					int						mRetries				= 0;
	public boolean isTerimanted = false;
	
	/**
	 * SimpleDownload's Constructor
	 * @param strDataLink 	the data pack's link
	 * @param strOutputPath the output path
	 * @param rRes 			the resources pack files vector
	 * @param globalOffset 	the global offset in the data pack
	 * @param sectionID 	the section's ID
	 */
	public SimpleDownload(String strDataLink, String strOutputPath, Vector<PackFile> rRes, long size_to_skip)
	{	
		m_strDataLink = strDataLink;
		m_strOutputPath = strOutputPath;
		CurrPackFile = 0;
		this.rRes = rRes;
		m_isFinished = false;
		m_isDisconnected = false;
		mSizeToSkip = size_to_skip;
	}
	/**
	 * SimpleDownload's Constructor from another SimpleDownload's data
	 * @param s the old section's information
	 */
	public SimpleDownload(SimpleDownload s)
	{	
		m_strDataLink = s.getDataLink();
		m_strOutputPath = s.getOutputPath();
		CurrPackFile = s.CurrPackFile;
		this.rRes = s.rRes;
		m_isFinished = false;
		m_isDisconnected = false;
	}
		
	/**
	 * Breaks the section's run main loop
	 */
	public void stopThread()
	{
		blinker = null;
		m_isFinished = false;
		m_isDisconnected = true;
	}
	
	/**
	 * Inilializes the connection in order to get the data pack
	 * @throws Exception If there is a problem connection to the server, the method
	 * will throw Exception marking this problem.
	 */
	public void initialize(String URL, long size_skip) throws Exception
	{		
		try
		{
			m_clientHTTP = new HttpClient();
			InputStream in = null;
			try
			{
				if (URL == "")
					in = m_clientHTTP.getInputStream(m_strDataLink, size_skip, 0);
				else
					in = m_clientHTTP.getInputStream(URL, size_skip, 0);
			}
			catch (SocketTimeoutException ste)
			{
				m_clientHTTP.incrementConnectionTimeout();
				ERR_TOAST(ToastMessages.Section.InitializeSocketError);
			}
			catch (Exception e)
			{
				ERR_TOAST(ToastMessages.Section.InitializeError);
				in = null;
			}
			
			if(in == null)
			{
				m_clientHTTP.close();
				try {this.sleep(SECTION_RETRY_SLEEP);} catch (Exception exx){}
			}
            if(in != null)
               	m_DIStream = new DataInputStream(in);
            else
             	throw new Exception();
				
	    	if(m_DIStream == null)
             	throw new Exception();
			m_temp = new byte[FILE_READ_SIZE];
		}
		catch (SocketTimeoutException ste)
		{
			ERR_TOAST(ToastMessages.Section.InitializeSocketError);
			m_clientHTTP.incrementConnectionTimeout();
			m_isDownloadFailed = true;
		}
		catch (Exception e)
		{
			m_isDownloadFailed = true;
			ERR_TOAST(ToastMessages.Section.InitializeError);
			DBG_EXCEPTION(e);
			throw new Exception();
		}
	}
	
	public void update()
	{
	//	if(m_isDownloadFailed)
	//		blinker = null;
	}
	/**
	 * The SimpleDownloads run loop where the data is downloaded
	 */
	public void run()
	{
		boolean isStreaming = false;
		String entryName = "";
		
		Thread thisThread = Thread.currentThread();
		blinker = Thread.currentThread();
		m_DownloadedSize = 0;
		m_FileDownloadedSize = 0;
		try
		{
		if (m_isFinished || m_isDownloadFailed || m_isDisconnected)
		{
			return;
		}
			
			int n;
			PackFile current;
			while (blinker != null && CurrPackFile < rRes.size())
			{
				try {this.sleep(10);} catch (Exception e){}			
				current = rRes.elementAt(CurrPackFile);
						
				entryName = current.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") + "/" + current.getName(); 

				// Native libs go on device
				if(entryName.endsWith(".so"))
				{
					m_strOutputPath = GameInstaller.LIBS_PATH;
					GameInstaller.addNativeLib(current.getName());
				}
				// Synch file & folder creation between threads
				//if (!entryName.startsWith("/") && ("/" + entryName.substring(1)).equals(entryName))//GEORGE
					entryName = m_strOutputPath + "/" + entryName;
					
				long size_to_skip = 0;
				File file = new File(entryName.replace("//", "/"));
				if(file.exists())
				{
					size_to_skip = file.length();
				}
				else
				{
					size_to_skip = 0;
				}
				//m_DownloadedSize += size_to_skip;
				DBG("SimpleDownload", "entryName: " + entryName.replace("//", "/") + " Size: " + size_to_skip);
				//initialize(current.getURL(), size_to_skip);//george
				initialize("", size_to_skip);
				
				
				m_out = getOutputStream(entryName);
					DBG("SimpleDownload", "Downloading: " + entryName);
					
				if(entryName.endsWith(".so"))
				{
					(new File(m_strOutputPath)).createNewFile();
					GameInstaller.makeLibExecutable(m_strOutputPath);
				}

					while ((n = m_DIStream.read(m_temp, 0, FILE_READ_SIZE)) > -1)
					{
						//DBG("SimpleDownload", "Reading size: "+n);
						m_out.write(m_temp, 0, n);
						m_FileDownloadedSize += n;

						if (blinker == null)
						{
							m_isFinished = false;
							m_isDisconnected = true;
							break;					
						}
					}
					//DBG("SimpleDownload", "m_DownloadedSize = "+m_DownloadedSize+" filedownloadsiez: "+m_FileDownloadedSize);
					m_DownloadedSize += m_FileDownloadedSize;
					DBG("SimpleDownload", "m_DownloadedSize = "+m_DownloadedSize+" filedownloadsiez: "+m_FileDownloadedSize);
					m_FileDownloadedSize = 0;
					CurrPackFile++;
			}
				/*m_isDownloadFailed = true;
				m_temp = null;
				
				if(m_clientHTTP != null)
				{
					m_clientHTTP.close();
					m_clientHTTP = null;
				}*/

				m_out.close();

			
			
			m_DIStream.close();
			m_temp = null;
			
			if (blinker != null)
				m_isFinished = true;
				
			DBG("SimpleDownload", "Exit with size downloaded: " + m_DownloadedSize+" finished: "+m_isFinished);
			
			if(m_clientHTTP != null)
			{
				m_clientHTTP.close();
				m_clientHTTP = null;
			}
		}
		catch (SocketTimeoutException ste)
		{
			ERR_TOAST(ToastMessages.Section.RunSocketTimeoutError);
			m_clientHTTP.incrementConnectionTimeout();
			m_isDownloadFailed = true;
			DBG_EXCEPTION(ste);
		}
		catch (Exception e)
		{
			ERR_TOAST(ToastMessages.Section.RunException);
			m_isDownloadFailed = true;
			m_temp = null;
			
			if(m_clientHTTP != null)
			{
				m_clientHTTP.close();
				m_clientHTTP = null;
			}
			DBG ("SimpleDownload", "Exception: " + ", " + entryName + " message: " + e.getMessage());
			DBG_EXCEPTION(e);
		}
		/*}
		catch (SocketTimeoutException ste)
		{
			ERR_TOAST(ToastMessages.Section.RunSocketTimeoutError);
			m_clientHTTP.incrementConnectionTimeout();
			m_isDownloadFailed = true;
			DBG_EXCEPTION(ste);
		}
		catch (Exception e)
		{
			ERR_TOAST(ToastMessages.Section.RunException);
			m_isDownloadFailed = true;
			m_temp = null;
			
			if(m_clientHTTP != null)
			{
				m_clientHTTP.close();
				m_clientHTTP = null;
			}
			DBG ("SimpleDownload", "Exception: " + ", " + entryName + " message: " + e.getMessage());
			DBG_EXCEPTION(e);
		}*/
		isTerimanted = true;
	}
	public void retryDownload()
	{
		m_isFinished = false;
		m_isDisconnected = false;
		m_isDownloadFailed = false;
	}
	/**
	 * Checks if the download is failed
	 * @return true if the download is failed
	 */
	public boolean isDownloadFailed()
	{
		return m_isDownloadFailed;
	}
	/**
	 * Checks if the download is finished
	 * @return true if the download is finished
	 */
	public boolean isFinished()
	{
		return m_isFinished;
	}
	/**
	 * Checks if the download finished, but is uncompleted
	 * @return if the download is uncompleted
	 */
	public boolean isUncompleted()
	{
		return (!m_isFinished && m_isDisconnected);
	}
	/**
	 * Gets the current downloaded size of the SimpleDownload
	 * @return the current downloaded size of the section
	 */
	public long getSize()
	{
		return m_DownloadedSize + m_FileDownloadedSize;
	}
	
	public void ResetProgress()
	{
		m_DownloadedSize = m_FileDownloadedSize = 0;
	}
	/**
	 * Gets the required resources for the current section
	 * @return the required resources for the current section
	 */
	public Vector<PackFile> getRequiredResources()
	{
		return rRes;
	}
	/**
	 * Gets the section's output path
	 * @return the output path
	 */
	public String getOutputPath()
	{
		return m_strOutputPath;
	}
	/**
	 * Gets the section's Data link
	 * @return the data link
	 */
	public String getDataLink()
	{
		return m_strDataLink;
	}
	/**
	 * Retrives the output stream
	 * @return the output stream
	 */
	private FileOutputStream getOutputStream(String filename)
	{
		try
		{
			File file = new File(filename);
			String path;
			
			// Check if folder
			if (filename.endsWith("/"))
				path = filename;
			else
				path = file.getParent();
			
			// Create the path and .nomedia file
			if(path != null)
			{
				File parent = new File(path);
				if (!parent.exists())
				{
					parent.mkdirs();
					parent = null;
				}
				File nomedia = new File(path + "/.nomedia");
				if (!nomedia.exists())
					nomedia.createNewFile();
				nomedia = null;
			}
			
			// Create the stream
			if (!filename.endsWith("/"))
				return new FileOutputStream(file, true);
		}
		catch (Exception e)
		{
			ERR_TOAST(ToastMessages.Section.getOutputStreamError);
			DBG_EXCEPTION(e);
		}
		
		return null;
	}
}