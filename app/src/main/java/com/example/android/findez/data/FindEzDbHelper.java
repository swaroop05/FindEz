package com.example.android.findez.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by meets on 1/5/2019.
 */

public class FindEzDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "items.db";

    public FindEzDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + FindEzContract.FindEzEntry.TABLE_NAME + " (" + FindEzContract.FindEzEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FindEzContract.FindEzEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " + FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION + " TEXT NOT NULL, " + FindEzContract.FindEzEntry.COLUMN_ITEM_COMMENTS + " TEXT, " + FindEzContract.FindEzEntry.COLUMN_ITEM_IMAGE + " BLOB" + ");";
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(FindEzContract.FindEzEntry.SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
