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

public class CRC {

	public static final boolean DELETE_INVALID_FILES = true;
	
    /**
     * @return the CRC from the file
     * @param fileName the full path of the file
     */
    public static long calcChecksum(String fileName)
    {
        try
        {
            CheckedInputStream ch_is = null;
            try
            {
                // Computer CRC32 checksum
                ch_is = new CheckedInputStream(new FileInputStream(fileName), new CRC32());
            } catch (FileNotFoundException e) {
                DBG_EXCEPTION(e);
                return 0;
            }

            byte[] buffer = new byte[128];
            while(ch_is.read(buffer) >= 0) { }

            long checksum = ch_is.getChecksum().getValue();
            ch_is.close();
            return checksum;
        } catch (IOException e) {
            DBG_EXCEPTION(e);
        }
        return 0;
    }

    /**
     * @return true if it is a valid file.
     * @param fileName the full path of the file
     * @param crc the crc to compare
     */
    public static boolean isValidChecksum(String fileName, long crc)
    {
		long fcrc = calcChecksum(fileName);
   		DBG("GameInstaller","file crc="+fcrc+" inf crc="+crc+" "+((fcrc == crc)?"Match":"Don\'t Match"));
   		if (DELETE_INVALID_FILES && fcrc != crc)
   		{
   			File file = new File(fileName);
   			file.delete();
   		}
    	return (fcrc == crc);
    }
		/**
     * @return the CRC from the file
     * @param fileName the full path of the file
     */
    public static long calcChecksum(String fileName, int splitNumber)
    {
        try
        {
        	CheckedInputStream ch_is = null;
            long currentRead = 0;
            CRC32 cs_crc = new CRC32();
            try
            {
                ch_is = new CheckedInputStream(new FileInputStream(fileName), cs_crc);
				 cs_crc.reset();
                if (splitNumber > 1)
                	ch_is.skip((splitNumber - 1) * SPLIT_SIZE*1024);
                cs_crc.reset();
            } catch (FileNotFoundException e) {
                DBG_EXCEPTION(e);
                return 0;
            }
			/*
            byte[] buffer = new byte[128];
            while((splitNumber == 0 || currentRead < SPLIT_SIZE) && ch_is.read(buffer) >= 0) 
            {
            	currentRead += 128;
            }*/
			if (splitNumber > 0) {
                ch_is.skip(SPLIT_SIZE*1024);
			}
			else {
				byte[] buffer = new byte[128];
				while((splitNumber == 0 || currentRead < SPLIT_SIZE*1024) && ch_is.read(buffer) >= 0) 
				{
					currentRead += 128;
				}
			}
            long checksum = ch_is.getChecksum().getValue();
            ch_is.close();
            return checksum;
        } catch (IOException e) {
            DBG_EXCEPTION(e);
        }
        return 0;
    }
    public static boolean isValidChecksum(String fileName, long crc, int splitNumber)
    {
		long fcrc = calcChecksum(fileName, splitNumber);
   		DBG("DownloadComponent","Split: " + splitNumber + "file "+fileName +" crc="+fcrc+" inf crc="+crc+" "+((fcrc == crc)?"Match":"Don\'t Match"));
   		if (splitNumber <= 0 && DELETE_INVALID_FILES && fcrc != crc)
   		{
   			File file = new File(fileName);
   			file.delete();
   		}
    	return (fcrc == crc);
    }
}
