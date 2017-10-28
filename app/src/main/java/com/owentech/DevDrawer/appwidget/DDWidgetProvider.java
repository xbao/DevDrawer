package com.owentech.DevDrawer.appwidget;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 25/01/2013
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */

import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.activities.ClickHandlingActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class DDWidgetProvider extends AppWidgetProvider {

    public static String PACKAGE_STRING = "default.package";

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {

        for (int i = 0; i < appWidgetIds.length; i++) {
            final RemoteViews widget = getRemoteViews(context, appWidgetIds[i]);

            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    public static RemoteViews getRemoteViews(final Context context, final int appWidgetId) {
        // Setup the widget, and data source / adapter
        final Intent svcIntent = new Intent(context, DDWidgetService.class);

        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        final RemoteViews widget;

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        if (sp.getString("theme", "Light").equals("Light")) {
            widget = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        } else {
            widget = new RemoteViews(context.getPackageName(), R.layout.widget_layout_dark);
        }

        widget.setRemoteAdapter(R.id.listView, svcIntent);

        final Intent clickIntent = new Intent(context, ClickHandlingActivity.class);
        final PendingIntent clickPI = PendingIntent
            .getActivity(context, 0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        widget.setPendingIntentTemplate(R.id.listView, clickPI);
        return widget;
    }

}