#if USE_OPTUS_DRM
package com.msap.store.drm.android.util;

/**
 * Utilities for encoding and decoding of hex-encoded binary data.
 * @author Edison Chan
 */
public class HexEncoding {
	private final static char[] HEXMAP = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Encode a byte array using hex encoding.
	 * @param input data to be encoded.
	 * @return the string containing the encoded data.
	 */
	public final static String encode(byte[] input) {
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < input.length; i++) {
			buffer.append(HEXMAP[(input[i] & 0xf0) >> 4]);
			buffer.append(HEXMAP[input[i] & 0x0f]);
		}

		return buffer.toString();
	}

	/**
	 * Decode a string with hex encoded data back to the original input.
	 * @param input string to be decoded.
	 * @return the decoded data in byte array.
	 */
	public final static byte[] decode(String input) {
		char[] buffer = input.toCharArray();
		byte[] result = new byte[buffer.length / 2];

		for (int i = 0; i < result.length; i++) {
			int j = 2 * i;
			int t = Character.digit(buffer[j], 16) * 16 + Character.digit(buffer[j + 1], 16);
			result[i] = (byte) (0xff & t);
		}

		return result;
	}
};

#endif	//USE_OPTUS_DRM
