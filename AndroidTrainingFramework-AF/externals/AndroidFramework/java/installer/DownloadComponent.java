
package APP_PACKAGE.installer.utils;

import android.content.Context;

//import android.util.Pair;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.Vector;
import java.util.ArrayList;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.HttpURLConnection;

import APP_PACKAGE.installer.utils.CRC;
//import APP_PACKAGE.installer.utils.GiSettings;
import APP_PACKAGE.installer.utils.HttpClient;
import APP_PACKAGE.installer.utils.PackFile;
import APP_PACKAGE.installer.utils.PackFileReader;
import APP_PACKAGE.installer.utils.SimpleDownload;
import APP_PACKAGE.installer.utils.Downloader;

import APP_PACKAGE.installer.GameInstaller;

public class DownloadComponent
{
	private final int BUFFER_SIZE			= 32 * 1024;

	private String mName = "";
	
	public String mServerFilename = "";
	
	private long mServerFilesize = -1;
	
	private String mServerFileChecksum = "";
	
	// private DataInputStream mDIStream;
	
	private String mServerURL = "";

	private String mRedirUrl = null;
	
	private String mVersion = "";
	
	private boolean m_isVersionUpdated = false;
	
	private boolean mIsGenericBuild = false;
	
	private String m_Infofilename = "";
	
	private Vector<PackFile> mPackFileInfo;
	
	private long mPack_biggestFile = -1;
	
	private int mPack_NoFiles = 0;
	
	Vector<PackFile> mRequiredResources = new Vector<PackFile>();
	
	private HttpClient mClientHTTP;
	
	private HttpClient mClientHTTPForUpdate;
	
	int mInfoFileLength;
	
	private SimpleDownload m_pSimpleDownloader;
	private Downloader m_pDownloader;
	
	long mRequiredSize = 0;
	//long mbAvailable = 0;
	long mbRequired = 0;
	long mbRequiredJustArhive = 0;
	long mTotalSize = 0;
	long mDownloadedSize = 0;
	long m_iRealRequiredSize = 0;
	long m_SimpleDownloadSizeToSkip = 0;
	boolean m_isCompleted = false;
		
	int mImportance = 1;
	boolean hasFixedPath = false;
	
	public DownloadComponent(String url, String name)
	{
		DBG("DownloadComponent","Creating component with name: "+name+" version: "+mVersion);
		mServerURL = url;
		mName = name;
		
		m_Infofilename = GameInstaller.SaveFolder + "/pack"+ mName + ".info";
	}
	
	public DownloadComponent(String url, String name, String version, String checksum)
	{
		DBG("DownloadComponent","Creating component with name: "+name+" version: "+version+" and checksum: "+checksum);
		mServerURL = url;
		mName = name;
		mVersion = version;
		mServerFileChecksum = checksum;
		
		m_Infofilename = GameInstaller.SaveFolder + "/pack"+ mName + ".info";
	}
	
	//ussualyy a gef component constructor
	public DownloadComponent(String url, String name, String version, String server_filename, long server_file_size, boolean have_fixed_path)
	{
		DBG("DownloadComponent","Creating component with name: "+name+" version: "+version+" filename: "+server_filename);
		mServerURL = url;
		mName = name;
		mVersion = version;
		mServerFilename = server_filename;
		mServerFilesize = server_file_size;
		hasFixedPath = have_fixed_path;
		
		m_Infofilename = GameInstaller.SaveFolder + "/pack"+ mName + ".info";
	}
	public String getName()
	{
		return mName;
	}
	public boolean isGenericBuild()
	{
		return mIsGenericBuild;
	}
	
	public String getInfoFilename()
	{
		return m_Infofilename;
	}
	
	public String getVersion()
	{
		return mVersion;
	}
	
	public void setVersion(String value)
	{
		mVersion = value;
	}
	
	public boolean isVersionUpdated()
	{
		return m_isVersionUpdated;
	}
	
	public void UpdateVersion()
	{
		DBG("DownloadComponent","UPDATE VERSION with version: "+getVersionInt());
		m_isVersionUpdated = true;
	}
	
	public int getVersionInt()
	{
		try{	
			return Integer.parseInt(mVersion);
		}catch(NumberFormatException ex){return -1;}
	}
	
