package APP_PACKAGE;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class KeyProvider extends ContentProvider {

    private KeyDatabase mDB;

    private static final String AUTHORITY = STR_APP_PACKAGE + ".KeyProvider";
    public static final int KEY = 1;

    private static final String KEY_BASE_PATH = "key";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + KEY_BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/mt-key";

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, KEY_BASE_PATH, KEY);
    }

    @Override
    public boolean onCreate() {
        mDB = new KeyDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(KeyDatabase.TABLE_SHARE);

        int uriType = sURIMatcher.match(uri);
        
        if (KEY != uriType)
        	throw new IllegalArgumentException("Unknown URI");
        
        Cursor cursor = queryBuilder.query(mDB.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDB.getWritableDatabase();
        int rowsAffected = 0;
        
        if (KEY == uriType)
        	 rowsAffected = sqlDB.delete(KeyDatabase.TABLE_SHARE, selection, selectionArgs);
        else
        	throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
     
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

    @Override
    public String getType(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case KEY:
            return CONTENT_TYPE;
        default:
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        if (uriType != KEY) {
            throw new IllegalArgumentException("Invalid URI for insert");
        }
        SQLiteDatabase sqlDB = mDB.getWritableDatabase();
        long newID = sqlDB
                .insert(KeyDatabase.TABLE_SHARE, null, values);
        if (newID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, newID);
            getContext().getContentResolver().notifyChange(uri, null);
            return newUri;
        } else {
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDB.getWritableDatabase();

        int rowsAffected;

        if (KEY == uriType)
        	rowsAffected = sqlDB.update(KeyDatabase.TABLE_SHARE, values, selection, selectionArgs);
        else
            throw new IllegalArgumentException("Unknown URI");

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }
}
