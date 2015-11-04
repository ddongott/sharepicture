package com.example.ddong.xphoto;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ddong on 2015-09-15.
 */
public class XPDatabaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "XP.db";

    private static final int DATABASE_VERSION = 2;
    private final String mTable;


    public XPDatabaseHelper(Context context, final String table) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mTable = table;
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        final String CREATE_LOCAL_TABLE = "create table " + LocalGalleryActivity.TABLE_NAME +
                "( _id integer primary key autoincrement," +
                "path text not null," +
                "thumbnail text not null);";

        final String CREATE_SHARE_TABLE = "create table " + SharedPhotoManager.TABLE_NAME +
                "( _id integer primary key autoincrement," +
                "serverid text not null," +
                "owner text not null," +
                "data text not null);";

        database.execSQL(CREATE_LOCAL_TABLE);
        database.execSQL(CREATE_SHARE_TABLE);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database,int oldVersion,int newVersion){
        Log.w(XPDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS");
        onCreate(database);
    }
}
