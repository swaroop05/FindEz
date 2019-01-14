package com.example.android.findez;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class FindEzWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_SEARCH = "search_from_widget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.find_ez_widget_provider);
        Intent addItemIntent = new Intent(context, EditorActivity.class);
        PendingIntent addItemPendingIntent = PendingIntent.getActivity(context, 0, addItemIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_btn_add_item, addItemPendingIntent);

        Intent searchItemIntent = new Intent(context, MainActivity.class);
        searchItemIntent.putExtra(WIDGET_SEARCH, "search");
        PendingIntent searchItemPendingIntent = PendingIntent.getActivity(context, 0, searchItemIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_btn_search_item, searchItemPendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

