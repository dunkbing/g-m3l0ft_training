package APP_PACKAGE;

public class n
{
	public static String QUIT = a();					// "Quitter"
	public static String SETTINGS = b();				// "Param\u00E8tres";
	public static String CANCEL = c();					// "Annuler";
	public static String NO_NETWORK_CONNECTION = d();	// "Oups \n Pour v\u00E9rifier vos droits d'utilisation, vous devez \u00EAtre connect\u00E9 au r\u00E9seau mobile Orange.";
	public static String TIMEOUT = e();					// "Oups \n Une erreur s'est produite et nous n'avons pas pu v\u00E9rifier vos droits d'utilisation. Merci de r\u00E9essayer ult\u00E9rieurement.";
	public static String OTHER_ERROR = f();				// "Oups \n Une erreur s'est produite et nous n'avons pas pu v\u00E9rifier vos droits d'utilisation.";
	public static String SERVER_INTERNAL_ERROR = g();	// "Oups \n Nous n'avons pas pu v\u00E9rifier vos droits d'utilisation.";
	public static String SERVER_BAD_PARAMETER = f();	// "Oups \n Une erreur s'est produite et nous n'avons pas pu v\u00E9rifier vos droits d'utilisation.";
	public static String SERVER_BAD_CONTENT = f();		// "Oups \n Une erreur s'est produite et nous n'avons pas pu v\u00E9rifier vos droits d'utilisation.";
	public static String SERVER_USER_NOT_ALLOWED = h();	// "Oups \n Il semble que vous n'ayez pas les droits n\u00E9c\u00E9ssaires pour utiliser cette application.";
	
