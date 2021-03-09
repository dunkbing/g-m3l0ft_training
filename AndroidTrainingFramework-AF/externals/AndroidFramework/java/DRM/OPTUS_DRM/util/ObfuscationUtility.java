#if USE_OPTUS_DRM
package com.msap.store.drm.android.util;

import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * This class implements some utility methods for obfuscating and
 * unobfuscating data.
 * @author Edison Chan
 */
public class ObfuscationUtility {
	private final static String OBFUSCATOR_PBKDF2_PRF = "HmacSHA1";
	private final static String OBFUSCATOR_ENCRYPTION_ALGORITHM = "AES";
	private final static String OBFUSCATOR_ENCRYPTION_TRANSFORMATION = "AES/ECB/PKCS5Padding";

	/**
	 * Get the secret key used by the obfuscator.
	 * @param password password used for generation of secret key.
	 * @param salt salt used to protect the key.
	 * @param round number of round executed to generate the secret key.
	 * @return secret key.
	 * @throws GeneralSecurityException when the secret key cannot be generated.
	 */
	public static final byte[] generateKey(byte[] password, byte[] salt, int round) 
			throws GeneralSecurityException {
		Mac mac = Mac.getInstance(OBFUSCATOR_PBKDF2_PRF);
		mac.init(new SecretKeySpec(password, OBFUSCATOR_PBKDF2_PRF));
	
		int slen = salt.length;
		int olen = 16;
		byte[] asalt = new byte[slen + 4];
		byte[] buffer1 = new byte[mac.getMacLength()];
		byte[] buffer2 = new byte[olen];
	
		for (int i = 0; i < slen; i++) {
			asalt[i] = salt[i];
		}
	
		asalt[slen] = 0;
		asalt[slen + 1] = 0;
		asalt[slen + 2] = 0;
		asalt[slen + 3] = 1;
	
		mac.update(asalt);
		mac.doFinal(buffer1, 0);
	
		for (int j = 0; j < olen; j++) {
			buffer2[j] = buffer1[j];
		}
	
		for (int i = 1; i < round; i++) {
			mac.update(buffer1);
			mac.doFinal(buffer1, 0);

			for (int j = 0; j < olen; j++) {
				buffer2[j] ^= buffer1[j];
			}
		}

		return buffer2;
	}

	/**
	 * Obfuscate the input data using the obfuscation key.
	 * @param input data to be obfuscated.
	 * @param keydata obfuscation key generated from generateObfuscationKey.
	 * @return obfuscated data.
	 * @throws GeneralSecurityException when there are problem encrypting input data.
	 */
	public static final byte[] obfuscate(byte[] input, byte[] keydata) 
			throws GeneralSecurityException {
		if (input.length > 0) {
			Cipher cipher = Cipher.getInstance(OBFUSCATOR_ENCRYPTION_TRANSFORMATION);
			SecretKey key = new SecretKeySpec(keydata, OBFUSCATOR_ENCRYPTION_ALGORITHM);

			cipher.init(Cipher.ENCRYPT_MODE, key);

			return cipher.doFinal(input);
		} else {
			return input;
		}
	}

	/**
	 * Deobfuscate the input data using the obfuscation key.
	 * @param input data to be deobfuscated.
	 * @param keydata obfuscation key generated from generateObfuscationKey.
	 * @return deobfuscated data.
	 * @throws GeneralSecurityException when there are problem decrypting input data.
	 */
	public static final byte[] deobfuscate(byte[] input, byte[] keydata)
			throws GeneralSecurityException {
		if (input.length > 0) {
			Cipher cipher = Cipher.getInstance(OBFUSCATOR_ENCRYPTION_TRANSFORMATION);
			SecretKey key = new SecretKeySpec(keydata, OBFUSCATOR_ENCRYPTION_ALGORITHM);

			cipher.init(Cipher.DECRYPT_MODE, key);

			return cipher.doFinal(input);
		} else {
			return input;
		}
	}
}

#endif	//USE_OPTUS_DRM
