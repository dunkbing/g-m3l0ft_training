package APP_PACKAGE.installer.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 *
 * @author Administrator
 */

public class SplitCRC 
{
	private CRC32 cs_crc = null;
	private CheckedInputStream ch_is = null;
	private String mPath = null;
	int counter = -1;
	
	public SplitCRC(String fileName)
	{
		try
        {
			cs_crc = new CRC32();
			ch_is = new CheckedInputStream(new FileInputStream(fileName), cs_crc);
			mPath = fileName;
			DBG("GameInstaller", "new crc with path="+fileName);
        } 
		catch (FileNotFoundException e) 
		{
            DBG_EXCEPTION(e);
        }
	}
	
	private long getNextChecksum()
	{
		try
        {
			cs_crc.reset();
			ch_is.skip(SPLIT_SIZE*1024);
			counter++;
			return ch_is.getChecksum().getValue();
        }
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
			return -1;
		}
	}
	
	public String getPath()
	{
		return mPath;
	}
	
	public boolean isChecksumOK(long checkSum)
	{
		long splitCRC = getNextChecksum();
		DBG("GameInstaller","file crc="+splitCRC+" inf crc="+checkSum+" counter= "+counter+" "+((splitCRC == checkSum)?"Match":"Don\'t Match"));
		return splitCRC == checkSum;
	}
	
	public void close()
	{
		try
		{
			if (ch_is != null)
			{
				ch_is.close();
				ch_is = null;
			}
			cs_crc = null;
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}
}
