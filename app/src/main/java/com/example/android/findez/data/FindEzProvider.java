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
        Cursor cursor;
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {
            case ITEMS_INFO:
                // For the ITEMS_INFO code, query the findez table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the findez table.
                cursor = database.query(FindEzContract.FindEzEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEMS_INFO_ID:
                // For the ITEMS_INFO_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.findez/item_info/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = FindEzContract.FindEzEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query on the findez table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(FindEzContract.FindEzEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //Set the notification uri on cursor
        //So we know what content uri the cursor is created for
        //If the data at this uri changes, then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
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
        validateUserInput(values);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(FindEzContract.FindEzEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //Notify all listeners that the data has changed for this Item Content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS_INFO:
                int idChanged = database.delete(FindEzContract.FindEzEntry.TABLE_NAME, selection, selectionArgs);
                if (idChanged != 0) {
                    //Notify all listeners that the data has changed for this items_info Content URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return idChanged;
            case ITEMS_INFO_ID:
                // For the ITEMS_INFO_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = FindEzContract.FindEzEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                int idChangedInRow = database.delete(FindEzContract.FindEzEntry.TABLE_NAME, selection, selectionArgs);
                if (idChangedInRow != 0) {
                    //Notify all listeners that the data has changed for this items_info Content URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return idChangedInRow;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS_INFO:
                int idChanged = updateItem(uri, values, selection, selectionArgs);
                if (idChanged != 0) {
                    //Notify all listeners that the data has changed for this item_info Content URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return idChanged;
            case ITEMS_INFO_ID:
                // For the ITEMS_INFO_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = FindEzContract.FindEzEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                int idChangedInRow = updateItem(uri, values, selection, selectionArgs);
                if (idChangedInRow != 0) {
                    //Notify all listeners that the data has changed for this item_info Content URI
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return idChangedInRow;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update item_info in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more item_info).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        validateUserInput(values);

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.update(FindEzContract.FindEzEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private void validateUserInput (ContentValues values){

        if (values.containsKey(FindEzContract.FindEzEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(FindEzContract.FindEzEntry.COLUMN_ITEM_NAME);
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Item requires a name which is not null");
            }
        }

        if (values.containsKey(FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION)) {
            String location = values.getAsString(FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION);
            if (location == null || location.isEmpty()) {
                throw new IllegalArgumentException("Item requires a location which is not null");
            }
        }
    }
}
