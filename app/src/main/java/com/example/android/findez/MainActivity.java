package com.example.android.findez;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;

import com.example.android.findez.data.FindEzContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private FirebaseAnalytics mFirebaseAnalytics;
    ItemsCursorAdapter itemsCursorAdapter;
    String mSearchString = null;
    String widgetSearch = null;

    @BindView(R.id.gv_items)
    GridView itemsGridView;

    @BindView(R.id.empty_view)
    View emptyView;

    @BindView(R.id.add_fab)
    FloatingActionButton fab;

    @BindView(R.id.app_bar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        //getSupportActionBar().setWindowTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Setup FAB to open EditorActivity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loggingFireBaseAnalytics(R.id.add_fab, getString(R.string.action_add),getString(R.string.floating_action_btn));
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);

            }
        });

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.

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
        handleIntent(getIntent());
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

        if (widgetSearch != null){
            searchView.setFocusable(true);
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        return true;
    }

    private void handleIntent(Intent intent) {

        if (intent.getStringExtra(FindEzWidgetProvider.WIDGET_SEARCH) != null) {
            widgetSearch = intent.getStringExtra(FindEzWidgetProvider.WIDGET_SEARCH);
        }
    }

    public void loggingFireBaseAnalytics (int id, String name, String type){
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

    }
}
