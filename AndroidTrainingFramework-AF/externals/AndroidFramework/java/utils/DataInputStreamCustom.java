package APP_PACKAGE.installer.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;


public final class DataInputStreamCustom  extends FilterInputStream
{
	private long pos = 0;
	private long maxBytes = 0;

	public DataInputStreamCustom(InputStream in)
	{
		super(in);
	}

	public synchronized long getPosition()
	{		
		return pos;
	}

	@Override
	public synchronized int read() throws IOException
	{
		if (pos + 1 > maxBytes)
			return -2;
	
		int b = super.read();
		if (b >= 0)
			pos += 1;

		return b;
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException
	{
		if (pos + len > maxBytes)
		{
			len = (int) (maxBytes - pos);
			
			if (len <= 0)
			{
				return -2;
			}
		}
		
		int n = super.read(b, off, len);
				
		if (n > 0)
			pos += n;
		
		return n;
	}

	@Override
	public synchronized long skip(long skip) throws IOException
	{
		long n = super.skip(skip);
		
		if (n > 0)
			pos += n;
		
		return n;
	}

	public synchronized void setMax(long max)
	{
		maxBytes = max;
	}
	
	public synchronized void resetPos()
	{
		pos = 0;
	}
	
	public synchronized void skipToMax()
	{
		try
		{
			long len = maxBytes - pos;
			if (len > 0)
				skip(len);
		}
		catch (Exception e)
		{
			ERR("DataInputStreamCustom", "skip exception: " + e.getMessage());
		}
	}
	
}