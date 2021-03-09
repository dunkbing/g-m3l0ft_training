package APP_PACKAGE.installer.utils;

import java.util.zip.ZipInputStream;
import java.util.zip.Inflater;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.DataFormatException;
import java.io.EOFException;

public class ZipInputStreamCustom extends ZipInputStream
{
    public ZipInputStreamCustom(InputStream in)
	{
		super(in);
    }
	
	public long getRemaining()
	{
		return inf.getRemaining();
	}
	
	public long getBytesRead()
	{
		return inf.getBytesRead();
	}

	public long getTotalIn()
	{
		return inf.getTotalIn();
	}
	
	public long getTotalOut()
	{
		return inf.getTotalOut();
	}


}