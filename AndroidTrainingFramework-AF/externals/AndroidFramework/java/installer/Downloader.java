//#if USE_DOWNLOAD_MANAGER
package APP_PACKAGE.installer.utils;

import android.util.Log;

import java.util.Vector;
import java.util.ArrayList;
import APP_PACKAGE.GLUtils.ZipFile;
import APP_PACKAGE.installer.GameInstaller;
import APP_PACKAGE.installer.ToastMessages;

import java.net.URL;
public class Downloader implements Defs
{		
	public String m_strDataLink = "";
	public String m_strOutputPath = "";	
	
	public long m_RequiredSize = 0;
	public int m_lGlobalOffset = 0;
	
	public boolean m_isDownloadCompleted;
	public boolean m_isDownloadFailed;
	public static boolean m_isPaused;
	
	ArrayList<Section> m_pSections;
	
	Vector<PackFile> m_RequiredResources;
	private int m_TotalSections = 0;
	private int m_UsedThreads = 0;
	private final int MIN_SIZE_PER_SECTION = 1<<20;
	
	public Downloader(String strDataLink, String strOutputPath, Vector<PackFile> rRes, int gbOff, long reqSize)
	{
		m_lGlobalOffset = gbOff;
		m_RequiredResources = rRes;
		m_strDataLink = strDataLink;
		m_strOutputPath = strOutputPath;		
		m_RequiredSize = reqSize;
		
		try
		{
			initialize();
		}
		catch (Exception e)
		{
			ERR("Downloader","Initialize error!");
			DBG_EXCEPTION(e);
			ERR_TOAST(ToastMessages.Downloader.InitializeError);
			m_isDownloadFailed = true;
		}		
	}

	public void retryDownload( Vector<PackFile> rRes)
	{
		m_RequiredResources = rRes;
		m_isDownloadFailed = false;
		//m_CurrentFileIdx = 0;
		try
		{
			initialize();
		}
		catch (Exception e)
		{
			ERR("GameInstaller","Retry Initialize error!");
			DBG_EXCEPTION(e);
			ERR_TOAST(ToastMessages.Downloader.InitializeRetryError);
			m_isDownloadFailed = true;
		}		
	}
	
	public void setGlobalOffset(int gbOff)
	{
		m_lGlobalOffset = gbOff;
	}
	
	
	public void initialize() throws Exception
	{	
		if(m_RequiredResources != null && m_RequiredResources.size() > 0)
		{
			setFileForDownload();
			
			m_isDownloadCompleted = false;
			m_isPaused = false;	
		}
		else
			throw new Exception();
	}
	
	public void setFileForDownload()
	{
	
		m_UsedThreads = 0;
		long sectionSize = m_RequiredSize / MAX_DOWNLOAD_THREADS;
		
		if (MAX_SECTION_SIZE * 1024 * 1024 < sectionSize)
			sectionSize = MAX_SECTION_SIZE * 1024 * 1024;
		
		splitSections(sectionSize);
	}
	
	public String getOutputFileName()
	{
		return m_strOutputPath +  "/" + JOINED_FILE_NAME;
	}

	public void start()
	{
		if (m_isDownloadFailed)
		{
			return;
		}
	
		try
		{		
			int threadsToStart = (m_TotalSections < MAX_DOWNLOAD_THREADS)? m_TotalSections:MAX_DOWNLOAD_THREADS;
			
			for (int i = 0; i < threadsToStart; i++)
			{
				DBG("Downloader", "Start new thread: " + (m_UsedThreads + 1)  + " out of " + m_TotalSections + " sections needed");
				m_pSections.get(i).start();
				m_UsedThreads++;
			}
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
			ERR_TOAST(ToastMessages.Downloader.StartThreadError);
			m_isDownloadFailed = true;
		}
	}
	
