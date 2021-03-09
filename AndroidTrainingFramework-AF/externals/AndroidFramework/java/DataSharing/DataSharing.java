package APP_PACKAGE;

import APP_PACKAGE.GLUtils.SUtils; //for Context

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.net.Uri;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

public class DataSharing {

	SET_TAG("DataSharing");

	private static ArrayList<String> gameloftPackages = new ArrayList<String>();
	
	private static HashMap<String, String> externalProvidersValuesHM = new HashMap<String, String>();
	private static HashMap<String, String> myProviderValuesHM = new HashMap<String, String>();

	public static void LazyInit()
	{
		if(gameloftPackages.isEmpty())
		{			
			//this line will cause a compilation error if ACCESS_SHARED_DATA is not included in the AndroidManifest
			SUtils.getContext().getPackageManager().checkPermission(Manifest.permission.ACCESS_SHARED_DATA,STR_APP_PACKAGE);
			
			List<ProviderInfo> providers = SUtils.getContext().getPackageManager().queryContentProviders(null, 0, 0);
			for(int i = 0; i < providers.size(); i++) 
			{
				ProviderInfo p = providers.get(i);

				String pName = "";
				pName = p.authority;
				if (pName.startsWith("com.gameloft"))
				{
					gameloftPackages.add(pName);
				}
			}
			
			//with the list of content provider we will retrieve all the values from their DBs
			for (int i = 0; i < gameloftPackages.size(); i++)
			{
				Uri provider = Uri.parse("content://" + gameloftPackages.get(i) + "/key/");
				if(gameloftPackages.get(i).contains(SUtils.getContext().getPackageName()))
				{
					fillDBArray(provider, true); //true for local provider
				}
				else
				{
					fillDBArray(provider, false); //false for local provider
				}
			}
		}
	}

	private static void fillDBArray(Uri provider, boolean isLocalProvider)
	{
		if(isLocalProvider)
		{
			DBG(TAG, "+++++++++++++++++Local provider Begin+++++++++++++++++");
		}
		DBG(TAG, "------------------------------------");
		DBG(TAG, "Printing content for " + provider.toString());
		DBG(TAG, "------------------------------------");
		
		String[] columns = new String[] {"key", "value" };
		try
		{
			Cursor myCursor = SUtils.getContext().getContentResolver().query(provider, columns, null, null, null);
			if (myCursor != null)
			{
				DBG(TAG, "columns = " + myCursor.getColumnCount());
				DBG(TAG, "rows = " + myCursor.getCount());
				
				myCursor.moveToFirst();
				
				for(int i = 0; i < myCursor.getCount(); i++)
				{
					DBG(TAG, "For row " + i + ":");
					DBG(TAG, "key = " + myCursor.getString(0));
					DBG(TAG, "value = " + myCursor.getString(1));
					if(isLocalProvider)
					{
						myProviderValuesHM.put(myCursor.getString(0), myCursor.getString(1));
					}
					else
					{
						externalProvidersValuesHM.put(myCursor.getString(0), myCursor.getString(1));
					}
					if(!myCursor.isLast())
					{
						myCursor.moveToNext();
					}
					DBG(TAG, "---------------");
				}
				myCursor.close();//close it?
			}
			else
			{
				DBG(TAG, "cursor null");
			}
		}
		catch (Exception e)
		{
			DBG(TAG, "Error logging for provider " + provider.toString());
			DBG(TAG, "Error logging with exception " + e.toString());
		}

		if(isLocalProvider)
		{
			DBG(TAG, "+++++++++++++++++Local provider End+++++++++++++++++");
		}
	}

