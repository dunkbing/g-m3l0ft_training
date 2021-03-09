package APP_PACKAGE.GLUtils;

import android.os.Build;
import android.util.Log;
import android.content.Context;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

#if USE_INSTALLER
import APP_PACKAGE.installer.utils.PackFile;
import APP_PACKAGE.installer.GameInstaller;
#endif

public class ZipFile
{
	public static void extract(String strInputPath, String strOutputPath)
	{
		try
		{
			byte[] buf = new byte[1024];

			ZipInputStream zipInputStream = null;
			ZipEntry zipEntry;
			zipInputStream = new ZipInputStream(new FileInputStream(strInputPath));

			zipEntry = zipInputStream.getNextEntry();

			while (zipEntry != null)
			{
				String entryName = zipEntry.getName();
				DBG("GameInstaller","file: " + entryName);
				int n;
				FileOutputStream out;
				File newFile = new File(entryName);
				String directory = newFile.getParent();

				if (directory == null)
				{
					if (newFile.isDirectory())
					{
						break;
					}
				}

				out = new FileOutputStream(strOutputPath + "/" + entryName);

				while ((n = zipInputStream.read(buf, 0, 1024)) > -1)
				{
					out.write(buf, 0, n);
				}

				out.close();
				zipInputStream.closeEntry();
				zipEntry = zipInputStream.getNextEntry();

			} //while

			zipInputStream.close();
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}
#if USE_INSTALLER
#if !USE_LZMA
	public static boolean unZip(PackFile packFile, String strOutputPath) 
	{		
		String name  	= packFile.getZipName();
		String folder  	= packFile.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/");           
		String dstFolder = strOutputPath + "/" + folder + "/";
			
		try 
		{			
			java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(dstFolder + name);
			Enumeration enumeration = zipFile.entries();
		
			while (enumeration.hasMoreElements())
			{
				ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();			
				
				String dstFileName = null;			
				if(zipEntry.getName().endsWith(".so"))
				{
					dstFileName = GameInstaller.LIBS_PATH + zipEntry.getName();					
					GameInstaller.addNativeLib(zipEntry.getName());
				}
				else				
				{
					dstFileName = dstFolder + zipEntry.getName();
				}
				
				DBG("GameInstaller","Unzipping: " + dstFileName);
				
				File dstFile = new File(dstFileName);			
				//Create folder if not exists
				File parent = new File(dstFile.getParent());
				parent.mkdirs();				
				parent = null;				
				
				if(dstFile.exists())
				{
					DBG("GameInstaller","Unzipping: file already exist remplacing" );
					dstFile.delete();
				}
				dstFile.createNewFile();

				if(dstFileName.endsWith(".so"))
				{
					GameInstaller.makeLibExecutable(dstFileName);					
				}			
				dstFile = null;			

				BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));

				int size;
				byte[] buffer = new byte[16*1024];
				
				FileOutputStream fos = new FileOutputStream(dstFileName);				
				BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);

				while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
					bos.write(buffer, 0, size);
				}

				bos.flush();
				bos.close();
				fos.close();
				bis.close();
			}
			File zip = new File(dstFolder + name);
			zip.delete();
		} catch (IOException e){
			DBG_EXCEPTION(e);
			return false;
		} catch (Exception e){
			DBG_EXCEPTION(e);
			return false;
		}
	
		return true;
	}
#else //LZMA
	public static boolean unZip(PackFile packFile, String strOutputPath) 
	{
		try 
		{
			String name  	= packFile.getZipName();
			String folder  	= packFile.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/");           
			String dstFolder = strOutputPath + "/" + folder + "/";
			
			File destFolder = new File (dstFolder);
			destFolder.mkdirs();           
			destFolder = null;
			
			DBG( "GameInstaller" ,  "LZMA unzipping: " + dstFolder + name ) ;
			File file = new File(dstFolder + name);
			String fileName = file.getName().substring(0,file.getName().length()-5);
			java.io.File outFile = new java.io.File(dstFolder+fileName);
			DBG("GameInstaller","Unzipping: folder:" + dstFolder + "name:"+fileName);
			java.io.BufferedInputStream inStream  = new java.io.BufferedInputStream(new java.io.FileInputStream(file));
			FileOutputStream fos = null;			
			if(fileName.endsWith(".so"))
			{
				GameInstaller.addNativeLib(fileName);
				
				File parent = new File(GameInstaller.LIBS_PATH);
				parent.mkdirs();				
				parent = null;				
				
				fileName = GameInstaller.LIBS_PATH + fileName;
				File dstFile = new File(fileName);
				if(dstFile.exists())
				{
					DBG("GameInstaller","Unzipping: file already exist remplacing" );
					dstFile.delete();
				}
				dstFile.createNewFile();
				dstFile = null;
				
				GameInstaller.makeLibExecutable(fileName);
				
				fos = new FileOutputStream(fileName);
				
			}
			else		
			{
				fos = new FileOutputStream(dstFolder + fileName);
			}
			java.io.BufferedOutputStream outStream = new java.io.BufferedOutputStream(fos);
			int propertiesSize = 5;
			byte[] properties = new byte[propertiesSize];
			if (inStream.read(properties, 0, propertiesSize) != propertiesSize)
				throw new Exception("input .lzma file is too short");
			SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
			if (!decoder.SetDecoderProperties(properties))
				throw new Exception("Incorrect stream properties");
			long outSize = 0;
			for (int j = 0; j < 8; j++)
			{
				int v = inStream.read();
				if (v < 0)
					throw new Exception("Can't read stream size");
				outSize |= ((long)v) << (8 * j);
			}
			if (!decoder.Code(inStream, outStream, outSize))
				throw new Exception("Error in data stream");
			outStream.flush();
			outStream.close();
			inStream.close();
			file.delete();
		} catch (IOException e){
			DBG_EXCEPTION(e);
			return false;
		} catch (Exception e){
			DBG_EXCEPTION(e);
			return false;
		}
		return true;
	}
#endif	
#endif
}
