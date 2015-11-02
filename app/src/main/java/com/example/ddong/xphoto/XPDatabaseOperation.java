package com.example.ddong.xphoto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by ddong on 2015-09-15.
 */
public class XPDatabaseOperation {
    private XPDatabaseHelper dbHelper;

    private SQLiteDatabase database;


    private static String mTable;
    /**
     *
     * @param context
     */
    public XPDatabaseOperation(Context context, final String table){
        dbHelper = new XPDatabaseHelper(context, table);
        database = dbHelper.getWritableDatabase();
        mTable = table;
    }

    public long createRecords(JSONObject jobj){
        ContentValues values = new ContentValues();
        try {
            for (Iterator<String> keys = jobj.keys(); keys.hasNext();) {
                String key = keys.next();
                values.put(key, jobj.getString(key));
            }
            return database.insert(mTable, null, values);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Cursor selectRecords(String[] cols) {
        Cursor mCursor = database.query(true, mTable,cols,null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }
}
