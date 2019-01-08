package com.example.android.findez;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
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

    //TODO: Remove this method after ContentProvider is implemented
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
                FindEzContract.FindEzEntry.COLUMN_ITEM_IMAGE,
                 };
        Uri baseUri;
        baseUri = FindEzContract.FindEzEntry.CONTENT_URI;
        Cursor cursor = getContentResolver().query(baseUri,projection,null,null,null);

        GridView itemsGridView = findViewById(R.id.gv_items);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemsGridView.setEmptyView(emptyView);

        ItemsCursorAdapter itemsCursorAdapter = new ItemsCursorAdapter(this, cursor);
        itemsGridView.setAdapter(itemsCursorAdapter);
    }
}
