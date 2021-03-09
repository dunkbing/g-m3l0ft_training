package APP_PACKAGE.DRM.Verizon;

public class Encoder {

	/**
	 * 
	 * */
	public static String String2Blob(String str) {
		byte[] bs = str.getBytes();

		byte[] sBlob; //blob buffer

		int nBlobPos = 0; //current index of blob
		int nSPos = 0; //current index of string
		int nBitsNotUsed = 8; //how many bits not used in the current s[nSPos]
		byte nKeyIndex; //the index of key calculated using s

		//calculate blob length and allocate memory
		int nBlobLength = (str.length()) * 8 / 6;

		if ((((str.length()) * 8) % 6) != 0)
		{ //has remainder, need one more char to hold it
			nBlobLength += 2; //remainder + ending 0
		}

		else
		{
			nBlobLength++; //need space for ending 0
		}

		sBlob = new byte[nBlobLength];

		for (int k = 0; k < nBlobLength; ++k)
		{
			sBlob[k] = 0;
		}

		while (nSPos < str.length())
		{
			//get next 6 not used bits from s

			//first shift not used bits in current pos all the way to right
			nKeyIndex = (byte) (bs[nSPos] & 127);

			nKeyIndex = (byte) (nKeyIndex >> (8 - nBitsNotUsed));

			if (nBitsNotUsed < 6)
			{ //not enough bits in current pos
				if (++nSPos < str.length())
				{ //there's still char in the next pos, so use its bits (nSPos points to next pos now)
					//get 6-nBitsNotUsed number of bits from next char
					//to do so, we first shift the char in next pos to left nBitsNotUsed times
					//then | with leftover bits to form a 6 bit number
					nKeyIndex |= (bs[nSPos] << nBitsNotUsed);

					nBitsNotUsed += 2; //since 6-nBitsNotUsed bits is used from next char, 8-(6-nBitsNotUsed) will be left there
				}
				//else
				//there's no next char, just use what we have left in the current pos (nSPos is now pointing pass the end of s)
			} else { //enough bits not used in current pos
				nBitsNotUsed -= 6; //we used 6 more bits from current pos
			}

			//use right most 6 bits as key index
			nKeyIndex &= 63; //& with 00111111(63) to take only the right most 6 bits

			//then use index to get a char from key to fill the blob
			sBlob[nBlobPos] = SSEncDec_GetCharFromKeyByIndex(nKeyIndex);
			nBlobPos++;
		}

		String retval = new String(sBlob, 0, nBlobPos);
		return retval;
	}
	
	/**
	 * Used when converting from string to blob.
	 * */
	private static byte SSEncDec_GetCharFromKeyByIndex(byte nKeyIndex)
	{
		if (nKeyIndex < 26)
		{ //key index 0-25 is a-z
			return (byte) (nKeyIndex + (byte) 97); //convert 0...25 to a...z
		}

		else if (nKeyIndex < 52)
		{ //key index 26-51 is A-Z
			return (byte) (nKeyIndex + (byte) 39); //convert 26...51 to A...Z
		}

		else if (nKeyIndex < 62)
		{ //key index 52-61 is 0-9
			return (byte) ((byte) nKeyIndex - (byte) 4); //convert 52...61 to 0...9
		}

		else if (nKeyIndex == 62)
		{ //key index 62 is '_'
			return 95; //'_';
		}

		else
		{ //key index 63 is '-'
			return 45; //'-';
		}
	}
	
	/**
	 * 
	 * */
	public static String Blob2String(String blob)
	{
		if (blob == null)
			return null;
		byte[] b = blob.getBytes();

		byte[] s; //string buffer

		int nBlobPos = 0; //current index of blob
		int nSPos = 0; //current index of string
		int nBitsNotSet = 8; //how many bits not set in the current s[nSPos]
		byte nKeyIndex; //the index of key calculated using 

		// Calculate string length and allocate memory
		// It's ok to cut off the remainder, and we need space for the
		// termination character.
		int nStrLength = ((blob.length()) * 6 / 8) + 1;
		s = new byte[nStrLength];

		// Initialize the string.
		for (int k = 0; k < nStrLength; ++k)
		{
			s[k] = 0;
		}

		for (nBlobPos = 0; nBlobPos < blob.length(); nBlobPos++)
		{
			// Each character in the blob represents 6 bits in the decoded string.
			nKeyIndex = SSEncDec_GetKeyFromChar(b[nBlobPos]);

			// If "a" represents the bits of the key from the first character of
			// the blob, "b" the bits of the key from the second character in
			// the blob, "b" the bits of the key from the third character in the
			//  blob, etc.
			//  Then the decoded string would be comprised as follows:
			// s[0] = bbaaaaaa;
			// s[1] = ccccbbbb;
			// s[2] = ddddddcc;
			// s[3] = ffeeeeee;
			// .
			// .
			// .

			// Reconstruct the string's character value. First fill in it's low
			// order bits first. Then shift to the high order bits that haven't
			// been set yet.
			s[nSPos] |= nKeyIndex << (8 - nBitsNotSet);

			if (nBitsNotSet > 6)
			{
				// The low order 6 bits of this string character have been set.
				// Only the top 2 bits are left.
				nBitsNotSet -= 6;
			}

			else
			{
				if (nSPos < (nStrLength - 2))
				{
					// There may be bits in the key that haven't been used. Go to
					// the string's next character and place the remaining bits
					// from the key into the character's low order bits. When
					// nBitsNotSet is 6 all of the bits of the key have been used
					// and this code will not set anything in the next character.
					s[++nSPos] |= nKeyIndex >> nBitsNotSet;

					// (6 - nBitsNotSet) low order bits have been set in the
					// string's current character.

					// Increment in order to lineup with the high order bits of the
					// string's character that haven't been set yet.
					nBitsNotSet += 2;
				}
				// else {}
				// we filled all char in s, so what's left is just padding bits,
				// ignore them
			}
		} // End of for loop.

		String retval = new String(s, 0, nStrLength);
		return retval.trim();
	}
	
	/**
	 * Used when converting from blob to string.
	 * */
	private static byte SSEncDec_GetKeyFromChar(byte nChar)
	{
		if (nChar == '-')
		{
			return 63;
		} else if (nChar == '_')
		{
			return 62;
		} else if (nChar < 58)
		{//48-57 is '0'-'9', index is 52-61
			return (byte) (nChar + 4);
		} else if (nChar < 91)
		{//65-91 is 'A'-'Z', index is 26-51
			return (byte) (nChar - 39);
		} else
		{//97-122 is 'a'-'z', index is 0-25
			return (byte) (nChar - 97);
		}
	}
}
