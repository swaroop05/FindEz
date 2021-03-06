package com.example.android.findez;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.findez.data.FindEzContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter the item's name
     */
    @BindView(R.id.ev_item_name)
    EditText mItemNameEditText;

    /**
     * EditText field to enter the item's location
     */
    @BindView(R.id.ev_item_location)
    EditText mItemLocationEditText;

    /**
     * EditText field to enter the item's comments
     */
    @BindView(R.id.ev_item_comments)
    EditText mItemCommentsEditText;

    /**
     * ImageView field to enter the item's image
     */
    @BindView(R.id.iv_item_image)
    ImageView mItemImageView;

    @BindView(R.id.loading_spinner_editor)
    ProgressBar mProgressbarView;

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
    private static final String KEY_ITEM_NAME = "item_name";
    private static final String KEY_ITEM_LOCATION = "item_location";
    private static final String KEY_ITEM_COMMENTS = "item_comments";
    private static final String KEY_ITEM_IMAGE_PATH = "item_image_path";
    private Bundle mSavedInstanceState;
    private boolean addingItem = false;
    private boolean mItemDetailsHasChanged = false;
    private static final String ADMOB_APP_ID = "ca-app-pub-2704977411494506~2297390896";
    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-2704977411494506/6508098646";
    private static final String ADMOB_AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/6300978111";
    private AdView mAdView;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mItemDetailsHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemDetailsHasChanged = true;
            return false;
        }
    };

    Uri mCurrentItemInfoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        mProgressbarView.setVisibility(View.GONE);
        MobileAds.initialize(this, ADMOB_APP_ID);
        Intent intent = getIntent();
        mCurrentItemInfoUri = intent.getData();
        requestPermissions();
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(ADMOB_AD_UNIT_ID_TEST);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (savedInstanceState != null) {
            if (savedInstanceState.getString(KEY_ITEM_NAME) != null) {
                mItemNameEditText.setText(savedInstanceState.getString(KEY_ITEM_NAME));
            }
            if (savedInstanceState.getString(KEY_ITEM_LOCATION) != null) {
                mItemLocationEditText.setText(savedInstanceState.getString(KEY_ITEM_LOCATION));
            }
            if (savedInstanceState.getString(KEY_ITEM_COMMENTS) != null) {
                mItemCommentsEditText.setText(savedInstanceState.getString(KEY_ITEM_COMMENTS));
            }
            if (savedInstanceState.getString(KEY_ITEM_IMAGE_PATH) != null) {
                picturePath = savedInstanceState.getString(KEY_ITEM_IMAGE_PATH);
            }
        }
        //Touch Listeners
        mItemNameEditText.setOnTouchListener(mTouchListener);
        mItemLocationEditText.setOnTouchListener(mTouchListener);
        mItemCommentsEditText.setOnTouchListener(mTouchListener);
        mItemImageView.setOnTouchListener(mTouchListener);
        if (mCurrentItemInfoUri != null) {
            setTitle(R.string.editor_activity_title_edit_item_info);
            getSupportLoaderManager().initLoader(EXISTING_ITEM_INFO_LOADER, null, this);
        } else {
            addingItem = true;
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


    private void insertItemDetails() {
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
        mProgressbarView.setVisibility(View.VISIBLE);
        if (mCurrentItemInfoUri != null) {
            new updateDeleteItemToDbTask().execute(values);
        } else {
            new InsertItemToDbTask().execute(values);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        //Hiding Delete Menu option for Add Item
        if (mCurrentItemInfoUri == null) {
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (validations()) {
                    insertItemDetails();

                }
                return true;
            case R.id.action_delete:
                deleteitem();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to validate the inputs
     *
     * @return
     */
    public boolean validations() {
        boolean result = false;
        String nameString = mItemNameEditText.getText().toString().trim();
        String locationString = mItemLocationEditText.getText().toString().trim();

        if (nameString.isEmpty() || nameString == null || locationString.isEmpty() || locationString == null || isImageNotPresent()) {
            displayToastMessage(getString(R.string.fields_mandatory_msg));
            return result;
        } else {
            result = true;
            return result;
        }
    }

    /**
     * Method to validate if image is present or not
     *
     * @return
     */
    public boolean isImageNotPresent() {
        if (addingItem) {
            if (picturePath == null) {
                if (makeImageMandatory) {
                    return true;
                    //true creates toast
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Request Permission if not already provided
     */
    public void requestPermissions() {
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


    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        mProgressbarView.setVisibility(View.VISIBLE);
        return new CursorLoader(getApplicationContext(), mCurrentItemInfoUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mProgressbarView.setVisibility(View.GONE);
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1 || mSavedInstanceState != null) {
            return;
        }
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_NAME));
            String location = cursor.getString(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_LOCATION));
            String comments = cursor.getString(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_COMMENTS));
            mItemNameEditText.setText(name);
            mItemLocationEditText.setText(location);
            if (comments != null) {
                mItemCommentsEditText.setText(comments);
            }
            byte[] image = null;
            try {
                image = cursor.getBlob(cursor.getColumnIndex(FindEzContract.FindEzEntry.COLUMN_ITEM_IMAGE));
                if (image != null) {

                    Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                    if (picturePath == null) {
                        mItemImageView.setImageBitmap(bmp);
                    }
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


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String nameString = mItemNameEditText.getText().toString().trim();
        String locationString = mItemLocationEditText.getText().toString().trim();
        String commentsString = mItemCommentsEditText.getText().toString().trim();


        if (!nameString.isEmpty()) {
            outState.putString(KEY_ITEM_NAME, nameString);
        }
        if (!locationString.isEmpty()) {
            outState.putString(KEY_ITEM_LOCATION, locationString);
        }
        if (!commentsString.isEmpty()) {
            outState.putString(KEY_ITEM_COMMENTS, commentsString);
        }
        if (picturePath != null) {
            outState.putString(KEY_ITEM_IMAGE_PATH, picturePath);
        }
        mSavedInstanceState = outState;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getString(KEY_ITEM_NAME) != null) {
            mItemNameEditText.setText(savedInstanceState.getString(KEY_ITEM_NAME));
        }
        if (savedInstanceState.getString(KEY_ITEM_LOCATION) != null) {
            mItemLocationEditText.setText(savedInstanceState.getString(KEY_ITEM_LOCATION));
        }
        if (savedInstanceState.getString(KEY_ITEM_COMMENTS) != null) {
            mItemCommentsEditText.setText(savedInstanceState.getString(KEY_ITEM_COMMENTS));
        }
        if (savedInstanceState.getString(KEY_ITEM_IMAGE_PATH) != null) {
            picturePath = savedInstanceState.getString(KEY_ITEM_IMAGE_PATH);
        }
    }

    @Override
    public void onBackPressed() {
        // If the Item hasn't changed, continue with handling back button press
        if (!mItemDetailsHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the Item in the database.
     */
    private void deleteitem() {
        if (mCurrentItemInfoUri != null) {
            DialogInterface.OnClickListener yesButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mProgressbarView.setVisibility(View.VISIBLE);
                            ContentValues values = null;
                            new updateDeleteItemToDbTask().execute(values);
                        }
                    };
            // Show dialog that there are unsaved changes
            showConfirmationDeleteDialog(yesButtonClickListener);
        }
    }

    /**
     * Method for Showing confirmation on Deleting an Item
     *
     * @param yesButtonClickListener
     */
    private void showConfirmationDeleteDialog(
            DialogInterface.OnClickListener yesButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure);
        builder.setPositiveButton(R.string.yes, yesButtonClickListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the Item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public class updateDeleteItemToDbTask extends AsyncTask<ContentValues, Void, Integer> {

        @Override
        protected Integer doInBackground(ContentValues... contentValues) {

            if (contentValues[0] != null) {
                return getContentResolver().update(mCurrentItemInfoUri, contentValues[0], null, null);
            } else {
                return getContentResolver().delete(mCurrentItemInfoUri, null, null);
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mProgressbarView.setVisibility(View.GONE);
            if (integer != 0) {
                displayToastMessage(getString(R.string.editor_update_delete_successful));

            } else {
                displayToastMessage(getString(R.string.editor_update_delete_failed));
            }
            finish();
        }
    }

    public class InsertItemToDbTask extends AsyncTask<ContentValues, Void, Uri> {

        @Override
        protected Uri doInBackground(ContentValues... contentValues) {
            return getContentResolver().insert(FindEzContract.FindEzEntry.CONTENT_URI, contentValues[0]);
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            mProgressbarView.setVisibility(View.GONE);
            if (uri == null) {
                // If the new content URI is null, then there was an error with insertion.
                displayToastMessage(getString(R.string.editor_update_delete_failed));
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                displayToastMessage(getString(R.string.editor_update_delete_successful));
            }
            finish();
        }
    }
}
