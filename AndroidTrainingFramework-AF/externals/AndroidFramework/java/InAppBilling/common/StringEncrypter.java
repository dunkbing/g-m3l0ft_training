#if USE_IN_APP_BILLING || USE_BILLING
package APP_PACKAGE.billing.common;

import APP_PACKAGE.R;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.io.InputStream;
import java.security.SecureRandom;  
import javax.crypto.KeyGenerator;  
import javax.crypto.SecretKey;  
import APP_PACKAGE.billing.common.Base64;

import APP_PACKAGE.GLUtils.SUtils;

public class StringEncrypter{

	//private final String ALGORITHM = "AES/CBC/PKCS5Padding";
	private final String ALGORITHM = "AES";
	SET_TAG("InAppBilling");
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
		
		
		mPasses[0] = -1;
		mPasses[1] = -1;
		mPasses[2] = -1;
		mPasses[3] = -1;
		mPasses[4] = -1;
		mPasses[5] = -1;
		
		try
		{
			mKey = new SecretKeySpec(getRawKey(mSeedKey.getBytes()), ALGORITHM);
			mOutCipher = Cipher.getInstance(ALGORITHM);
			mOutCipher.init(Cipher.ENCRYPT_MODE,mKey);
		
			mInCipher = Cipher.getInstance(ALGORITHM);
			mInCipher.init(Cipher.DECRYPT_MODE,mKey);
		} catch(Exception e) {	DBG_EXCEPTION(e);	}
		
		
		mPasses[6] = -1;
		mPasses[7] = -1;
		mPasses[8] = -1;
		mPasses[9] = -1;
		
	}
	
	//return dummy value.
	public String getValue()
	{
		int id = mnPasses % 6;
		
		String ret = new String(mPasses[id]+"");
		id = (id+1) % 10;
		ret += id;
		id = (id+1) % 8;
		ret += id;
		return new String( ret );
	}
	
	int mPasses[] = new int[10];
	String mValues[] = new String[10];
	String mValidator;
	public StringEncrypter(String seed, String validator)
	{
		mSeedKey = seed;
		mValidator = validator;
		
		mPasses[0] = Integer.parseInt(new String(validator.charAt(2)+""));
		mPasses[1] = Integer.parseInt(new String(validator.charAt(3)+""));
		mPasses[2] = Integer.parseInt(new String(validator.charAt(4)+""));
		mPasses[3] = Integer.parseInt(new String(validator.charAt(5)+""));
		mPasses[4] = Integer.parseInt(new String(validator.charAt(6)+""));
		mPasses[5] = Integer.parseInt(new String(validator.charAt(7)+""));
		
		try
		{
			mKey = new SecretKeySpec(getRawKey(mSeedKey.getBytes()), ALGORITHM);
			mOutCipher = Cipher.getInstance(ALGORITHM);
			mOutCipher.init(Cipher.ENCRYPT_MODE,mKey);
		
			mInCipher = Cipher.getInstance(ALGORITHM);
			mInCipher.init(Cipher.DECRYPT_MODE,mKey);
		}catch(Exception e) {	DBG_EXCEPTION(e);	}
		
	//dummy info
		mPasses[6] = Integer.parseInt(new String(validator.charAt(0)+""));
		mPasses[7] = Integer.parseInt(new String(validator.charAt(1)+""));
		mPasses[8] = Integer.parseInt(new String(validator.charAt(2)+"")+new String(validator.charAt(5)+""));
		mPasses[9] = Integer.parseInt(new String(validator.charAt(3)+"")+new String(validator.charAt(7)+""));
	}
	
	int mnPasses;
	
	public String encrypt(String str)
	{
		try {
			mnPasses++;
			
			byte[] data = str.getBytes();
			
			// if(mPasses < mPasses[4])
			// {
				// data[3] = 0;
				// data[5] = 0;
				// data[4] = 0;
			// }
			
			// Encrypt
			byte[] enc = mOutCipher.doFinal(data);
			
			// Encode bytes to base64 to get a string
			

			String value =  Base64.encode(enc);
			mValues[mnPasses-1] = new String(str);
			return value;
			
		} catch (Exception e) { 
			//DBG_EXCEPTION(e); 
		}
		return "";
	}
	
	public String decrypt(String str)
	{
		try {
			mnPasses++;
			
			byte[] data = Base64.decode(str);
			byte[] dec = mInCipher.doFinal(data);
			String value = new String(dec);
			mValues[mnPasses-1] = new String(value);
			return value;
		} catch (Exception e) { /*DBG_EXCEPTION(e);*/ }
		return null;
	}
	
	static String decryptBase64(int id)
	{
		Context c = SUtils.getContext();
		String value = c.getString(id);
		
		try{
			byte[] data = Base64.decode(value.getBytes());
			return new String(data);
		}catch(Exception e){
			DBG_EXCEPTION(e);
		}
		
		//dummy operations
		try{
			value = c.getString(id++);
			byte[] data = value.getBytes();
			byte[] dec = mStatic.mInCipher.doFinal(data);
			return new String(dec);
			//we dont need these information anyway
		}catch(Exception e){}
		
		return "";
	}
	
	
	static StringEncrypter mStatic;
#if USE_IN_APP_BILLING	
	public static String getString(int stringID)
	{
		Context c = SUtils.getContext();
		if(mStatic == null)
		{
			mStatic = new StringEncrypter(c.getString(IAB_STR_ENCODE_KEY));
			//JDUMP("SE",c.getString(IAB_STR_ENCODE_KEY));
		}
			
		String value = c.getString(stringID);
		String decode = mStatic.decryptBase64(stringID);
		//DBG("SE","value ["+value+"] decoded to: ["+decode+"]");
		return decode;
	}
#endif //#if USE_IN_APP_BILLING	
	
}
#endif//#if USE_IN_APP_BILLING || USE_BILLING