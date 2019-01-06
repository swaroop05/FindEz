package com.example.android.findez;

import android.*;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.findez.data.FindEzContract;
import com.example.android.findez.data.FindEzDbHelper;

public class EditorActivity extends AppCompatActivity {

    /** EditText field to enter the item's name */
    private EditText mItemNameEditText;

    /** EditText field to enter the item's location */
    private EditText mItemLocationEditText;

    /** EditText field to enter the item's comments */
    private EditText mItemCommentsEditText;

    /** ImageView field to enter the item's image */
    private ImageView mItemImageView;

    int readPermission;
    int writePermission;
    private static final int PERMISSION_REQUEST_READ_STORAGE = 0;

    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        // Find all relevant views that we will need to read user input from
        mItemNameEditText = findViewById(R.id.ev_item_name);
        mItemLocationEditText = findViewById(R.id.ev_item_location);
        mItemCommentsEditText = findViewById(R.id.ev_item_comments);
        mItemImageView = findViewById(R.id.iv_item_image);
        requestPermissions();
    }

    private void insertItemDetails (){
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mItemNameEditText.getText().toString().trim();
        String locationString = mItemLocationEditText.getText().toString().trim();
        String commentsString = mItemCommentsEditText.getText().toString().trim();

        // Create database helper
        FindEzDbHelper mDbHelper = new FindEzDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(FindEzContract.FindEzEntry.COLUMN_ITEM_NAME, nameString);
        values.put(FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION, locationString);
        values.put(FindEzContract.FindEzEntry.COLUMN_ITEM_COMMENTS, commentsString);

        // Insert a new row for item in the database, returning the ID of that new row.
        long newRowId = db.insert(FindEzContract.FindEzEntry.TABLE_NAME, null, values);
        // Show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, "Error with saving item", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, "Item saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                insertItemDetails();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void requestPermissions(){
        readPermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        writePermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!(readPermission == PERMISSION_REQUEST_READ_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_STORAGE);
        }

        if (!(writePermission == PERMISSION_REQUEST_WRITE_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_STORAGE);
        }
    }
}
