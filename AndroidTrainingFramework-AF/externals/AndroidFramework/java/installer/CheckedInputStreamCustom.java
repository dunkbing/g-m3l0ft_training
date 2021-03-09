package APP_PACKAGE.installer.utils;

import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Checksum;



public class CheckedInputStreamCustom extends FilterInputStream
{
	public CheckedInputStreamCustom(InputStream in, Checksum sum)
	{
		super (in);
		this.sum = sum;
	}
	
	public Checksum getChecksum ()
	{
		return sum;
	}
	
	public int read () throws IOException
	{
		int x = in.read();
		if (x != -1)
			sum.update(x);
		return x;
	}
	
	public int read (byte[] buf, int off, int len) throws IOException
	{
		int r = in.read(buf, off, len);
		if (r != -1)
			sum.update(buf, off, r);
		return r;
	}

	public long skip (long n) throws IOException
	{
		if (n == 0)
			return 0;
	
		int min = (int) Math.min(n, 1024);
		byte[] buf = new byte[min];
	
		long s = 0;
		while (n > 0)
		{
			int r = in.read(buf, 0, min);
			if (r == -1)
				break;
			n -= r;
			s += r;
			min = (int) Math.min(n, 1024);
			sum.update(buf, 0, r);
		}
		sum.reset();
		return s;
	}
	
	private Checksum sum;
 }
 
