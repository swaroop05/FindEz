package com.example.android.findez;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.android.findez.data.FindEzContract;
import com.example.android.findez.data.FindEzDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ItemsCursorAdapter itemsCursorAdapter;
    String mSearchString = null;
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

        GridView itemsGridView = findViewById(R.id.gv_items);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemsGridView.setEmptyView(emptyView);
        itemsCursorAdapter = new ItemsCursorAdapter(this, null);
        itemsGridView.setAdapter(itemsCursorAdapter);
        itemsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = ContentUris.withAppendedId(FindEzContract.FindEzEntry.CONTENT_URI, id);
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri baseUri;
        baseUri = FindEzContract.FindEzEntry.CONTENT_URI;
        String selection = null;
        String[] selectionArgs = null;
        String searchString;
        String[] projection = {FindEzContract.FindEzEntry._ID,
                FindEzContract.FindEzEntry.COLUMN_ITEM_NAME,
                FindEzContract.FindEzEntry.COLUMN_ITEM_IMAGE};
        if (mSearchString != null ) {
            searchString = "%" + mSearchString + "%";
            selection = FindEzContract.FindEzEntry.COLUMN_ITEM_NAME + " LIKE ?";
            selectionArgs = new String[]{searchString};
        }
        return new CursorLoader(getApplicationContext(), baseUri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        itemsCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        itemsCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchString = query;
                getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    mSearchString = null;
                }else {
                    mSearchString = newText;
                }
                getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchString = null;
                getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                return false;
            }
        });

        return true;
    }
}
