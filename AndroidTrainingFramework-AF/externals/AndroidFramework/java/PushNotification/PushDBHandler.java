#if SIMPLIFIED_PN
package APP_PACKAGE.PushNotification;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PushDBHandler {

	SET_TAG("PushNotification");

	private SQLiteDatabase 		database;
	private PushSQLiteHelper 	dbHelper;
	
	private String[] allColumns = {	PushSQLiteHelper.COLUMN_ID, 
									PushSQLiteHelper.COLUMN_PNID, 
									PushSQLiteHelper.COLUMN_CREATION, 
									PushSQLiteHelper.COLUMN_SCHEDULE, 
									PushSQLiteHelper.COLUMN_GROUP, 
									PushSQLiteHelper.COLUMN_TYPE, 
									PushSQLiteHelper.COLUMN_CONTENT};
									
	private String[] endpointColumns = {PushSQLiteHelper.COLUMN_ID, 
									PushSQLiteHelper.COLUMN_TRANSPORT, 
									PushSQLiteHelper.COLUMN_REGID};

	public PushDBHandler(Context context)
	{
		dbHelper = new PushSQLiteHelper(context);
	}

	public void open()
	{
		try{
		database = dbHelper.getWritableDatabase();
		INFO(TAG, "*** PNDataBasePath: " + database.getPath());
		}catch(SQLException e){DBG_EXCEPTION(e);}
	}

	public void close()
	{
		INFO(TAG, "[close]");
		try{dbHelper.close();}catch(Exception e){DBG_EXCEPTION(e);}
	}

	public void createPNRecord(ContentValues values)
	{
		INFO(TAG, "[createPNRecord]");
		try{
		long insertId = database.insert(PushSQLiteHelper.TABLE_RECORDS, null, values);
		Cursor cursor = database.query(PushSQLiteHelper.TABLE_RECORDS, allColumns, PushSQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		cursor.close();
		}catch(Exception e){DBG_EXCEPTION(e);}
	}

	public void deletePNRecord(String columnID, String value)
	{
		INFO(TAG, "[deletePNRecord]");
		try{
		database.delete(PushSQLiteHelper.TABLE_RECORDS, columnID + "=?", new String[] { value });
		}catch(Exception e){DBG_EXCEPTION(e);}
	}
	
	public void updateEndpointRecord(ContentValues values)
	{
		INFO(TAG, "[updateEndpointRecord]");
		try{
		database.replace(PushSQLiteHelper.TABLE_ENDPOINT, null, values);
		}catch(Exception e){DBG_EXCEPTION(e);}
	}
} 
#endif
