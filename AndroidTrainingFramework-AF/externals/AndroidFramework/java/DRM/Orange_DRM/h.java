package APP_PACKAGE;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import APP_PACKAGE.GLUtils.SUtils;

public class h
{
	Cipher mOutCipher, mInCipher;

	public h(String seed)
	{
		try
		{
			mOutCipher = Cipher.getInstance(c(), d());
			mOutCipher.init(
				Cipher.ENCRYPT_MODE,
				KeyFactory.getInstance(a(), d()).generatePublic(
					new X509EncodedKeySpec(
						k.decode(seed))));
		
			mInCipher = Cipher.getInstance(c(), d());
			mInCipher.init(
				Cipher.DECRYPT_MODE, 
				KeyFactory.getInstance(a(), d()).generatePublic(
					new X509EncodedKeySpec(
						k.decode(seed))));
			
		} 
		catch(Exception e) 
		{
			DBG_EXCEPTION(e);
		}
		
	}
	
	public String encrypt(String str)
	{
		try {
			byte[] data = str.getBytes();
			byte[] enc = mOutCipher.doFinal(data);
			return k.encodeBytes(enc);
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
		return null;
	}
	
	public synchronized String getMiddle()
	{
		String empty = GAME_NAME_STR;
		empty = empty.substring(0, empty.length());
		String str = empty.trim();
		str = str + ((char)95 + "");
		str = str + SUtils.ReadFile(R.raw.infoversion);
		return str.trim();
	}
	
	private String c()
	{
		byte[] input = // "RSA/None/PKCS1Padding"
		{ 
			82, 1, -18, -18, 31, 33, -1, -9, -54, 33, 
			-5, -8, 16, -34, 31, 17, 3, 0, 5, 5, -7
		};
		
		for (int i = 0; i < 21; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return (new String(input));
	}
	
	private String a()
	{
		byte[] input = // "RSA"
		{ 
			82, 1, -18
		};
		
		for (int i = 0; i < 3; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return (new String(input));
	}
	
	private String d()
	{
		byte[] input = // "BC"
		{ 
			66, 1
		};
		
		for (int i = 0; i < 2; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return (new String(input));
	}
	
	public String decrypt(String str)
	{
		try {
			byte[] data = k.decode(str);
			byte[] dec = mInCipher.doFinal(data);
			return new String(dec);
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
		return null;
	}
	
	public String getLast()
	{
		byte[] input = { 95, -30, 45, -10, 14, -3, -6, -5 };
		for (int i = 0; i < 8; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return (new String(input));
	}
	
}
