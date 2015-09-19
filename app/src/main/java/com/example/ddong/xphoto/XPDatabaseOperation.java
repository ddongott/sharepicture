package com.example.ddong.xphoto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ddong on 2015-09-15.
 */
public class XPDatabaseOperation {
    private XPDatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String PHOTO_ID="_id"; // id
    public final static String PHOTO_PATH="path";  // path of photo
    public final static String PHOTO_THUMB="thumbnail";  // path of thumbnail
    private static String mTable;
    /**
     *
     * @param context
     */
    public XPDatabaseOperation(Context context, final String table){
        dbHelper = new XPDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        mTable = table;
    }


    public long createRecords(String path, String thumbnail){
        ContentValues values = new ContentValues();
        values.put(PHOTO_PATH, path);
        values.put(PHOTO_THUMB, thumbnail);
        return database.insert(mTable, null, values);
    }

    public Cursor selectRecords() {
        String[] cols = new String[] {PHOTO_ID, PHOTO_PATH, PHOTO_THUMB};
        Cursor mCursor = database.query(true, mTable,cols,null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }
}
