#if USE_IN_APP_BILLING
package APP_PACKAGE.billing.common;
 
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;

import APP_PACKAGE.iab.InAppBilling;
 
public class DefReader
{
	private static DefReader mThis = null;
	public DefReader()
	{
	
	}
	
	public static DefReader getInstance()
	{
		if (mThis == null)
			mThis = new DefReader();
		return mThis;	
	}
	
	private final String ALGORITHM = "AES";

	public String getPlainDef(String input, String key)
    {
    	String ret = null;
    	try
    	{
	    	String mSeedKey = key;
			SecretKeySpec mKey = new SecretKeySpec(getRawKey(mSeedKey), ALGORITHM);

			Cipher mInCipher = Cipher.getInstance(ALGORITHM);
			mInCipher.init(Cipher.DECRYPT_MODE,mKey);
			
			byte[] data = Base64.decode(input);
			byte[] dec = mInCipher.doFinal(data);
			ret = new String (dec);
	    }catch (Exception e)
	    {
	    	DBG_EXCEPTION(e);
	    }
    	return ret;
    }
	
	private byte[] getRawKey(String seed) throws Exception 
	{
		/*KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);  
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG"); //cannot use SecureRandom, to generate the PRNG cause no provider match found between Java and Android
		sr.setSeed(seed.getBytes());  
		kgen.init(128, sr); // 192 and 256 bits may not be available  
		SecretKey skey = kgen.generateKey();  
		byte[] raw = skey.getEncoded();
		*/
		byte[] raw = hf16(seed).getBytes();
		return raw;
	}
	
	private String hf16(String input)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			MessageDigest digest = MessageDigest.getInstance(InAppBilling.GET_STR_CONST(IAB_SHA_256));
			digest.reset();
			digest.update(input.getBytes("UTF-8"));
			byte[] byteData = digest.digest();
			for (int i = 0; i < byteData.length; i++){
			  sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));//Parsing to Hex String
			}
		}
		catch(Exception e)
		{
			DBG_EXCEPTION(e);
		}
		return sb.toString().substring(0, 16);//we only have to use 16 bytes for the key
	}
	
	
}
#endif