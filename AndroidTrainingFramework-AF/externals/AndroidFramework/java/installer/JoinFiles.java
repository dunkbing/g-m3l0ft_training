//#if USE_DOWNLOAD_MANAGER
package APP_PACKAGE.installer.utils;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import java.io.BufferedInputStream;


public class JoinFiles implements Defs
{
	static final int MERGE_BUFFER_SIZE = 512 * 1024;
	static long m_lFileSize = 0;
	static int m_iNumParts = 0;
	
	public static void join (String strOutputPath, PackFile outFile, long fileSize, int numParts)
	{
		String fullFileName;
		
		if (outFile == null)
			fullFileName = strOutputPath + "/" + JOINED_FILE_NAME;
		else
			fullFileName = strOutputPath + "/" + outFile.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") + "/" + outFile.getZipName();		
		
		DBG("GameInstaller","join() fullFileName=" + fullFileName);
		
		byte[][] temp = new byte[2][];
		temp[0] = new byte[MERGE_BUFFER_SIZE];		
	
		try
		{
			m_lFileSize = fileSize;
			m_iNumParts = numParts;
			
			File file = new File(fullFileName);
			file.getParentFile().mkdirs();
			FileOutputStream out = null;
			if (numParts > 0)
			{
				file.createNewFile();
				out = new FileOutputStream(fullFileName);
			}
			
			for (int i = 0; i < numParts; i++)
			{
				File tmpFile = new File(strOutputPath + "/" + SECTION_FILE_NAME + i);
				if (numParts == 1)
				{	
					tmpFile.renameTo(file);
					return;
				}
				FileInputStream fis = new FileInputStream(tmpFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				DataInputStream dis = new DataInputStream(bis);		

				long readSize = 0;
				long splitFileSize = dis.available();
				
				DBG("GameInstaller","splitFileSize=" + splitFileSize);
				
				while (readSize < splitFileSize)
				{
					int left = (int)(splitFileSize - readSize);
					int bytesToRead = (left > MERGE_BUFFER_SIZE) ? MERGE_BUFFER_SIZE : (int)left;
					
					if (bytesToRead == MERGE_BUFFER_SIZE)
					{
						dis.readFully(temp[0]);			
						out.write(temp[0]);
					}
					else
					{
						temp[1] = new byte[bytesToRead];
						dis.readFully(temp[1]);			
						out.write(temp[1]);
					}
					
					readSize += bytesToRead;
					
					DBG("GameInstaller","readSize=" + readSize);
				}
				
				fis.close();
				bis.close();
				dis.close();
										
				// Remove joined file part
				tmpFile.delete();				
				tmpFile = null;
			}
			
			out.close();
			out = null;
			temp = null;
			System.gc();
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}
	public static void join (String strOutputPath, long fileSize, int numParts)
	{
		join(strOutputPath, null, fileSize, numParts);
	}
}

//#endif