	public static String getSharedValue(final String key)
	{
		LazyInit();
		
		String result = "";
		if(myProviderValuesHM.containsKey(key))//check on local values
		{
			result = myProviderValuesHM.get(key);
			DBG(TAG, "Reading "+key+ " from local map = "+result);
			if(android.text.TextUtils.isEmpty(result))
			{
				result = "";
			}
			
			return result;
		}
	
		if(externalProvidersValuesHM.containsKey(key))//check on other GL apps
		{
			result = externalProvidersValuesHM.get(key);
			DBG(TAG, "Reading " + key + " from other GL app = "+result);
			if(android.text.TextUtils.isEmpty(result))
			{
				result = "";
			}
			//add it to local content provider
			myProviderValuesHM.put(key, result);
			final String resultToSave = result;
			new Thread()
			{
				public void run()
				{
					synchronized (gameloftPackages)
					{
						DBG(TAG, "Saving "+key+" to local provider by calling get");
						Uri myProvider = Uri.parse("content://" + SUtils.getContext().getPackageName() + ".KeyProvider/key/");
						AddOrUpdate(myProvider, key, resultToSave);
					}
				}
			}.start();
			return result;
		}
		DBG(TAG, key + " not found on local map or external apps.");
		return "";
	}
	
    public static void AddOrUpdate(Uri provider, String key, String value)
    {
		LazyInit();
		
	    //Try to UPDATE
	    ContentValues mNewValues = new ContentValues();
	    mNewValues.put("value", value);
		
		try
		{
			int retRows = SUtils.getContext().getContentResolver().update(provider, mNewValues, "key='" + key + "'", null);
			// Update failed, create it
			if (retRows < 1)
			{
				mNewValues.put("key", key);
				mNewValues.put("value", value);
				SUtils.getContext().getContentResolver().insert(provider, mNewValues);
			}
		}
		catch (Exception e)
		{
			DBG("DataSharing", "Error updating or creating " + value + " from provider " + provider.toString());
		}
    }


	
	public static void setSharedValue(final String key, final String value)
	{
		LazyInit();		

		DBG(TAG, "setSharedValue " + key + " with value=" + value);
		//cons: minimize the game and install a new one. return to game. save same value. it won't be saved in the recently installed game
		boolean isSameValueAsLocal = false;
		boolean isSameValueAsExternal = false;
		if(externalProvidersValuesHM.containsKey(key) && externalProvidersValuesHM.get(key).equals(value))
		{
			isSameValueAsExternal = true;
		}
		if(myProviderValuesHM.containsKey(key) && myProviderValuesHM.get(key).equals(value))
		{
			isSameValueAsLocal = true;
		}
		//if the local provider
		if(isSameValueAsLocal && isSameValueAsExternal)
		{
			DBG(TAG, "setSharedValue " + key + " with same value as the already saved. SKIP");
			return;
		}
		
		//save in local HashMap. If already present, it will overwrite the saved value.
		myProviderValuesHM.put(key, value);
		externalProvidersValuesHM.put(key, value);
		
		new Thread()
		{
			public void run()
			{
				synchronized (gameloftPackages)
				{
					DBG(TAG, "Start  Save " + key + " with value "+value);
					final int max = gameloftPackages.size();
					for (int i = 0; i < max; i++)
					{
						Uri provider = Uri.parse("content://" + gameloftPackages.get(i) + "/key/");
						AddOrUpdate(provider, key, value);
					}
				}
			}
		}.start();
	}
	
	public static void deleteSharedValue(final String key)
	{
		LazyInit();		

		//remove key from the map
		DBG(TAG, "deleteSharedValue "+key);
		if(!externalProvidersValuesHM.containsKey(key) && !myProviderValuesHM.containsKey(key))
		{
			DBG(TAG, "deleteSharedValue " + key + " with unexistent key");
			return;
		}
		
		myProviderValuesHM.remove(key);
		externalProvidersValuesHM.remove(key);
		
		new Thread()
		{
			public void run()
			{
				synchronized (gameloftPackages)
				{
					DBG(TAG, "Delete " + key);
					final int max = gameloftPackages.size();
					for (int i = 0; i < max; i++)
					{
						Uri provider = Uri.parse("content://" + gameloftPackages.get(i) + "/key/");
						try
						{
							SUtils.getContext().getContentResolver().delete(provider, "key='" + key + "'", null);
						}
						catch (Exception e)
						{
							DBG("DataSharing", "Error deleting " + key + " from provider " + provider.toString());
						}
					}
				}
			}
		}.start();
	}

	public static boolean isSharedValue(final String key)
	{
		LazyInit();
		
		String sharedValue = getSharedValue(key);
		if(sharedValue == null || sharedValue.equals(""))
			return false;
		else
			return true;
	}
}
