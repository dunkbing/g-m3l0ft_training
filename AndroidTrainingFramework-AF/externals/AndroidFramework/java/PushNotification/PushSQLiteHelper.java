#if SIMPLIFIED_PN
package APP_PACKAGE.PushNotification;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PushSQLiteHelper extends SQLiteOpenHelper {

	SET_TAG("PushNotification");
	
	public static final String DATABASE_NAME 	= "PN.db";
	public static final String TABLE_RECORDS 	= "Message";
	public static final String TABLE_ENDPOINT 	= "Endpoint";
	public static final String COLUMN_ID 		= "_id";
	public static final String COLUMN_PNID 		= "pn_alarm_id";
	public static final String COLUMN_CREATION 	= "creation_time";
	public static final String COLUMN_SCHEDULE 	= "schedule_time";
	public static final String COLUMN_GROUP 	= "message_group";
	public static final String COLUMN_TYPE 		= "type";
	public static final String COLUMN_CONTENT 	= "message_content";
	public static final String COLUMN_TRANSPORT	= "transport";
	public static final String COLUMN_REGID 	= "registration_id";
	public static final String DDMS_TOOL_FLAG 	= "pn_ddms_flag";
	private static final int DATABASE_VERSION 	= 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table " + TABLE_RECORDS + "(" + COLUMN_ID + " integer primary key autoincrement, " 
																						+ COLUMN_PNID + " text not null, "
																						+ COLUMN_CREATION + " text not null, "
																						+ COLUMN_SCHEDULE + " text not null, "
																						+ COLUMN_GROUP + " text not null, "
																						+ COLUMN_TYPE + " text not null, "
																						+ COLUMN_CONTENT + " text not null);";
																						
	// Database creation sql statement
	private static final String DB_CREATE_ENDPOINT = "create table " + TABLE_ENDPOINT + "(" + COLUMN_ID + " integer primary key, " 
																						+ COLUMN_TRANSPORT + " text not null, "
																						+ COLUMN_REGID + " text not null);";
	
	public PushSQLiteHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		INFO(TAG, "[PushSQLiteHelper]");
	}

	@Override
	public void onCreate(SQLiteDatabase database) 
	{
		INFO(TAG, "[onCreate]");
		database.execSQL(DATABASE_CREATE);
		database.execSQL(DB_CREATE_ENDPOINT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		INFO(TAG, "[onUpgrade]");
		DBG(TAG, "Upgrade: " + PushSQLiteHelper.class.getName() + " Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENDPOINT);
		onCreate(db);
	}
}
#endif
