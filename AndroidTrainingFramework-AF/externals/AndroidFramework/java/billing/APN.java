
package APP_PACKAGE.billing;

import java.lang.Integer;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import APP_PACKAGE.GLUtils.SUtils;

public class APN
{
	/* * Information of all APNs */
	public static final Uri APN_TABLE_URI 		= Uri.parse("content://telephony/carriers");
	/* Information of the preferred APN	 */
	public static final Uri PREFERRED_APN_URI 	= Uri.parse("content://telephony/carriers/preferapn"); 

	SET_TAG("Wrapper");
	
	//fields
	/*
	"(_id INTEGER PRIMARY KEY,"
	"name TEXT,"
	"numeric TEXT,"
	"mcc TEXT,"
	"mnc TEXT,"
	"apn TEXT,"
	"user TEXT,"
	"server TEXT," 
	"password TEXT,"
	"proxy TEXT,"
	"port TEXT,"
	"mmsproxy TEXT,"
	"mmsport TEXT,"
	"mmsc TEXT,"
	"type TEXT,"
	"current INTEGER);");
	 */
	
	public static int getAPNID(String name)
	{
		//Cursor db = SUtils.getContext().getContentResolver().query(Uri.parse("content://telefphony/carriers"),new String[]{"_id"}, "name='"+name+"'",null,null);
		
		Cursor db = SUtils.getContext().getContentResolver().query(PREFERRED_APN_URI, null, null,null,null);
		try
		{
			
			db.moveToFirst();
			int id = Integer.parseInt(db.getString(0));
			do
			{
				String idx = db.getString(0);
				String apn = db.getString(1);
				if(apn.compareTo(name) == 0)
				{
					DBG("Billing","APN: ["+name+"] ID: "+idx);
					return Integer.parseInt(idx);
				}
				
			}while(db.moveToNext());
			
			DBG("Billing","APN: ["+name+"] ID: Not Found");
			return id;
		}catch(Exception e){}
		
		return -1;
	}
	
	public static int getPort(int apnID)
	{
		//DBG("Billing","Port: ["+apnID+"]");
		Cursor db = SUtils.getContext().getContentResolver().query(APN_TABLE_URI,new String[]{"port"}, "_id="+apnID,null,null);
		
		try
		{
			db.moveToFirst();
			int id = Integer.parseInt(db.getString(0));
			return id;
		}catch(Exception e){}
		
		return -1;
	}
	public static String getProxy(int apnID)
	{
		//DBG("Billing","proxy: ["+apnID+"]");
		Cursor db = SUtils.getContext().getContentResolver().query(APN_TABLE_URI,new String[]{"proxy"}, "_id="+apnID,null,null);
		try
		{
			db.moveToFirst();
			return db.getString(0);
		}catch(Exception e){}
		
		return null;
	}
}

