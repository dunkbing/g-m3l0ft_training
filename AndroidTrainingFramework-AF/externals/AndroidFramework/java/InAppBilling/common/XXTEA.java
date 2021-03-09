#if GAMELOFT_SHOP || USE_BILLING
package APP_PACKAGE.billing.common;


public class XXTEA/* implements Constants */{
	//------------------------------------------------------------------------------
	/// Encrypt data with key.
	///
	/// @param data data to encrypt
	/// @param key key must be unique for each project
	/// @return byte array with encrypted data
	//------------------------------------------------------------------------------
	public byte[] XXTEA_Encrypt(byte[] data, String key)
	{
		byte[] tmp = null;
		if (!"".equals(key) && key != null)
			tmp = key.getBytes();

		return XXTEA_Encrypt(data, tmp);
	}

	//------------------------------------------------------------------------------
	/// Encrypt data with key.
	///
	/// @param data data to encrypt
	/// @param key key must be unique for each project
	/// @return byte array with encrypted data
	//------------------------------------------------------------------------------
	public byte[] XXTEA_Encrypt(byte[] data, byte[] key)
	{
		if (data.length == 0)
		{
			return data;
		}
		return ToByteArray(XXTEA_Encrypt(ToIntArray(data, true, null), ToIntArray(key, false, null)), false, null);
	}

	//------------------------------------------------------------------------------
	/// Decrypt data with key.
	///
	/// @param data data to decrypt
	/// @param key key used for encryption
	/// @return byte array with dencrypted data
	//------------------------------------------------------------------------------
	public byte[] XXTEA_Decrypt(byte[] data, String key)
	{
		byte[] tmp = null;
		if (!"".equals(key) && key != null)
			tmp = key.getBytes();

		return XXTEA_Decrypt(data, tmp);
	}

	//------------------------------------------------------------------------------
	/// Decrypt data with key.
	///
	/// @param data data to decrypt
	/// @param key key used for encryption
	/// @return byte array with dencrypted data
	//------------------------------------------------------------------------------
	public byte[] XXTEA_Decrypt(byte[] data, byte[] key)
	{
		if (data == null || data.length == 0)
		{
			return data;
		}

		return ToByteArray(XXTEA_Decrypt(ToIntArray(data, false, null), ToIntArray(key, false, null)), true, null);
	}

	//------------------------------------------------------------------------------
	/// Encrypt data with key.
	///
	/// @param data data to encrypt
	/// @param key key must be unique for each project
	/// @return int array with encrypted data
	//------------------------------------------------------------------------------
	public int[] XXTEA_Encrypt(int[] data, String key)
	{
		byte[] tmp = null;
		if (!"".equals(key) && key != null)
			tmp = key.getBytes();

		return XXTEA_Encrypt(data, ToIntArray(tmp));
	}

	//------------------------------------------------------------------------------
	/// Encrypt data with key.
	///
	/// @param data data to encrypt
	/// @param key key must be unique for each project
	/// @return int array with encrypted data
	//------------------------------------------------------------------------------
	public int[] XXTEA_Encrypt(int[] data, int[] key)
	{
		int n = data.length - 1;

		if (n < 1)
		{
			return data;
		}

		if (key.length < 4)
		{
			int[] tmp = new int[4];

			System.arraycopy(key, 0, tmp, 0, key.length);
			key = tmp;
		}

		int z = data[n], y = data[0], delta = 0x9E3779B9, sum = 0, e;
		int p, q = 6 + 52 / (n + 1);

		while (q-- > 0)
		{
			sum = sum + delta;
			e = sum >>> 2 & 3;
			for (p = 0; p < n; p++)
			{
				y = data[p + 1];
				z = data[p] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (key[p & 3 ^ e] ^ z);
			}
			y = data[0];
			z = data[n] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (key[p & 3 ^ e] ^ z);
		}
		return data;
	}

	//------------------------------------------------------------------------------
	/// Decrypt data with key.
	///
	/// @param data data to decrypt
	/// @param key key used for encryption
	/// @return byte array with dencrypted data
	//------------------------------------------------------------------------------
	public int[] XXTEA_Decrypt(int[] data, String key)
	{
		byte[] tmp = null;
		if (!"".equals(key) && key != null)
			tmp = key.getBytes();

		return XXTEA_Decrypt(data, ToIntArray(tmp));
	}

	//------------------------------------------------------------------------------
	/// Decrypt data with key.
	///
	/// @param data data to decrypt
	/// @param key key used for encryption
	/// @return byte array with dencrypted data
	//------------------------------------------------------------------------------
	public int[] XXTEA_Decrypt(int[] data, int[] key)
	{
		int n = data.length - 1;

		if (n < 1)
		{
			return data;
		}

		if (key.length < 4)
		{
			int[] tmp = new int[4];

			System.arraycopy(key, 0, tmp, 0, key.length);
			key = tmp;
		}

		int z = data[n], y = data[0], delta = 0x9E3779B9, sum, e;
		int p, q = 6 + 52 / (n + 1);

		sum = q * delta;
		while (sum != 0)
		{
			e = sum >>> 2 & 3;
			for (p = n; p > 0; p--)
			{
				z = data[p - 1];
				y = data[p] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (key[p & 3 ^ e] ^ z);
			}

			z = data[n];
			y = data[0] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (key[p & 3 ^ e] ^ z);
			sum = sum - delta;
		}
		return data;
	}
	

