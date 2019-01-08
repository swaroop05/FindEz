package com.example.android.findez;

import android.*;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.findez.data.FindEzContract;
import com.example.android.findez.data.FindEzDbHelper;
import com.example.android.findez.data.FindEzProvider;

import java.io.ByteArrayOutputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the item's name */
    private EditText mItemNameEditText;

    /** EditText field to enter the item's location */
    private EditText mItemLocationEditText;

    /** EditText field to enter the item's comments */
    private EditText mItemCommentsEditText;

    /** ImageView field to enter the item's image */
    private ImageView mItemImageView;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    int readPermission;
    int writePermission;
    private static final int PERMISSION_REQUEST_READ_STORAGE = 0;

    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 0;

    private static int RESULT_LOAD_IMAGE = 1;

    private boolean makeImageMandatory = false;

    String picturePath = null;

    private static final int EXISTING_ITEM_INFO_LOADER = 0;

    Uri mCurrentItemInfoUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        mCurrentItemInfoUri = intent.getData();
        // Find all relevant views that we will need to read user input from
        mItemNameEditText = findViewById(R.id.ev_item_name);
        mItemLocationEditText = findViewById(R.id.ev_item_location);
        mItemCommentsEditText = findViewById(R.id.ev_item_comments);
        mItemImageView = findViewById(R.id.iv_item_image);
        requestPermissions();

        if (mCurrentItemInfoUri != null) {
            setTitle(R.string.editor_activity_title_edit_item_info);
            getSupportLoaderManager().initLoader(EXISTING_ITEM_INFO_LOADER, null, this);
        } else {
            setTitle(R.string.editor_activity_title_add_item_info);

        }

        mItemImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (makeImageMandatory) {
                    Intent i = new Intent(
                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                } else {
                    displayToastMessage(getString(R.string.permission_denied_message));
                }
            }
        });
    }


    private void insertItemDetails (){
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mItemNameEditText.getText().toString().trim();
        String locationString = mItemLocationEditText.getText().toString().trim();
        String commentsString = mItemCommentsEditText.getText().toString().trim();
        Bitmap bitmap = null;
        byte[] imageData = null;
        if (!(picturePath == null)) {
            bitmap = BitmapFactory.decodeFile(picturePath);
            imageData = getBitmapAsByteArray(bitmap);
        } else {
            BitmapDrawable drawable = (BitmapDrawable) mItemImageView.getDrawable();
            bitmap = drawable.getBitmap();
            imageData = getBitmapAsByteArray(bitmap);
        }



        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(FindEzContract.FindEzEntry.COLUMN_ITEM_NAME, nameString);
        values.put(FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION, locationString);
        values.put(FindEzContract.FindEzEntry.COLUMN_ITEM_COMMENTS, commentsString);
        if (imageData != null) {
            values.put(FindEzContract.FindEzEntry.COLUMN_ITEM_IMAGE, imageData);
        }



        Uri uri = null;
        int changedRowID = 0;
        if (mCurrentItemInfoUri != null) {
            //TODO Update this item once CursorLoaders and CursorAdapters is implemented
        } else {
            uri = getContentResolver().insert(FindEzContract.FindEzEntry.CONTENT_URI, values);
            if (uri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
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

    /**
     * Request Permission if not already provided
     */
    public void requestPermissions(){
        readPermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        writePermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!(readPermission == PERMISSION_REQUEST_READ_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_STORAGE);
        } else {
            makeImageMandatory = true;
        }

        if (!(writePermission == PERMISSION_REQUEST_WRITE_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeImageMandatory = true;
                } else {
                    makeImageMandatory = false;
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * Method to complete the image pick from gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            mItemImageView.setImageBitmap(bitmap);
        }
    }


    /**
     * Method to show Toast messages
     *
     * @param message
     */
    public void displayToastMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        return outputStream.toByteArray();

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(getApplicationContext(), mCurrentItemInfoUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
// Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {


            String name = cursor.getString(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_NAME));
            String location = cursor.getString(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION));
            String comments = cursor.getString(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_COMMENTS));
            mItemNameEditText.setText(name);
            mItemLocationEditText.setText(location);
            if (comments != null){
                mItemCommentsEditText.setText(comments);
            }



            byte[] image = null;
            try {
                image = cursor.getBlob(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_IMAGE));
                if (image != null){

                    Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                    mItemImageView.setImageBitmap(bmp);
                }

            } catch (Exception e) {
                Log.d("cursor problem,", cursor.getBlob(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_IMAGE)).toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mItemNameEditText.setText("");
        mItemLocationEditText.setText("");
        mItemCommentsEditText.setText("");
    }
}
