package APP_PACKAGE.DRM.Gloft;

import android.util.Log;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.io.InputStream;
import java.security.SecureRandom;  
import javax.crypto.KeyGenerator;  
import javax.crypto.SecretKey;  
//import android.util.Base64;

public class StringEncrypter{

	//private final String ALGORITHM = "AES/CBC/PKCS5Padding";
	private final String ALGORITHM = "AES";
	String mSeedKey;
	
	SecretKeySpec mKey;
	
	Cipher mOutCipher, mInCipher;
	
	
	private byte[] getRawKey(byte[] seed) throws Exception {
			KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);  
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");  
			sr.setSeed(seed);  
			kgen.init(128, sr); // 192 and 256 bits may not be available  
			SecretKey skey = kgen.generateKey();  
			byte[] raw = skey.getEncoded();  
			return raw;
	}

	public StringEncrypter(String seed)
	{
		mSeedKey = seed;
		
		try
		{
			mKey = new SecretKeySpec(getRawKey(mSeedKey.getBytes()), ALGORITHM);
			mOutCipher = Cipher.getInstance(ALGORITHM);
			mOutCipher.init(Cipher.ENCRYPT_MODE,mKey);
		
			mInCipher = Cipher.getInstance(ALGORITHM);
			mInCipher.init(Cipher.DECRYPT_MODE,mKey);
		}catch(Exception e) {	DBG_EXCEPTION(e);	}
		
	}
	
	public String encrypt(String str)
	{
		try {
			byte[] data = str.getBytes();
			// Encrypt
			byte[] enc = mOutCipher.doFinal(data);
			
			// Encode bytes to base64 to get a string
			return Base64.encodeBytes(enc);
		} catch (Exception e) { DBG_EXCEPTION(e); }
		return null;
	}
	
	public String decrypt(String str)
	{
		try {
			byte[] data = Base64.decode(str);
			byte[] dec = mInCipher.doFinal(data);
			return new String(dec);
		} catch (Exception e) { DBG_EXCEPTION(e); }
		return null;
	}	
	
}
