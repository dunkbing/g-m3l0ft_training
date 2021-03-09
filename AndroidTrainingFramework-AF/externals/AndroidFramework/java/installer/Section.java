//#if USE_DOWNLOAD_MANAGER
package APP_PACKAGE.installer.utils;

import android.util.Log;

import android.os.Build;
import java.io.DataOutputStream;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
#if !USE_LZMA
import java.util.zip.DataFormatException;
#endif
import java.util.Vector;
import APP_PACKAGE.installer.Utils;
import APP_PACKAGE.installer.GameInstaller;
import APP_PACKAGE.installer.ToastMessages;


public class Section extends Thread
{
	private			final	int 					MAX_SECTION_RETRY		= 3;
	private			final	int 					SECTION_RETRY_SLEEP		= 3000; // 3 * 1000
	private			final	int 					FILE_READ_SIZE			= 32768; // 32 * 1024
	public			final	static long				m_SPLIT_SIZE			= SPLIT_SIZE * 1024;
	private	static 			Object 					lock 					= new Object();
	private	static 			Object 					closeLock 				= new Object();
	private 				Thread 					blinker					= null;
	private 				RandomAccessFile 		m_out 					= null;
	private 				HttpClient 				m_clientHTTP			= null;
	private 				boolean 				m_isFinished			= false;
	private 				boolean 				m_isDisconnected		= false;
	private 				long 					m_DownloadedSize 		= 0;
	private 				long 					m_FileDownloadedSize 	= 0;
	private 				long 					m_iGlobalOffset 		= 0;
	private 				int 					m_iSectionID 			= 0;
	private 				int 					m_iFilesCompleted 		= 0;
	private 				int 					m_retryCount 			= 0;
	private 				String 					m_strDataLink			= "";
	private 				String 					m_strOutputPath			= "";
	private 				Vector<PackFile> 		m_RequiredResources		= null;
	
#if !USE_LZMA
	private 				ZipInputStreamCustom 	m_zin;
	private 				ZipEntry 				m_zipEntry;
	private					byte[]					m_temp 					= null;
	private 				DataInputStreamCustom 	m_DIStream;
#else
	private 				InputStream 			m_DIStream;
	private 				SevenZip.Compression.LZMA.Decoder 	decoder;
#endif
	