	public long getRealRequiredSize()
	{
		return m_iRealRequiredSize;
	}
	
	public long getMBRequiredJustArhive()
	{
		return mbRequiredJustArhive;
	}
	
	public long getMBRequired()
	{
		return mbRequired;
	}
	
	public int getRequiredResourcesSize()
	{
		return mRequiredResources.size();
	}
	
	public int getInfoFileLength()
	{
		return (mPackFileInfo == null) ? 0 : mPackFileInfo.size();
	}
	
	public long getTotalSize()
	{
		return mTotalSize;
	}
	
	public long getDownloadedSize()
	{
		return mDownloadedSize;
	}
	
	public String getServerUrl()
	{
		return mServerURL;
	}
	
	public void setServerUrl(String url)
	{
		mServerURL = url;
	}
	
	public void setImportance(int value)
	{
		mImportance = value;
	}
	
	public void setServerFileSize(long value)
	{
		mServerFilesize = value;
	}
	
	public void setServerFileName(String value)
	{
		mServerFilename = value;
	}
	
	public boolean fixedPath()
	{
		return hasFixedPath;
	}
	
	public void setFixedPath(boolean value)
	{
		hasFixedPath = value;
	}
	
	public long getBiggestFileSize()
	{
		return mPack_biggestFile;
	}
	
	public int getFilesNo()
	{
		return mPack_NoFiles;
	}
	
	public boolean haveFile(String filename)
	{
		if(mPackFileInfo != null)
			for(int i = 0; i < mPackFileInfo.size(); i++)
			{
				DBG("DownloadComponent","have file component name: "+mPackFileInfo.get(i).getName());
				if(mPackFileInfo.get(i).getName().equals(filename))
					return true;
			}
		return false;
	}
	
	private DataInputStream generateDataStreamFromConnection(HttpURLConnection urlConn)
	{
		try 
		{
			urlConn.setConnectTimeout(60*1000);
			urlConn.connect();
			
			DataInputStream dis = new DataInputStream(urlConn.getInputStream());
			return dis;
		}catch(Exception e){}
		return null;
	}
	
	private boolean generateDataStream()
	{
		DBG("DownloadComponent","getdatastream url = : " + mServerURL);
		InputStream in = null;
		try
		{
			if(mClientHTTP == null)
			{
				mClientHTTP = new HttpClient();
			}
			else
			{
				mClientHTTP.close();
			}
			
			in = mClientHTTP.getInputStream(mServerURL);
			
			if(in == null)
			{
				//mStatus = STATUS_FILE_NOT_FOUND;
				//ERR_TOAST(ToastMessages.GameInstaller_getDataStream.StatusFileNotFound);
				
				return false;
			}
			
			// if (mDIStream != null)
			// {
				// mDIStream.close();
				// mDIStream = null;
			// }
			
			// mDIStream = new DataInputStream(in);
			
			return true;
		}
		catch (SocketTimeoutException ste)
		{
			//ERR_TOAST(ToastMessages.GameInstaller_getDataStream.SocketTimeoutException);
			mClientHTTP.incrementConnectionTimeout();
		}
		catch (FileNotFoundException fnf)
		{
			//ERR_TOAST(ToastMessages.GameInstaller_getDataStream.FileNotFoundException);
			DBG_EXCEPTION(fnf);
			//mStatus = STATUS_FILE_NOT_FOUND;
		}
		catch (Exception e)
		{
			//ERR_TOAST(ToastMessages.GameInstaller_getDataStream.Exception);
			DBG_EXCEPTION(e);
			//mStatus = STATUS_NETWORK_UNAVALIABLE;
		}
		//destroyObjects();
		return false;
	}
	public void readResources(Context context)
	{
		try
		{
			DBG("DownloadComponent", "read res "+m_Infofilename);
			PackFileReader loadPack = new PackFileReader(context);
			mPackFileInfo 		= loadPack.read(m_Infofilename);
			mPack_biggestFile 	= loadPack.getBiggestSize();
			mPack_NoFiles		= mPackFileInfo.size();
		} catch(IOException e) {DBG_EXCEPTION(e);DBG("DownloadComponent", "Exception reading fileinfo");}
		DBG("DownloadComponent", "Done reading");
	}

