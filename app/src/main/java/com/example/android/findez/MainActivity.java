package com.example.android.findez;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.android.findez.data.FindEzContract;
import com.example.android.findez.data.FindEzDbHelper;

public class MainActivity extends AppCompatActivity {

    private FindEzDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        mDbHelper = new FindEzDbHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the items database.
     */
    private void displayDatabaseInfo() {
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FindEzContract.FindEzEntry._ID,
                FindEzContract.FindEzEntry.COLUMN_ITEM_NAME,
                FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION,
                FindEzContract.FindEzEntry.COLUMN_ITEM_COMMENTS,
                 };

        // Perform a query on the items table
        Cursor cursor = db.query(
                FindEzContract.FindEzEntry.TABLE_NAME,   // The table to query
                projection,            // The columns to return
                null,                  // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order

        TextView displayView = (TextView) findViewById(R.id.tv_temp_text);

        try {
            // Create a header in the Text View that looks like this:
            //
            // The Item table contains <number of rows in Cursor> items.
            // _id - name - breed - gender - weight
            //
            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column in this order.
            displayView.setText("The Item table contains " + cursor.getCount() + " items.\n\n");
            displayView.append(FindEzContract.FindEzEntry._ID + " - " +
                    FindEzContract.FindEzEntry.COLUMN_ITEM_NAME + " - " +
                    FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION + " - " +
                    FindEzContract.FindEzEntry.COLUMN_ITEM_COMMENTS + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(FindEzContract.FindEzEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_NAME);
            int itemLocationColumnIndex = cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION);
            int itemCommentsColumnIndex = cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_COMMENTS);


            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentLocation = cursor.getString(itemLocationColumnIndex);
                String currentComments = cursor.getString(itemCommentsColumnIndex);
                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" + currentID + " - " +
                        currentName + " - " +
                        currentLocation + " - " +
                        currentComments));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }
}