	/**
	 * Section's Constructor
	 * @param strDataLink 	the data pack's link
	 * @param strOutputPath the output path
	 * @param rRes 			the resources pack files vector
	 * @param globalOffset 	the global offset in the data pack
	 * @param sectionID 	the section's ID
	 */
	public Section(String strDataLink, String strOutputPath, Vector<PackFile> rRes, long globalOffset, int sectionID)
	{	
		m_strDataLink = strDataLink;
		m_strOutputPath = strOutputPath;
		m_iGlobalOffset = globalOffset;
		m_iSectionID = sectionID;
		m_RequiredResources = rRes;
		m_retryCount = 0;
		m_isFinished = false;
		m_isDisconnected = false;
	}
	/**
	 * Section's Constructor from another Section's data
	 * @param s the old section's information
	 */
	public Section(Section s)
	{	
		m_strDataLink = s.getDataLink();
		m_strOutputPath = s.getOutputPath();
		m_iGlobalOffset = s.getGlobalOffset();
		m_iSectionID = s.getSectionsID();
		m_RequiredResources = s.getRequiredResources();
		m_retryCount = s.getRetriesCount() + 1;
		m_iFilesCompleted = s.getFilesCompleted();
		m_isFinished = false;
		m_isDisconnected = false;
		m_DownloadedSize = s.getSize();
		DBG("Section","NEW SECTION WITH SIZE:"+m_FileDownloadedSize);
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
	public void initialize() throws Exception
	{		
		try
		{
			m_clientHTTP = new HttpClient();
			InputStream in = null;
			try
			{
				if (m_iFilesCompleted <  m_RequiredResources.size())
					in = m_clientHTTP.getInputStream(m_strDataLink, m_RequiredResources.get(m_iFilesCompleted).getOffset(), m_iGlobalOffset, 0);
				m_DownloadedSize = m_RequiredResources.get(m_iFilesCompleted).getOffset() - m_RequiredResources.get(0).getOffset();
				DBG("Section", "Section ID " + m_iSectionID + " creating stream at: " + m_RequiredResources.get(0).getOffset() + ", " + m_iGlobalOffset + "at url: "+m_strDataLink);
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
            #if !USE_LZMA
               	m_DIStream = new DataInputStreamCustom(in);
	    	#else
	    		m_DIStream = in;
	    	#endif
            else
             	throw new Exception();
				
	    	if(m_DIStream == null)
             	throw new Exception();
		#if !USE_LZMA
			m_temp = new byte[FILE_READ_SIZE];
		#endif
			
		}
		catch (SocketTimeoutException ste)
		{
			ERR_TOAST(ToastMessages.Section.InitializeSocketError);
			m_clientHTTP.incrementConnectionTimeout();
		}
		catch (Exception e)
		{
			ERR_TOAST(ToastMessages.Section.InitializeError);
			DBG_EXCEPTION(e);
			throw new Exception();
		}
	}
	/**
	 * The Sections run loop where the data is downloaded
	 */
	public void run()
	{
		boolean isStreaming = false;
		String entryName = "";
		
		Thread thisThread = Thread.currentThread();
		blinker = Thread.currentThread();
		int splitNumber = 0;
		
		try
		{
			initialize();
			
			if (m_isFinished)
			{
				return;
			}
			
			int n;
			PackFile CurrPackFile = null;
			
			DBG("Section", "Section ID " + m_iSectionID + " running with " + m_RequiredResources.size() + " files to download.");
			while (blinker == thisThread && m_RequiredResources.size() > m_iFilesCompleted)
			{
				try {this.sleep(10);} catch (Exception e){}
				splitNumber = 0;
				
				CurrPackFile = m_RequiredResources.get(m_iFilesCompleted);
			#if !USE_LZMA
				m_DIStream.setMax(CurrPackFile.getZipLength());
				m_zin = new ZipInputStreamCustom(m_DIStream);
				m_zipEntry = m_zin.getNextEntry();
				
				if (m_zipEntry == null)
				{
					ERR("Section", m_iSectionID + "zipEntry: " + ((m_zipEntry != null) ? m_zipEntry : "null") + " pos: " + m_DIStream.getPosition() + " MaxSet: "+ CurrPackFile.getZipLength() + " DownloadedSize: " + m_DownloadedSize);
					ERR_TOAST(ToastMessages.Section.RunZipEntryNull);
					throw new DataFormatException("m_zipEntry = null");
				}
				else
					DBG("Section", m_iSectionID + "zipEntry: " + ((m_zipEntry != null) ? m_zipEntry : "null") + " pos: " + m_DIStream.getPosition() + " MaxSet: "+ CurrPackFile.getZipLength() + " DownloadedSize: " + m_DownloadedSize);
			#endif				
				if (isStreaming)
					continue;
				
				isStreaming = true;
				entryName = CurrPackFile.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") + "/" + CurrPackFile.getName(); 
			#if !USE_LZMA
				DBG("Section", "Section ID " + m_iSectionID + " extracting: " + entryName + ", " + m_zipEntry);
			#else
				DBG("Section", "Section ID " + m_iSectionID + " extracting: " + entryName);
			#endif
				if (entryName.contains(".split_"))
				{
					splitNumber = Integer.parseInt(entryName.substring(entryName.lastIndexOf("_") + 1));
					entryName = entryName.substring(0, entryName.lastIndexOf('.'));
				}
				String path = m_strOutputPath + "/" + entryName;

				// Native libs go on device
				if(entryName.endsWith(".so"))
				{
					DBG("Section", "adding native lib "+CurrPackFile.getName()+" entry name: "+entryName);
					String lib_name = CurrPackFile.getName();
					
					if (CurrPackFile.getName().contains(".split_"))
						lib_name = lib_name.substring(0, lib_name.lastIndexOf('.'));
					
					path = GameInstaller.LIBS_PATH + lib_name;
					GameInstaller.addNativeLib(lib_name);
				}
				// Synch file & folder creation between threads
				synchronized (lock)
				{
					m_out = getOutputStream(path, splitNumber);
				}

				if(entryName.endsWith(".so"))
				{
					(new File(path)).createNewFile();
					GameInstaller.makeLibExecutable(path);
				}

				// Folder?
				if (m_out == null)
				{
				#if !USE_LZMA
					m_zin.closeEntry();
					m_zipEntry = m_zin.getNextEntry();
					DBG("Section", "zipEntry: " + ((m_zipEntry != null) ? m_zipEntry : "null"));
					if (m_zipEntry == null)
					{
						ERR_TOAST(ToastMessages.Section.RunZipEntryNull);
						throw new DataFormatException("m_zipEntry = null");
					}
				#endif
					isStreaming = false;
					continue;
				}

			#if !USE_LZMA
				while ((n = m_zin.read(m_temp, 0, FILE_READ_SIZE)) > -1)
				{
					m_out.write(m_temp, 0, n);
					m_FileDownloadedSize = m_zin.getBytesRead();
				}
				m_DownloadedSize += m_FileDownloadedSize;
				m_FileDownloadedSize = 0;
			#else
				int propertiesSize = 5;
				byte[] properties = new byte[propertiesSize];
				if (m_DIStream.read(properties, 0, propertiesSize) != propertiesSize)
				{
					ERR_TOAST(ToastMessages.Section.RunLzmaFileTooShort);
					throw new Exception("input .lzma file is too short");
				}
				decoder = new SevenZip.Compression.LZMA.Decoder();
				int dictionarySize = 0;
				for (int i = 0; i < 4; i++)
					dictionarySize += ((int)(properties[1 + i]) & 0xFF) << (i * 8);
				DBG("Section", "dictionarySize: "+ dictionarySize);
				
				long outSize = 0;
				for (int j = 0; j < 8; j++)
				{
					int v = m_DIStream.read();
					if (v < 0)
					{
						ERR_TOAST(ToastMessages.Section.RunLzmaCantReadSize);
						throw new Exception("Can't read stream size");
					}
					outSize |= ((long)v) << (8 * j);
				}
				DBG("Section", "Section ID " + m_iSectionID + " outSize: " + outSize);
				
				if (!decoder.SetDecoderProperties(properties, outSize))
				{
					ERR_TOAST(ToastMessages.Section.RunLzmaIncorrectProperties);
					throw new Exception("Incorrect stream properties");
				}
					
				if (!decoder.Code(m_DIStream, m_out, outSize))
				{
					ERR_TOAST(ToastMessages.Section.RunLzmaDecodeError);
					throw new Exception("Error in data stream");
				}
				
				m_DownloadedSize += decoder.Size + 5 + 8;
				decoder.Size = 0;
				DBG("Section", "DNLD: "+ m_DownloadedSize);
			#endif
				
				if (CurrPackFile.getName().contains(".split_"))
				{
					if( (CurrPackFile.getUnsplittedLength() / m_SPLIT_SIZE ) + 1 == splitNumber)
					{
						m_out.setLength(CurrPackFile.getUnsplittedLength());
					}
				}
				
				Utils.markAsSaved(m_RequiredResources.get(m_iFilesCompleted));
				m_iFilesCompleted++;
				m_out.close();
				
			#if !USE_LZMA
				m_zin.closeEntry();
				m_DIStream.skipToMax();
				m_DIStream.resetPos();
			#endif
				isStreaming = false;
			} //while
			DBG("Section", "Exit unzip loop: " + m_iFilesCompleted + "," + m_DownloadedSize);

			new Thread(new Runnable()
			{
				public void run()
				{
					synchronized (closeLock)
					{
						try
						{
							m_DIStream.close();
						}catch(IOException ioe){}
					}
				}
			}).start();

		#if !USE_LZMA
			m_zin = null;
			m_temp = null;
		#endif
			m_isFinished = true;
			
			if(m_clientHTTP != null)
			{
				try { Thread.sleep(50); } catch (Exception e) {}
				synchronized (closeLock)
				{
					m_clientHTTP.close();
					m_clientHTTP = null;
				}
			}
		}
		catch (SocketTimeoutException ste)
		{
			ERR_TOAST(ToastMessages.Section.RunSocketTimeoutError);
			m_clientHTTP.incrementConnectionTimeout();
			m_isDisconnected = true;
			DBG_EXCEPTION(ste);
		}
		catch (Exception e)
		{
			ERR_TOAST(ToastMessages.Section.RunException);
			m_isDisconnected = true;
		#if !USE_LZMA
			m_temp = null;
		#endif
			
			if(m_clientHTTP != null)
			{
				m_clientHTTP.close();
				m_clientHTTP = null;
			}
			DBG ("Section", "Exception: " + m_iSectionID + ", " + entryName + " message: " + e.getMessage());
			DBG_EXCEPTION(e);
			
			m_DownloadedSize += m_FileDownloadedSize;
		}
		WARN("Section", m_iSectionID + " finished.");
		m_isDisconnected = true;
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
	 * Checks if the download has a number of retries left
	 * @return true if the section has retries left 
	 */
	public boolean hasRetriesLeft()
	{
		return m_retryCount < MAX_SECTION_RETRY;
	}
	/**
	 * Gets the current downloaded size of the Section
	 * @return the current downloaded size of the section
	 */
	public long getSize()
	{
		#if !USE_LZMA
			return m_DownloadedSize + m_FileDownloadedSize;
		#else
			if (decoder != null)
				return m_DownloadedSize + decoder.Size;
			return 0;
		#endif
	}
	/**
	 * Gets the number of finished downloaded files
	 * @return the number of currently downloaded files
	 */
	public int getFilesCompleted()
	{
		return m_iFilesCompleted;
	}
	/**
	 * Gets the number of retries attempted for the current data pack
	 * @return the number of retries attempted
	 */
	public int getRetriesCount()
	{
		return m_retryCount;
	}
	/**
	 * Gets the required resources for the current section
	 * @return the required resources for the current section
	 */
	public Vector<PackFile> getRequiredResources()
	{
		return m_RequiredResources;
	}
	/**
	 * Gets the section's ID
	 * @return the section's ID
	 */
	public int getSectionsID()
	{
		return m_iSectionID;
	}
	/**
	 * Gets the global offset of the section
	 * @return the section's global offset
	 */
	public long getGlobalOffset()
	{
		return m_iGlobalOffset;
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
	private RandomAccessFile getOutputStream(String filename, int splitNumber)
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
			{
				RandomAccessFile raf = new RandomAccessFile(file, "rw");
				if (splitNumber > 0)
				{
					if (!file.exists())
					{
						if (raf.length() < splitNumber * m_SPLIT_SIZE)
							raf.setLength(splitNumber * m_SPLIT_SIZE);
					}
					raf.seek(m_SPLIT_SIZE * (splitNumber - 1));
				}
				return raf;
			}
		}
		catch (Exception e)
		{
			ERR_TOAST(ToastMessages.Section.getOutputStreamError);
			DBG_EXCEPTION(e);
		}
		
		return null;
	}
}
//#endif