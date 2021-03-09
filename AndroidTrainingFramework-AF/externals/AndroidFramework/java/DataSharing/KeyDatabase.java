package APP_PACKAGE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class KeyDatabase extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "gameloft_sharing";

    public static final String TABLE_SHARE = "glshare";
    public static final String COL_KEY = "key";
    public static final String COL_VALUE = "value";

    private static final String CREATE_TABLE_TUTORIALS = "create table "
            + TABLE_SHARE + " (" + COL_KEY
            + " text primary key, " + COL_VALUE + " text not null);";

    private static final String DB_SCHEMA = CREATE_TABLE_TUTORIALS;

    public KeyDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_SCHEMA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARE);
        onCreate(db);
    }
}
