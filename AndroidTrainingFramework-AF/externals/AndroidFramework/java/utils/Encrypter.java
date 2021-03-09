
package APP_PACKAGE.GLUtils;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;


public class Encrypter {

    // Key to sync between encode and decode used for java and PHP
    private static String enrypt_key= getValue();
    
    public static String crypt(String input)
    {
        byte[] crypted = null;
        try{
            input = PadString(input);
            SecretKeySpec skey = new SecretKeySpec(Base64Coder.decodeLines(enrypt_key), "ECB");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            crypted = cipher.doFinal(input.getBytes());
        }catch(Exception e){
        }
        return new String(Base64Coder.encode(crypted));
    }

    public static String decrypt(String input)
    {
        byte[] output = null;
        try{
            SecretKeySpec skey = new SecretKeySpec(Base64Coder.decodeLines(enrypt_key), "ECB");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skey);
            output = cipher.doFinal(Base64Coder.decodeLines(input));
        }catch(Exception e){
        }
        return new String(output);
    }
    
    // Add enough block for fix sync with PHP
    public static String PadString(String in) 
    {
       int slen = (in.length() % 16);
       int i = (16 - slen);
       if ((i > 0) && (i < 16)){
           StringBuffer buf = new StringBuffer(in.length() + i);
           buf.insert(0, in);
           for (i = (16 - slen); i > 0; i--) {
             buf.append(" ");
           }
         return buf.toString();
       }
       else {
           return in;
       }
     }
    
    // Dummy constructor.
    private Encrypter() {}
    
    private static String getValue()
    {
    	byte[] aux = new byte[25];
    	byte[] vector = new byte[]
    	{
    		-48, -37, 4, 31, -39, -27, 52, -36, 41, 13, -32, 32,
    		-35, 20, 5, 11, -8, -33, -5, -9, 5, -6, -4, 0, 0
    	};
    	
    	byte start = 0x76;
    	
    	aux[0] = start;
    	for (int i = 1; i < 24; i++)
    	{
    		aux[i] = (byte)(aux[i-1] + vector[i]);
    	}
    	return new String(aux, 0, 24);
    }
    
}// end class Base64Coder