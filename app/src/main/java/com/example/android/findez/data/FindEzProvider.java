package com.example.android.findez.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by meets on 1/6/2019.
 */

public class FindEzProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = FindEzProvider.class.getSimpleName();

    /**
     * Database helper object
     */
    private FindEzDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the findez table
     */
    private static final int ITEMS_INFO = 100;

    /**
     * URI matcher code for the content URI for a single item in the findez table
     */
    private static final int ITEMS_INFO_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(FindEzContract.CONTENT_AUTHORITY, FindEzContract.PATH_ITEMS_INFO, ITEMS_INFO);
        sUriMatcher.addURI(FindEzContract.CONTENT_AUTHORITY, FindEzContract.PATH_ITEMS_INFO + "/#", ITEMS_INFO_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new FindEzDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS_INFO:
                return FindEzContract.FindEzEntry.CONTENT_LIST_TYPE;
            case ITEMS_INFO_ID:
                return FindEzContract.FindEzEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS_INFO:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for URI " + uri);
        }
    }

    /**
     * Method to insert Item to db.
     *
     * @param uri
     * @param values
     * @return
     */
    public Uri insertItem(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(FindEzContract.FindEzEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //Notify all listeners that the data has changed for this inventory Content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
