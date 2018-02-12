package de.psdev.devdrawer.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.widget.RemoteViews
import de.psdev.devdrawer.R
import de.psdev.devdrawer.activities.ClickHandlingActivity
import de.psdev.devdrawer.activities.MainActivity
import de.psdev.devdrawer.utils.Constants
import mu.KLogging
import java.text.DateFormat
import java.util.*

class DDWidgetProvider: AppWidgetProvider() {

    companion object: KLogging() {
        const val THEME_LIGHT = "Light"

        @JvmStatic
        fun createRemoteViews(context: Context, appWidgetId: Int): RemoteViews {
            // Setup the widget, and data source / adapter
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val widget: RemoteViews = if (sharedPreferences.getString("theme", THEME_LIGHT) == THEME_LIGHT) {
                RemoteViews(context.packageName, R.layout.widget_layout)
            } else {
                RemoteViews(context.packageName, R.layout.widget_layout_dark)
            }

            widget.setTextViewText(R.id.txt_last_updated, DateFormat.getTimeInstance().format(Date()))

            val reloadPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(Constants.ACTION_REFRESH_APPS).apply {
                setPackage(context.packageName)
            }, PendingIntent.FLAG_UPDATE_CURRENT)
            widget.setOnClickPendingIntent(R.id.btn_reload, reloadPendingIntent)

            val mainActivityIntent = MainActivity.createStartIntent(context).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            val mainActivityPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            widget.setOnClickPendingIntent(R.id.btn_settings, mainActivityPendingIntent)

            // Apps list
            val appListServiceIntent = Intent(context, DDWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("viewId", R.id.listView)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            widget.setRemoteAdapter(R.id.listView, appListServiceIntent)

            val clickIntent = Intent(context, ClickHandlingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val clickPI = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            widget.setPendingIntentTemplate(R.id.listView, clickPI)
            return widget
        }
    }

    // ==========================================================================================================================
    // AppWidgetProvider
    // ==========================================================================================================================

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            try {
                val widget = createRemoteViews(context, appWidgetId)
                appWidgetManager.updateAppWidget(appWidgetId, widget)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView)
            } catch (e: Exception) {
                logger.warn(e) { "Error updating widget: $appWidgetId: ${e.message}" }
            }
        }
    }

}