	public boolean getRequiredResourcesValues(boolean overwrite_info)
	{

		if(mServerFilename.equals(""))
		{
			if(mClientHTTP == null)
			// if(mDIStream == null)
			{
				if(!generateDataStream())
				{
					DBG("DownloadComponent", "getRequiredResourcesValues(), Could not generate data stream for " + mName + " component.");
					return false;
				}
			}
			
			try
			{
				// //URL url = new URL(mServerURL);
				if(mRedirUrl == null)
				{
					mRedirUrl = mClientHTTP.GetRedirectedUrl(mServerURL);
				}
				// HttpClient cl = new HttpClient();
				// cl.getInputStream(redir_url);
				// long conn_size = cl.getFileSize();
				// cl.close();
								
				URL mRedirURL = new URL(mRedirUrl.replace(" ", "%20"));
				final HttpURLConnection urlConn = (HttpURLConnection)mRedirURL.openConnection();
				urlConn.setConnectTimeout(60*1000);
				urlConn.connect();
				
				long conn_size = -1;
				try{
					conn_size = Long.parseLong(urlConn.getHeaderField("Content-Length"));
				}catch(Exception e)
				{
					conn_size = -1;
				}
				
				if(conn_size == -1)
				{
					try
					{
						String url = mRedirUrl.replace(" ", "%20");
						DBG("DownloadComponent", "conn_size is -1, let's try with http 1.0 for: " + url);
						org.apache.http.client.HttpClient client = new org.apache.http.impl.client.DefaultHttpClient();
						client.getParams().setParameter("http.protocol.version", org.apache.http.HttpVersion.HTTP_1_0);
						org.apache.http.client.methods.HttpGet getMethod = new org.apache.http.client.methods.HttpGet((url));
						org.apache.http.HttpResponse httpResponse = client.execute(getMethod);
						conn_size = httpResponse.getEntity().getContentLength();
						DBG("DownloadComponent", "File size is:" + conn_size);
					}
					catch(Exception ex){
						DBG_EXCEPTION(ex);
						conn_size = -1;
					}
				}

				new Thread(new Runnable() {
					
					@Override
					public void run() {
						urlConn.disconnect();						
					}
				}).start();
				
				if(conn_size > 0)
				{
					int slashIndex = mRedirUrl.lastIndexOf('/');
					//int dotIndex = redir_url.lastIndexOf('.', slashIndex);
					mServerFilename = mRedirUrl.substring(slashIndex + 1);
					mServerFilesize = conn_size;

					if(mServerFilesize <= 0)
						return false;
				}else
				if(mImportance == 1)
					return false;
				else
				if(mImportance == 0)
					return true;
			}catch(IOException ex){DBG_EXCEPTION(ex);return false;}
			
		}
		DBG("DownloadComponent", "url filename to download: "+mServerFilename);
		FileOutputStream fOut = null;
		try
		{
			File packFile = new File(m_Infofilename);
			if(packFile.exists())
			//if(overwrite_info)
			{
				packFile.delete();
			}
			//else
			{
			//	DBG("DownloadComponent", "don't overwirte the info file");
			//	return true;
			}
			packFile.createNewFile();
			packFile = null;
			
			fOut = new FileOutputStream(m_Infofilename);
		}catch(IOException ex){DBG_EXCEPTION(ex);return false;}
				
		if(mServerFilename.contains(".amz") || mServerFilename.contains(".jar"))
		{
			try
			{
				// if(mDIStream == null)
				// {
					// if(!generateDataStream())
					// {
						// DBG("DownloadComponent", "getRequiredResourcesValues(), Could not generate data stream for " + mName + " component.");
						// return false;
					// }
				// }
				
				final HttpURLConnection nHttpUrlConn = (HttpURLConnection)new URL(mServerURL).openConnection();
				final DataInputStream nDIStream = generateDataStreamFromConnection(nHttpUrlConn);
				if( nDIStream == null)
				{
					DBG("DownloadComponent", "getRequiredResourcesValues(), Could not generate data stream for " + mName + " component.");
					return false;
				}
				DBG("DownloadComponent", "We have a amz downloader");
				mInfoFileLength = (int)nDIStream.readInt();
				// mInfoFileLength = (int)mDIStream.readInt();
				
				//DBG("DownloadComponent", "getRequiredResourcesValues(), mInfoFileLength = " + mInfoFileLength +" m_Infofilename: "+ m_Infofilename);
				int readSize = 0;
				
				while(readSize < mInfoFileLength)
				{
					DBG("DownloadComponent", "getRequiredResourcesValues(), reading packet...");
					int section  = mInfoFileLength - readSize;
					if(section > BUFFER_SIZE)
						section = BUFFER_SIZE;
					byte[] tmpByte = new byte[section];
					// mDIStream.readFully(tmpByte);
					nDIStream.readFully(tmpByte);
					fOut.write(tmpByte);
					fOut.flush();
					readSize += section;
					tmpByte = null;
				}
				fOut.close();
				fOut = null;
				// mDIStream.close();
				// mDIStream = null;
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						try{
							nDIStream.close();
							nHttpUrlConn.disconnect();
						}catch(Exception e){}
					}
				}).start();
				