	public static String a() // "Quitter"
	{
		byte[] input = { 81, 36, -12, 11, 0, -15, 13 };
		
		for (int i = 0; i < 7; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		
		return new String(input);
	}
	
	public static String b() // "Param\u00E8tres"
	{
		byte[] inputA = { 80, 17, 17, -17, 12 };
		byte[] inputB = { 116, -2, -13, 14 };
		
		for (int i = 0; i < 5; i++)
		{
			if (i != 0)
				inputA[i] = (byte)(inputA[i-1] + inputA[i]);
		}
		
		for (int i = 0; i < 4; i++)
		{
			if (i != 0)
				inputB[i] = (byte)(inputB[i-1] + inputB[i]);
		}
		
		return (new String(inputA)) + "\u00E8" + (new String(inputB));
	}
	
	public static String c() // "Annuler"
	{
		byte[] input = { 65, 45, 0, 7, -9, -7, 13 };
		
		for (int i = 0; i < 7; i++)
		{
			if (i != 0)
				input[i] = (byte)(input[i-1] + input[i]);
		}
		DBG("TESTY", "a\t" + a());
		DBG("TESTY", "a\t" + b());
		DBG("TESTY", "a\t" + d());
		DBG("TESTY", "a\t" + e());
		DBG("TESTY", "a\t" + f());
		DBG("TESTY", "a\t" + g());
		DBG("TESTY", "a\t" + h());
		return new String(input);
	}
	
	public static String d() // "Oups \n Pour v\u00E9rifier vos droits d'utilisation, vous devez \u00EAtre connect\u00E9 au r\u00E9seau mobile Orange.";
	{
		byte[] inputA = { 79, 38, -5, 3, -83, -22, 22, 48, 31, 6, -3, -82, 86 };
		byte[] inputB = { 114, -9, -3, 3, -4, 13, -82, 86, -7, 4, -83, 68, 14, -3, -6, 11, -1, -83, 68, -61, 78, -1, -11, 3, -3, 10, -18, 19, -11, 6, -1, -66, -12, 86, -7, 6, -2, -83, 68, 1, 17, -17, 21, -90 };
		byte[] inputC = { 116, -2, -13, -69, 67, 12, -1, 0, -9, -2, 17 };
		byte[] inputD = { 32, 65, 20, -85, 82 };
		byte[] inputE = { 115, -14, -4, 20, -85, 77, 2, -13, 7, 3, -7, -69, 47, 35, -17, 13, -7, -2, -55 };
		
		for (int i = 0; i < 13; i++)
		{
			if (i != 0)
				inputA[i] = (byte)(inputA[i-1] + inputA[i]);
		}
		
		for (int i = 0; i < 44; i++)
		{
			if (i != 0)
				inputB[i] = (byte)(inputB[i-1] + inputB[i]);
		}
		
		for (int i = 0; i < 11; i++)
		{
			if (i != 0)
				inputC[i] = (byte)(inputC[i-1] + inputC[i]);
		}
		
		for (int i = 0; i < 5; i++)
		{
			if (i != 0)
				inputD[i] = (byte)(inputD[i-1] + inputD[i]);
		}
		
		for (int i = 0; i < 19; i++)
		{
			if (i != 0)
				inputE[i] = (byte)(inputE[i-1] + inputE[i]);
		}
		
		return (new String(inputA)) + "\u00E9" + (new String(inputB)) + "\u00EA" + (new String(inputC)) + "\u00E9" + (new String(inputD)) + "\u00E9" + (new String(inputE));
	}
	
	public static String e() // "Oups \n Une erreur s'est produite et nous n'avons pas pu v\u00E9rifier vos droits d'utilisation. Merci de r\u00E9essayer ult\u00E9rieurement.";
	{
		byte[] inputA = { 79, 38, -5, 3, -83, -22, 22, 53, 25, -9, -69, 69, 13, 0, -13, 16, -3, -82, 83, -76, 62, 14, 1, -84, 80, 2, -3, -11, 17, -12, 11, -15, -69, 69, 15, -84, 78, 1, 6, -2, -83, 78, -71, 58, 21, -7, -1, 5, -83, 80, -15, 18, -83, 80, 5, -85, 86 };
		byte[] inputB = { 114, -9, -3, 3, -4, 13, -82, 86, -7, 4, -83, 68, 14, -3, -6, 11, -1, -83, 68, -61, 78, -1, -11, 3, -3, 10, -18, 19, -11, 6, -1, -64, -14, 45, 24, 13, -15, 6, -73, 68, 1, -69, 82 };
		byte[] inputC = { 101, 14, 0, -18, 24, -20, 13, -82, 85, -9, 8 };
		byte[] inputD = { 114, -9, -4, 16, -3, -13, 8, -8, 9, 6, -70 };
		
		for (int i = 0; i < 57; i++)
		{
			if (i != 0)
				inputA[i] = (byte)(inputA[i-1] + inputA[i]);
		}
		
		for (int i = 0; i < 43; i++)
		{
			if (i != 0)
				inputB[i] = (byte)(inputB[i-1] + inputB[i]);
		}
		
		for (int i = 0; i < 11; i++)
		{
			if (i != 0)
			{
				inputC[i] = (byte)(inputC[i-1] + inputC[i]);
				inputD[i] = (byte)(inputD[i-1] + inputD[i]);
			}
		}
		
		return (new String(inputA)) + "\u00E9" + (new String(inputB)) + "\u00E9" + (new String(inputC)) + "\u00E9" + (new String(inputD));
	}
	
	public static String f() // "Oups \n Une erreur s'est produite et nous n'avons pas pu v\u00E9rifier vos droits d'utilisation.";
	{
		byte[] inputA = { 79, 38, -5, 3, -83, -22, 22, 53, 25, -9, -69, 69, 13, 0, -13, 16, -3, -82, 83, -76, 62, 14, 1, -84, 80, 2, -3, -11, 17, -12, 11, -15, -69, 69, 15, -84, 78, 1, 6, -2, -83, 78, -71, 58, 21, -7, -1, 5, -83, 80, -15, 18, -83, 80, 5, -85, 86 };
		byte[] inputB = { 114, -9, -3, 3, -4, 13, -82, 86, -7, 4, -83, 68, 14, -3, -6, 11, -1, -83, 68, -61, 78, -1, -11, 3, -3, 10, -18, 19, -11, 6, -1, -64 };
		
		for (int i = 0; i < 57; i++)
		{
			if (i != 0)
				inputA[i] = (byte)(inputA[i-1] + inputA[i]);
		}
		
		for (int i = 0; i < 32; i++)
		{
			if (i != 0)
				inputB[i] = (byte)(inputB[i-1] + inputB[i]);
		}
		
		return (new String(inputA)) + "\u00E9" + (new String(inputB));
	}
	
	public static String g() // "Oups \n Nous n'avons pas pu v\u00E9rifier vos droits d'utilisation.";
	{
		byte[] inputA = { 79, 38, -5, 3, -83, -22, 22, 46, 33, 6, -2, -83, 78, -71, 58, 21, -7, -1, 5, -83, 80, -15, 18, -83, 80, 5, -85, 86 };
		byte[] inputB = { 114, -9, -3, 3, -4, 13, -82, 86, -7, 4, -83, 68, 14, -3, -6, 11, -1, -83, 68, -61, 78, -1, -11, 3, -3, 10, -18, 19, -11, 6, -1, -64 };
		
		for (int i = 0; i < 28; i++)
		{
			if (i != 0)
				inputA[i] = (byte)(inputA[i-1] + inputA[i]);
		}
		
		for (int i = 0; i < 32; i++)
		{
			if (i != 0)
				inputB[i] = (byte)(inputB[i-1] + inputB[i]);
		}
		
		return (new String(inputA)) + "\u00E9" + (new String(inputB));
	}
	
	public static String h() // "Oups \n Il semble que vous n'ayez pas les droits n\u00E9c\u00E9ssaires pour utiliser cette application.";
	{
		byte[] inputA = { 79, 38, -5, 3, -83, -22, 22, 41, 35, -76, 83, -14, 8, -11, 10, -7, -69, 81, 4, -16, -69, 86, -7, 6, -2, -83, 78, -71, 58, 24, -20, 21, -90, 80, -15, 18, -83, 76, -7, 14, -83, 68, 14, -3, -6, 11, -1, -83, 78 };
		byte[] inputB = { 115, 0, -18, 8, 9, -13, 14, -83, 80, -1, 6, -3, -82, 85, -1, -11, 3, -3, 10, -14, 13, -82, 67, 2, 15, 0, -15, -69, 65, 15, 0, -4, -3, -6, -2, 19, -11, 6, -1, -64 };
		
		for (int i = 0; i < 49; i++)
		{
			if (i != 0)
				inputA[i] = (byte)(inputA[i-1] + inputA[i]);
		}
		
		for (int i = 0; i < 40; i++)
		{
			if (i != 0)
				inputB[i] = (byte)(inputB[i-1] + inputB[i]);
		}
		
		return (new String(inputA)) + "\u00E9c\u00E9" + (new String(inputB));
	}
	
}