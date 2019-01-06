package com.example.android.findez.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by meets on 1/5/2019.
 */

public class FindEzContract {
    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.findez";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventory/inventory/ is a valid path for
     * looking at item data. content://com.example.android.inventory/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_FIND_EZ = "findEz";

    private FindEzContract() {
    }

    public static abstract class FindEzEntry implements BaseColumns {

        public static final String TABLE_NAME = "findEz";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_IMAGE = "image";
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_LOCATION = "location";
        public static final String COLUMN_ITEM_COMMENTS = "comments";


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of item.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FIND_EZ;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FIND_EZ;

        /**
         * Databse column datatypes
         */

        public static final String SQL_DELETE_TABLE = "DROP " + TABLE_NAME + ";";
        /**
         * The content URI to access the item data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FIND_EZ);

    }
}