				DBG("DownloadComponent", "done writing file "+m_Infofilename);
				return true;
			}
			catch (Exception ex) {DBG_EXCEPTION(ex);}
		}
		else
		{
		try{
			DBG("DownloadComponent", "We have different type of downloader:");
			DBG("DownloadComponent", "download file size: "+mServerFilesize);
			String folder = ".\\";
			DataOutputStream fOutd = new DataOutputStream(fOut);
			fOutd.writeUTF(folder);
			fOutd.writeInt(1);
			fOutd.writeInt(0);
			fOutd.writeInt(((int)mServerFilesize));
			fOutd.writeLong(0);
			fOutd.writeUTF(mServerFilename);
			fOutd.writeInt(((int)mServerFilesize));
			String filezip = mServerFilename+".zip";
			fOutd.writeUTF(filezip);
			#if USE_MDL
			//fOutd.writeUTF(mServerFileChecksum);
			#endif
			fOutd.close();
			fOut.close();
			fOut = null;
			}catch(IOException ex){DBG_EXCEPTION(ex);}
			DBG("DownloadComponent", "done writing file "+m_Infofilename);
			return true;
		}

		return false;
	}
	
	public ArrayList<String> getSoFiles()
	{
		if(mPackFileInfo == null)
		{
			DBG("GameInstaller", "getSoFiles() -  mPackFileInfo == null");
			return null;
		}
		
		ArrayList<String> sos = new ArrayList<String>();
		for(int i = 0; i < mPackFileInfo.size(); i++)
		{
			//DBG("GameInstaller", "name of the component "+mPackFileInfo.get(i).getName());
			String pack_name = mPackFileInfo.get(i).getName();
			if (pack_name.contains(".split_"))
			{
				//splitNumber = Integer.parseInt(pack_name.substring(pack_name.lastIndexOf('_') + 1));
				pack_name = pack_name.substring(0, pack_name.lastIndexOf('.'));
			}
			//DBG("GameInstaller", "name: "+pack_name);
			if(pack_name.endsWith(".so") && !sos.contains(pack_name))
				sos.add(pack_name);
		}
		
		return sos;
	}
	
	private int performFullSplitCheck(String fileName, int index)
	{
		int zipExtraSpace = 0;
		APP_PACKAGE.installer.utils.SplitCRC scrc = null;
		DBG("GameInstaller", "\t\tChecking split section: " + fileName);
		for (int i = index; i < mPackFileInfo.size(); i++)
		{
			PackFile file = mPackFileInfo.get(i);
			if (scrc == null)
				scrc = new APP_PACKAGE.installer.utils.SplitCRC(GameInstaller.DATA_PATH +  file.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") +"/"+fileName);
			DBG("GameInstaller", "\t\tChecking split section: " + APP_PACKAGE.installer.Utils.getSplitName(file.getName())+ " " + i);
			if (!APP_PACKAGE.installer.Utils.getSplitName(file.getName()).equals(fileName))
				break;
			
			if (!scrc.isChecksumOK(file.getChecksum()))
			{
				mRequiredResources.add(file);
				mRequiredSize += file.getLength();
				//DBG("GameInstaller", "\t\tSplit section: " + fileName + "; split fail" + file.getName() + ": " + mRequiredSize + " " + mRequestAll);
				m_iRealRequiredSize += file.getZipLength();

				if (file.getZipLength() > zipExtraSpace)
					zipExtraSpace = file.getZipLength();
			}
			else
			{
				//WARN("GameInstaller", "\t\tSplit section: " + fileName + "; split ok" + file.getName() + ": " + mRequiredSize + " " + mRequestAll);
				mDownloadedSize += file.getZipLength();
				APP_PACKAGE.installer.Utils.markAsSaved(file);
			}
		}
		
		scrc.close();
		scrc = null;
		
		return zipExtraSpace;
	}
	
	public static boolean goodSize(PackFile file)
	{
		String main_path = null;
		String path = file.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") +"/"+ file.getName();
		path = path.substring(0, path.lastIndexOf('.'));
					
		if(path.endsWith(".so"))
		path = GameInstaller.LIBS_PATH + path;
		else
		path = GameInstaller.DATA_PATH + path;
		
		//DBG("DownloadComponent", "good size path: "+file.getName());
		File sdFile = new File(path);
		if (!sdFile.exists())
			return false;
		
		int splitsNo = (int)((sdFile.length() + SPLIT_SIZE - 1) / SPLIT_SIZE);
		
		if (splitsNo == APP_PACKAGE.installer.Utils.getSplitNumber(file.getName()))
		{
			if ((sdFile.length() % SPLIT_SIZE) == file.getLength())
				return true;
			return false;
		}
		else if (sdFile.length() < SPLIT_SIZE * APP_PACKAGE.installer.Utils.getSplitNumber(file.getName()))
			return false;
		return true;
	}
	
	SplitCRC current_crc = null;
	private boolean isRequiredFile(PackFile packfile, boolean testChecksum, boolean validResources, String previousSplitFile)
	{
		
		String path = null;
		int splitNumber = 0;
		//Avoid Installer load the dummy lib.so (always cause crashed).
		String pack_name = packfile.getName();
		if (pack_name.contains(".split_"))
		{
			splitNumber = Integer.parseInt(pack_name.substring(pack_name.lastIndexOf('_') + 1));
			pack_name = pack_name.substring(0, pack_name.lastIndexOf('.'));
		}
		if(pack_name.endsWith("libnativedummy.so"))
			return false;
		if(pack_name.endsWith(".so"))
			path = GameInstaller.LIBS_PATH + pack_name;
		else
		if(mName.startsWith("patch") || mName.startsWith("main"))
			path = GameInstaller.marketPath + packfile.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") +"/"+ packfile.getName();
		else
			path = GameInstaller.DATA_PATH +  packfile.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") +"/"+ packfile.getName();
		
		//DBG("DownloadComponent", "isrequiredfile: "+packfile.getFolder());
		
		long checksum = packfile.getChecksum();
		if (path.contains(".split_"))
		{
			splitNumber = Integer.parseInt(path.substring(path.lastIndexOf('_') + 1));
			path = path.substring(0, path.lastIndexOf('.'));
		}
		//DBG("DownloadComponent","the path to verify : "+path);
		File file =  new File(path);		
		
		boolean invalidChecksum = false;
		//boolean needFile = !file.exists() || file.length() != packfile.getLength();	
		boolean needFile = !file.exists() || (splitNumber > 0 ? !(APP_PACKAGE.installer.Utils.hasBeenDownloaded(packfile, validResources) && goodSize(packfile)) : file.length() != packfile.getLength());		
		
		if(needFile && !file.exists() && APP_PACKAGE.installer.Utils.hasBeenDownloaded(packfile, validResources))
		{
			APP_PACKAGE.installer.Utils.markAsNotDownloaded(packfile);//in case the file is marked as downloaded but it doesn't exist in the folder (manually erased from file system).
		}
		/*DBG("DownloadComponent","need the file? :"+needFile);
		DBG("DownloadComponent","split no :"+splitNumber);
		DBG("DownloadComponent","file exists: "+file.exists());
		DBG("DownloadComponent","is good size:"+goodSize(packfile));
		DBG("DownloadComponent","is the length different:"+(file.length() != packfile.getLength()));
		DBG("DownloadComponent","file absoulte path: "+file.getAbsolutePath());
		DBG("DownloadComponent","the length of the file: "+file.length());*/
		if (!needFile && checksum != 0) 
		{		
			/*if (testChecksum && DEBUG)
			{
				DBG("DownloadComponent","File "+packfile.getName()+ "Size "+packfile.getLength());
			}*/
			
			
			//DBG("DownloadComponent","File "+packfile.getName()+ "Size "+packfile.getLength());
			if (!previousSplitFile.equals(packfile.getName()) && splitNumber > 0)
			{
				if(testChecksum)
				if(current_crc == null)
				{
					//DBG("GameInstaller", "null splitcrc");
					current_crc = new SplitCRC(path);
				}
				else
				{
					if(current_crc.getPath().compareTo(path) != 0)
					{
						//DBG("GameInstaller", "splitcrc old path = "+current_crc.getPath()+" new path: "+path);
						current_crc = new SplitCRC(path);
					}
					//else
					//DBG("GameInstaller", "using same splitcrc with path: "+path);
				}
				//invalidChecksum = !testChecksum?false: ( packfile.getMD5().equals("")?(!CRC.isValidChecksum(path, checksum, splitNumber)):MD5.isValidChecksum(path, packfile.getMD5()));//george
				invalidChecksum = !testChecksum?false: ( packfile.getMD5().equals("")?(!current_crc.isChecksumOK(checksum)):MD5.isValidChecksum(path, packfile.getMD5()));//george
			}
			else if (splitNumber <= 0)
			{
				
				invalidChecksum = !testChecksum?false: ( packfile.getMD5().equals("")?(!CRC.isValidChecksum(path, checksum, splitNumber)):MD5.isValidChecksum(path, packfile.getMD5()));//george
				//invalidChecksum = !testChecksum?false: ( packfile.getMD5().equals("")?(!current_crc.isChecksumOK(checksum)):MD5.isValidChecksum(path, packfile.getMD5()));//george
			}
				
			//DBG("DownloadComponent","Invalid checksum: "+invalidChecksum);
		}
		else if (splitNumber > 0)
		{
			//DBG("TESTY", validResources);
			//DBG("TESTY", "\t" + !APP_PACKAGE.installer.Utils.hasBeenDownloaded(packfile, validResources));
			if (!APP_PACKAGE.installer.Utils.hasBeenDownloaded(packfile, validResources) && validResources)
			{
					invalidChecksum = true; //!CRC.isValidChecksum(path, checksum, splitNumber);
			}
		}
/*#if ENABLE_DOWNLOAD_NATIVE
		if(packfile.getName().endsWith(".so") && !(needFile || invalidChecksum))
			addNativeLib(packfile.getName());
#endif*///george
		if(needFile || invalidChecksum)
		{
			//DBG("DownloadComponent","require file : "+path);
			if((mPackFileInfo.size() == 1)&&(!m_isVersionUpdated))//this is for the simple downloader where there is only one file
			{
				//long ldf = packfile.getLength() - file.length();
				//long ldfzip = packfile.getZipLength() - file.length();
				//DBG("DownloadComponent","setting length "+ ldf +" for "+path);
				//packfile.setLength(ldf);//it's complicated - george
				//packfile.setZipLength((int)ldf);
				m_SimpleDownloadSizeToSkip = file.length();
				
			}
			else
			{
				m_isVersionUpdated = false;
				m_SimpleDownloadSizeToSkip = 0;
				try
				{ 
					if(file.exists()&& splitNumber <= 0)
					{
						file.delete(); 
						//DBG("DownloadComponent","Needed file removed: " + path);
					}
				} catch (Exception fde){ }
			}
		}
		file = null;
		return (needFile || invalidChecksum) ;
	}
	
	public boolean verifySplitChecksum(boolean testChecksum)
	{
		if (!testChecksum)
			return false;
		return true;
		
	}
	
	public int validateFiles(boolean testChecksum, GameInstaller installer)
	{
		DBG("DownloadComponent", "validateFiles() for "+mName);
		
		if(mPackFileInfo == null)
		{
			ERR("DownloadComponent", "validateFiles, mPackFileInfo = null and importance: "+mImportance);
			if(mImportance == 0)
				return 0;
			else
				return 1;
		}
	
		int numberOfFiles = mPackFileInfo.size();
		
		mRequiredResources.clear();
		
		mRequiredSize = 0;
		mDownloadedSize = 0;
		m_iRealRequiredSize = 0;
		int zipExtraSpace = 0;
		
		boolean validResource = false;//isValidResourceVersion();
		
		int firstSplitFileIndex = -1;
		String previousSplitFile = "";
		
		for(int i = 0; i < numberOfFiles; i++)
		{
			PackFile file = mPackFileInfo.get(i);
			if(isRequiredFile(file, testChecksum, validResource, previousSplitFile))
			{
				//DBG("DownloadComponent", "Adding required file "+file.getName()+" with size: "+file.getLength());
				mRequiredResources.add(file);
				mRequiredSize += file.getLength();
				//DBG("DownloadComponent", "File required: " + file.getName() + " " + mRequiredSize);
				m_iRealRequiredSize += file.getZipLength();
				
				if (file.getZipLength() > zipExtraSpace)
					zipExtraSpace = file.getZipLength();
					
				mDownloadedSize += m_SimpleDownloadSizeToSkip;
				
				mRequiredSize -= m_SimpleDownloadSizeToSkip;
				m_iRealRequiredSize -= m_SimpleDownloadSizeToSkip;
			}
			else if (!APP_PACKAGE.installer.Utils.getSplitName(file.getName()).equals(previousSplitFile) && (!(APP_PACKAGE.installer.Utils.hasBeenDownloaded(file, validResource) && goodSize(file)) || validResource))
			{
				ERR("GameInstaller", "Checking for split");
				if (previousSplitFile != "")
				{
					int aux = performFullSplitCheck(previousSplitFile, firstSplitFileIndex);
					if (aux > zipExtraSpace)
						zipExtraSpace = aux;
						
				}
				
				if (file.getName().contains(".split_"))
				{
					firstSplitFileIndex = i;
					previousSplitFile = APP_PACKAGE.installer.Utils.getSplitName(file.getName());
				}
				else
				{
					firstSplitFileIndex = -1;
					previousSplitFile = "";
				}
				
				mDownloadedSize += m_SimpleDownloadSizeToSkip;
			}
			else
			{
				mDownloadedSize += file.getZipLength();
			}
			/*if (testChecksum && mState == GI_STATE_VERIFYING_CHECKSUM)//george - don't forget to comunicate with the installer
			{
				mCurrentProgress = i;// Verifying checksum ProgressBar Status
				updateProgressBar();
			}*/
			
			installer.mCurrentProgress ++;
			installer.updateProgressBar();
		}

		//restart the d_o_w_n_l_o_a_d_e_d file
		APP_PACKAGE.installer.Utils.requestResetDownloadedFile();
		current_crc = null;
		
		mTotalSize = m_iRealRequiredSize + mDownloadedSize;
		
		mbRequiredJustArhive = (int)((mRequiredSize >> 20) + 1);
		
		// Plus extra space for the downloading parts when checking available space
		//mRequiredSize += (zipExtraSpace);//no need for an extra zip space -  check here if you find any problems with amz downloader
		mbRequired = (int)((mRequiredSize >> 20) + 1);
		
		DBG("DownloadComponent", "mbrequired: "+mbRequired + " "+mRequiredSize+" "+zipExtraSpace+" "+mbRequiredJustArhive);
		DBG("DownloadComponent", "realsize: "+m_iRealRequiredSize + " downloaded size: "+mDownloadedSize+" total: "+mTotalSize);
		
		m_isCompleted = false;
		if (mRequiredSize == 0)
		{
			m_isCompleted = true;
		}
		
		/*if (mbRequired > 0)
			mbRequired += EXTRA_SPACE_ON_SD;*/
			
		if(mRequiredResources.size() > 0)
			return 1;
			
		return 0;

	}
	
	public boolean requestForNewVersion()
	{
		if(mVersion.equals(""))
		{
			DBG("DownloadComponent","empty version");
			try
			{
			String update_url = mServerURL + "&head=1";
			if(mClientHTTPForUpdate == null)
			{
				mClientHTTPForUpdate = new HttpClient();
			}
			else
			{
				mClientHTTPForUpdate.close();
			}
			
			mClientHTTPForUpdate.getInputStream(update_url);
			DBG("DownloadComponent","The connection is right");
			return true;
			}catch(Exception ex){DBG_EXCEPTION(ex);DBG("DownloadComponent","The connection is not right");return false;}
		}
		else
			DBG("DownloadComponent","version is not empty, is: "+mVersion);
		
		return true;
	}
	
	public void handleRequestForNewVersion()
	{
		if(mVersion.equals(""))
		{
			mVersion = Integer.toString(mClientHTTPForUpdate.getHeaderInt("x-gl-version", -1));
			DBG("DownloadComponent", "the version: "+mVersion);
			mIsGenericBuild = mClientHTTPForUpdate.getHeaderBoolean("x-gl-generic", true);
			mClientHTTPForUpdate.close();
			mClientHTTPForUpdate = null;
		}
	}
	
	public void StartDownload(String where)
	{
		DBG("DownloadComponent", "Starting download at: "+where + " at url: "+ mServerURL);
		if((mRequiredResources == null) || (mRequiredResources.size() <= 0))
		{
			if(m_pSimpleDownloader != null)
				m_pSimpleDownloader.ResetProgress();
			return;
		}
		
		if(mServerFilename.contains(".amz") || mServerFilename.contains(".jar"))
		{
			DBG("DownloadComponent", "Amz download");
			if (m_pDownloader == null)
			{
				m_pDownloader = new Downloader(mRedirUrl, where, mRequiredResources, mInfoFileLength+4, m_iRealRequiredSize);
			}
			else
			{
				m_pDownloader.retryDownload(mRequiredResources);
			}
			
			m_pDownloader.start();
		}
		else
		{
			DBG("DownloadComponent", "starting simple download for"+ mName);
			if (m_pSimpleDownloader == null)
			{
				m_pSimpleDownloader = new SimpleDownload(mServerURL, where, mRequiredResources, m_SimpleDownloadSizeToSkip);
				m_pSimpleDownloader.start();
			}
			else
			{
			//try{
				//m_pSimpleDownloader.initialize("", m_SimpleDownloadSizeToSkip);
				//m_pSimpleDownloader.retryDownload();
				
				if(m_pSimpleDownloader.isTerimanted)
				{
					m_pSimpleDownloader = new SimpleDownload(mServerURL, where, mRequiredResources, m_SimpleDownloadSizeToSkip);
					m_pSimpleDownloader.start();
				}
				//}catch(Exception ex){}
			}
				
		}
	}
	
	public void StopDownload()
	{
		if (m_pDownloader != null)
			m_pDownloader.stopDownload();
		else
		if (m_pSimpleDownloader != null)
			m_pSimpleDownloader.stopThread();
	}
	
	public void Update()
	{
		if (m_pDownloader != null)
			m_pDownloader.update();//george
			
		if(m_pSimpleDownloader != null)
			m_pSimpleDownloader.update();
	}
	
	public boolean isDownloadCompleted()
	{
		if (m_isCompleted)
			return true;
		if (m_pDownloader != null)
			return m_pDownloader.m_isDownloadCompleted;//george
		else
		if(m_pSimpleDownloader != null)
			return m_pSimpleDownloader.isFinished();
		else
			return true;
	}
	
	public long getDownloadedSizeForDownloader()
	{
		if (m_pDownloader != null)
			return m_pDownloader.getDownloadedSize();
		else
		if (m_pSimpleDownloader != null)
		{
			return m_pSimpleDownloader.getSize();
		}
		else
			return 0;
	}
	
	public boolean isDownloadFailed()
	{
		if (m_pDownloader != null)
			return m_pDownloader.m_isDownloadFailed;
		else
		if(m_pSimpleDownloader != null)
			return m_pSimpleDownloader.isDownloadFailed();
		return false;
	}
	
	public void cancelDownload()
	{
		if (m_pDownloader != null)
		{
			m_pDownloader.m_isDownloadFailed = true;
		}
		else
		if(m_pSimpleDownloader != null)
			m_pSimpleDownloader.stopThread();
	}
}