	//------------------------------------------------------------------------------
	/// Convert int array to byte array.
	///
	/// @param data int array
	/// @return byte array
	//------------------------------------------------------------------------------
	public byte[] ToByteArray(int[] data)
	{
		return ToByteArray(data, false, null);
	}

	//------------------------------------------------------------------------------
	/// Convert int array to byte array.
	///
	/// @param data int array
	/// @param includeLength if true, will add one more element to result
	/// @param result array to store result. If it is null new array will be allocated
	/// @see GLLib.ToIntArray, includeLength param
	/// @return byte array
	//------------------------------------------------------------------------------
	public byte[] ToByteArray(int[] data, boolean includeLength, byte[] result)
	{
		int n = data.length << 2;

		if (includeLength)
		{
			int m = data[data.length - 1];

			if (m > n)
			{
				return null;
			}
			else
			{
				n = m;
			}
		}

		if (result == null)
			result = new byte[n];

		for (int i = 0; i < n; i++)
		{
			result[i] = (byte)((data[i >>> 2] >>> ((i & 3) << 3)) & 0xff);
		}
		return result;
	}

	//------------------------------------------------------------------------------
	/// Convert byte array to int array.
	///
	/// @param data byte array
	/// @return int array
	//------------------------------------------------------------------------------
	public int[] ToIntArray(byte[] data)
	{
		return ToIntArray(data, false, null);
	}

	//------------------------------------------------------------------------------
	/// Convert byte array to int array.
	///
	/// @param data byte array
	/// @param includeLength if true, will add length to the end of array
	/// @param result array to store result. If it is null new array will be allocated
	/// @return int array
	//------------------------------------------------------------------------------
	public int[] ToIntArray(byte[] data, boolean includeLength, int[] result)
	{
		int n = (((data.length & 3) == 0) ? (data.length >>> 2) : ((data.length >>> 2) + 1));

		if (includeLength)
		{
			if (result == null)
				result = new int[n + 1];

			result[n] = data.length;
		}
		else if (result == null)
		{
			result = new int[n];
		}

		n = data.length;
		for (int i = 0; i < n; i++)
		{
			result[i >>> 2] |= (0x000000ff & data[i]) << ((i & 3) << 3);
		}
		return result;
	}


	final static char[] hexDigit = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	//------------------------------------------------------------------------------
	/// Convert byte array to hex string
	///
	/// @param byte array
	/// @return hex string
	//------------------------------------------------------------------------------

	public String GetHexString(byte[] bytes)
	{
		StringBuffer sb = new StringBuffer( bytes.length*2 );
		for( int i = 0; i < bytes.length; i++ )
		{
			sb.append(hexDigit[(bytes[i] >> 4) & 0xF]);
			sb.append(hexDigit[(bytes[i]) & 0xF]);
		}

		return sb.toString();
	}


	//------------------------------------------------------------------------------
	/// Get byte array from a hex string
	/// @param hex string
	/// @return byte array
	//------------------------------------------------------------------------------
	public byte[] GetByteArrayFromHexString(String hex)
	{
		try
		{
			byte[] result = new byte[hex.length()>>1];
			
			for(int i = 0;i < result.length; i++)
			{
				result[i] = (byte)(Integer.parseInt(hex.substring(i*2, i*2 + 2), 16) & 0xFF);
			}
			return result;
		}
		catch(Exception e)
		{
		}
		return null;
	}
	
	private String ENCRYPTION_KEY = null;
	private final String XXTEA_K 	= XXTEA_KEY;
	private final  String KEY		= GGC_GAME_CODE;
	public String getKey()
	{
		if (ENCRYPTION_KEY == null)
		{
			ENCRYPTION_KEY = APP_PACKAGE.GLUtils.DefReader.getInstance().getPlainDef(XXTEA_K, KEY);
		}
		if (ENCRYPTION_KEY == null)//return debug key
			ENCRYPTION_KEY = GGC_GAME_CODE;
		return ENCRYPTION_KEY;
	}
	
	/*public static String EncryptString(String str)
	{
		byte[] encryptStr = XXTEA.XXTEA_Encrypt(str.getBytes(), IAP_XXTEA_DEFAULT_KEY);
		String hexXXTEA = XXTEA.GetHexString(encryptStr);
		return hexXXTEA;
	}
	
	public static String DecryptString(String str)
	{
		byte[] encryptStr = XXTEA.GetByteArrayFromHexString(str);
        byte[] decryptStr = XXTEA.XXTEA_Decrypt(encryptStr, IAP_XXTEA_DEFAULT_KEY);
        return new String(decryptStr);
	}*/
}
#endif