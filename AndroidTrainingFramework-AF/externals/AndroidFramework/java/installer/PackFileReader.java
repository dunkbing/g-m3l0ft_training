package APP_PACKAGE.installer.utils;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;


import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.Context;

import android.util.Log;

public class PackFileReader {

    Vector<PackFile> mData = null;
	//AssetManager mAssets;
	Resources mRes;
	long serverFileSize = 0;
	long biggestZipSize = 0; 
	long biggestSize	= 0;
	long packSplitSize;
	
    public PackFileReader(Context context)
    {
        mData 	= new Vector<PackFile>();
		//mAssets = context.getAssets();
		mRes = context.getResources();
    }
    private void processRead(DataInputStream inFile) throws IOException
    {
        int version;
        int fileCount;
        int fileSize;
        long fileChecksum;
        int fileZipSize;
        int fileOffset;
		int totalFiles = 0;
        String folder;
        String fileName;
        String fileZipName;
		long entireFileSize = 0;
        PackFile currentFile = null;
        do {
	        folder      = inFile.readUTF();
        if (folder.startsWith("version: ")) {
            version = Integer.parseInt(folder.substring(("version: ").length()));
            packSplitSize = inFile.readInt() * 1024;
            folder  = inFile.readUTF();
        } else {
            version = -1;
            packSplitSize = 0;
		}
	        fileCount   = inFile.readInt();
	
	        for(int i=0;i<fileCount;i++)
	        {
	            currentFile = new PackFile();
	
	            fileOffset  	= inFile.readInt();
	            fileSize    	= inFile.readInt();
	            fileChecksum	= inFile.readLong();
	            fileName    	= inFile.readUTF();
            if (version >= 101)
            {
                if (fileName.endsWith(".split_0001"))
                {
                    entireFileSize = inFile.readLong();
                    currentFile.setUnsplittedLength(entireFileSize);
                    DBG("GameInstaller", "\tfileName: "+fileName+" entireFileSize: "+entireFileSize);
                }
                else if (!fileName.contains(".split_"))
                {
                        currentFile.setUnsplittedLength(fileSize);
                }
                else
                {
                        currentFile.setUnsplittedLength(entireFileSize);
                }
            }
	            fileZipSize 	= inFile.readInt();
	            fileZipName 	= inFile.readUTF();
				
				#if USE_MDL
				String md5 = "";
				if(fileCount == 1)
				DBG("GameInstaller", "here for "+fileName);
				//md5				= inFile.readUTF();
				#endif
	
	            currentFile.setFolder(folder);
	            currentFile.setName(fileName);
	            currentFile.setZipName(fileZipName);
	            currentFile.setLength(fileSize);
	            currentFile.setChecksum(fileChecksum);
	            currentFile.setZipLength(fileZipSize);
	            currentFile.setOffset(fileOffset);
				currentFile.setID(totalFiles++);
				
				#if USE_MDL
				if(fileCount == 1)
				currentFile.setMD5(md5);
				#endif
	
				mData.add(currentFile);
	            if ((fileZipSize > biggestZipSize))
	            	biggestZipSize = fileZipSize;
	            if ((fileSize > biggestSize))
	            	biggestSize = fileSize;
	        }
        }
        while (inFile.available()>0);
       	serverFileSize = currentFile.getOffset() + currentFile.getZipLength();
    }
	
    public Vector<PackFile> read(int resID) throws FileNotFoundException, IOException
    {        
		DataInputStream inFile = null;
		InputStream raw = null;
		raw = mRes.openRawResource(resID);			
		inFile = new DataInputStream(raw);

        if(inFile != null && inFile.available()>0)
        {
            processRead(inFile);
            inFile.close();
        }
        else
        {
            ERR("GameInstaller","Error: reading from "+resID+" no mData avaliable ");
        }

        return mData;
    }
    
    public Vector<PackFile> read(String fileName) throws FileNotFoundException, IOException
    {        
		DataInputStream inFile = null;
		inFile = new DataInputStream(new FileInputStream(fileName));

        if(inFile != null && inFile.available() > 0)
        {
            processRead(inFile);
            inFile.close();
        }             
		else
        {
            ERR("GameInstaller","Error: reading from "+fileName+" no mData avaliable ");
        }
        return mData;
    }
    
    public long getServerFileSize()
    {
    	return serverFileSize;
    }
    
    public long getBiggestZipSize()
    {
		return biggestZipSize;
    }
    
    public long getBiggestSize()
    {
		return biggestSize;
    }
		
}
