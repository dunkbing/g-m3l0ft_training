package APP_PACKAGE.billing.common;



public class Crockford {
	
   private final char[] C =
		"0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

	public char[] encode32 (byte[] b) {
		// Douglas Crockford's Base32 Encoding
		// see http://www.crockford.com/wrmg/base32.html
		assert b != null;
		assert b.length <= Integer.MAX_VALUE / 8 * 5 - 4;
		char[] c = new char[(b.length + 4) / 5 * 8];
		int i = 0, j = 0;
		long x;
		while (i + 4 < b.length) {
			x = (long) (b[i + 0] & 0xff) << 32 | (long) (b[i + 1] & 0xff) << 24
				| (long) (b[i + 2] & 0xff) << 16 | (long) (b[i + 3] & 0xff) << 8
				| (long) (b[i + 4] & 0xff);
			c[j + 0] = C[(int) (x >> 35) & 0x1f];
			c[j + 1] = C[(int) (x >> 30) & 0x1f];
			c[j + 2] = C[(int) (x >> 25) & 0x1f];
			c[j + 3] = C[(int) (x >> 20) & 0x1f];
			c[j + 4] = C[(int) (x >> 15) & 0x1f];
			c[j + 5] = C[(int) (x >> 10) & 0x1f];
			c[j + 6] = C[(int) (x >> 5) & 0x1f];
			c[j + 7] = C[(int) (x >> 0) & 0x1f];
			i += 5;
			j += 8;
		}
		if (i + 4 == b.length) {
			x = (long) (b[i + 0] & 0xff) << 32 | (long) (b[i + 1] & 0xff) << 24
				| (long) (b[i + 2] & 0xff) << 16 | (long) (b[i + 3] & 0xff) << 8;
			c[j + 0] = C[(int) (x >> 35) & 0x1f];
			c[j + 1] = C[(int) (x >> 30) & 0x1f];
			c[j + 2] = C[(int) (x >> 25) & 0x1f];
			c[j + 3] = C[(int) (x >> 20) & 0x1f];
			c[j + 4] = C[(int) (x >> 15) & 0x1f];
			c[j + 5] = C[(int) (x >> 10) & 0x1f];
			c[j + 6] = C[(int) (x >> 5) & 0x1f];
			c[j + 7] = '=';
		} else if (i + 3 == b.length) {
			x = (long) (b[i + 0] & 0xff) << 32 | (long) (b[i + 1] & 0xff) << 24
				| (long) (b[i + 2] & 0xff) << 16;
			c[j + 0] = C[(int) (x >> 35) & 0x1f];
			c[j + 1] = C[(int) (x >> 30) & 0x1f];
			c[j + 2] = C[(int) (x >> 25) & 0x1f];
			c[j + 3] = C[(int) (x >> 20) & 0x1f];
			c[j + 4] = C[(int) (x >> 15) & 0x1f];
			c[j + 5] = '=';
			c[j + 6] = '=';
			c[j + 7] = '=';
		} else if (i + 2 == b.length) {
			x = (long) (b[i + 0] & 0xff) << 32 | (long) (b[i + 1] & 0xff) << 24;
			c[j + 0] = C[(int) (x >> 35) & 0x1f];
			c[j + 1] = C[(int) (x >> 30) & 0x1f];
			c[j + 2] = C[(int) (x >> 25) & 0x1f];
			c[j + 3] = C[(int) (x >> 20) & 0x1f];
			c[j + 4] = '=';
			c[j + 5] = '=';
			c[j + 6] = '=';
			c[j + 7] = '=';
		} else if (i + 1 == b.length) {
			x = (long) (b[i + 0] & 0xff) << 32;
			c[j + 0] = C[(int) (x >> 35) & 0x1f];
			c[j + 1] = C[(int) (x >> 30) & 0x1f];
			c[j + 2] = '=';
			c[j + 3] = '=';
			c[j + 4] = '=';
			c[j + 5] = '=';
			c[j + 6] = '=';
			c[j + 7] = '=';
		}
		return c;
	}
}