	public void update()
	{
		if (m_isDownloadFailed)
		{
			this.stopDownload();
			return;
		}
		int finishedThreads = 0;
		int failedSections = 0;
		for (int i = 0; i < m_TotalSections; i++)
		{
			if (m_pSections.get(i).isUncompleted())
			{
				if (m_pSections.get(i).hasRetriesLeft())
				{
					m_pSections.get(i).stopThread();
					m_pSections.set(i, new Section(m_pSections.get(i)));
					m_pSections.get(i).start();
					DBG("Downloader", "Restart Section: " + i  + " out of " + m_TotalSections + " sections needed");
				}
				else
				{
					failedSections++;
				}
			}
			else if (m_pSections.get(i).isFinished())
			{
				finishedThreads++;
			}
		}
		if (failedSections > 0 && failedSections + finishedThreads == m_TotalSections)
		{
			m_isDownloadFailed = true;
			this.stopDownload();
			return;
		}
		
		if (m_UsedThreads - (failedSections + finishedThreads) < MAX_DOWNLOAD_THREADS && m_UsedThreads < m_TotalSections)
		{
			DBG("Downloader", "Start new thread: " + (m_UsedThreads + 1)  + " out of " + m_TotalSections + " sections needed");
			m_pSections.get(m_UsedThreads).start();
			m_UsedThreads++;
		}
		
		if ((failedSections + finishedThreads) < m_TotalSections)
			return;

		DBG("Downloader","file Downloading completed!");
		m_isDownloadCompleted = true;
	}
	
	public void stopDownload()
	{
		try
		{
			for (int i = 0; i < m_pSections.size(); i++)
			{
				DBG("Downloader", "Stop section thread: " + (i + 1)  + " out of " + m_pSections.size());
				if (m_pSections.get(i) != null)
					m_pSections.get(i).stopThread();
			}
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
			ERR_TOAST(ToastMessages.Downloader.StopError);
			m_isDownloadFailed = true;
		}
	}
	
	public long getDownloadedSize()
	{
		long totalSize = 0;
		
		for (int i = 0; i < m_TotalSections; i++)
		{
			totalSize += m_pSections.get(i).getSize();
		}

		return totalSize;
	}
	
	public int getDownloadedFiles()
	{
		int totalFiles = 0;
		for (int i = 0; i < m_TotalSections; i++)
		{
			totalFiles += m_pSections.get(i).getFilesCompleted();
		}
		
		return totalFiles;
	}
	
	public void pause()
	{
		m_isPaused = true;
	}
	
	public void resume()
	{
		m_isPaused = false;
	}
	public void splitSections(long maxChunkSize)
	{
		m_pSections = new ArrayList<Section>(); 	
		m_TotalSections = 0;

		if (m_RequiredResources.size() == 0)
			return;
		
		Section tmpSection;
		Vector<PackFile> sectionPackFiles = new Vector<PackFile>();
		
		int lastID = m_RequiredResources.get(0).getID();
		int totalSizeCompressed = m_RequiredResources.get(0).getZipLength();
		sectionPackFiles.add(m_RequiredResources.get(0));
		
		// final HttpClient chttp = new HttpClient();
		// m_strDataLink = chttp.GetRedirectedUrl(m_strDataLink);
		// chttp.close();
		
		String name = "";
		// Horrible code: Find sequential chunks of needed files
		for (int i=1; i<m_RequiredResources.size(); i++)
		{
			// Files are sequential
			if (lastID + 1 == m_RequiredResources.get(i).getID() && totalSizeCompressed < maxChunkSize)
			{
				sectionPackFiles.add(m_RequiredResources.get(i));
				totalSizeCompressed += m_RequiredResources.get(i).getZipLength();
			}
			else
			{
				tmpSection = new Section(m_strDataLink, m_strOutputPath, sectionPackFiles, m_lGlobalOffset, m_pSections.size());
				m_pSections.add(tmpSection);
				m_TotalSections++;
				
				sectionPackFiles = new Vector<PackFile>();
				sectionPackFiles.add(m_RequiredResources.get(i));
				totalSizeCompressed = m_RequiredResources.get(i).getZipLength();
			}
			lastID = m_RequiredResources.get(i).getID();
		}
		
		tmpSection = new Section(m_strDataLink, m_strOutputPath, sectionPackFiles, m_lGlobalOffset, m_pSections.size());
		m_pSections.add(tmpSection);
		m_TotalSections++;
		
		
		for (int w=0; w < m_TotalSections; w++)
		{
			DBG("Downloader", "Section[" + w + "]:");
			for (int y=0; y < m_pSections.get(w).getRequiredResources().size(); y++)
			{
				DBG("Downloader", "Section[" + w + "]:" + m_pSections.get(w).getRequiredResources().get(y).getName() + ", " + m_pSections.get(w).getRequiredResources().get(y).getZipLength() + ", " + m_pSections.get(w).getRequiredResources().get(y).getOffset() + ", " + m_pSections.get(w).getRequiredResources().get(y).getLength());
			}
		}
	}
}
//#endif 