package APP_PACKAGE.GLUtils;

import java.io.InputStream;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.Context;

class ResLoader
{
	SET_TAG("ResLoader");
	static final int WARNING_SIZE = 1*1024*1024; //1MB
	private static String trimName(String file)
	{
		if (file.startsWith(".//"))
		{
			file = file.substring(3);
		}
		else if (file.startsWith("./"))
		{
			file = file.substring(2);
		}
		file = file.trim();
		// String name = file.substring(file.lastIndexOf('/') + 1).toLowerCase();
		// int ext = name.indexOf('.');
		// if (ext != -1)
		// {
			// name = name.substring(0, ext);
		// }
		
		 
		return file;
	}
	
	private static InputStream getInputStream(String filename)
	{
		InputStream mIS;
		String apkName = trimName(filename);
		//DBG(TAG, "filename: ["+apkName+"]");
		try
		{
			AssetManager assets = SUtils.getContext().getAssets();
			mIS = assets.open(apkName);
			if(mIS != null) return mIS;
		}
		catch (Exception e)	{	/*DBG_EXCEPTION(e);*/	}
		
		// // look for file on protected resources, drawable
		// String path = filename.substring(0, filename.lastIndexOf('/') + 1);
		// String protectedRes = path + "res_" + apkName;
		// DBG(TAG, "path: "+path);
		// DBG(TAG, "protectedRes: "+protectedRes);
		// Resources res = SUtils.getContext().getResources();
		// int ID = res.getIdentifier(protectedRes, "assets", SUtils.getContext().getPackageName());
		// int temp = 0;
		// DBG(TAG, "ID: "+ID);
		// if (ID != 0)
		// {
			// try
			// {
				// mIS = res.openRawResource(ID);
				// if (mIS != null)
				// {
					// return mIS;
				// }
			// }
			// catch (Exception ex) { return null; }
		// }
		
		
		return null;
	}
	
	public static int getLength(String filename)
	{
		
		InputStream mIS = getInputStream(filename);
		
		if(mIS != null)
		{
			try {
				int value = mIS.available();
				mIS.close();	mIS = null;
				return value;
			} catch (Exception e)	{	/*DBG_EXCEPTION(e);*/	}
		}
		return 0;
	}

	public static byte[] getBytes(String s)
	{
		InputStream mIS = getInputStream(s);
		
		byte[] data = null;
		if (mIS != null)
		{
			try
			{
				int resLength = mIS.available();
				if (resLength > WARNING_SIZE)
				{
					WARN(TAG, "Warning: You are opening a file with "+resLength+", you will create a byte array with this size, considere split these file");
				}
				data = new byte[resLength];
				mIS.read(data, 0, resLength);
				mIS.close();	mIS = null;
				return data;
			}
			catch (Exception ex) { 	/*DBG_EXCEPTION(ex);*/	}
		}
		return data;
	}

	public static byte[] getBytes(int id)
	{
		try
		{
			Resources res = SUtils.getContext().getResources();
			InputStream mIS = res.openRawResource(id);
			int resLength = mIS.available();
			if (resLength > WARNING_SIZE)
			{
				WARN(TAG, "Warning: You are opening a file with "+resLength+", you will create a byte array with this size, considere split these file");
			}
			byte[]data = new byte[resLength];
			mIS.read(data, 0, resLength);
			mIS.close();
			return data;
		}
		catch (Exception ex) { 	/*DBG_EXCEPTION(ex);*/	}
		
		return null;
	}

	// public static byte[] getBytes(String s, int offset, int loadSize) {
		
		// InputStream mIS = getInputStream(s);
		// byte[] data = null;
		// int resLength = 0;
		
		// if (mIS != null) 
		// {
			// try
			// {
				// data = new byte[loadSize];
				// mIS.skip(offset);
				// mIS.read(data, 0,loadSize);
				// mIS.close();
				// mIS = null;
			// }
			// catch (Exception ex)	{	DBG_EXCEPTION(ex);	}
		// }
		
		// return data;
	// }
	
/////////////////////////////////////////////////////////////
//			NATIVE
/////////////////////////////////////////////////////////////
}