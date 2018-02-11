package de.psdev.devdrawer.appwidget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import de.psdev.devdrawer.DevDrawerApplication
import de.psdev.devdrawer.R
import de.psdev.devdrawer.activities.ClickHandlingActivity
import de.psdev.devdrawer.activities.WidgetConfigActivity
import de.psdev.devdrawer.database.WidgetConfig
import de.psdev.devdrawer.receivers.UpdateReceiver
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import mu.KLogging
import java.text.DateFormat
import java.util.*

class DDWidgetProvider: AppWidgetProvider() {

    companion object: KLogging() {

        private fun db(context: Context) = (context.applicationContext as DevDrawerApplication).devDrawerDatabase

        fun createRemoteViews(context: Context, appWidgetId: Int, title: String): RemoteViews {

            // Setup the widget, and data source / adapter
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val resources = context.resources
            val widget: RemoteViews = if (sharedPreferences.getString(resources.getString(R.string.pref_theme),
                    resources.getString(R.string.pref_theme_default)) == resources.getString(R.string.theme_light)) {
                RemoteViews(context.packageName, R.layout.widget_layout)
            } else {
                RemoteViews(context.packageName, R.layout.widget_layout_dark)
            }

            widget.setTextViewText(R.id.txt_title, title)
            widget.setTextViewText(R.id.txt_last_updated, DateFormat.getTimeInstance().format(Date()))

            val reloadPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, UpdateReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
            widget.setOnClickPendingIntent(R.id.btn_reload, reloadPendingIntent)

            val configActivityIntent = WidgetConfigActivity.createStartIntent(context, appWidgetId)
            configActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK)
            val configActivityPendingIntent = PendingIntent.getActivity(context, 0, configActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            widget.setOnClickPendingIntent(R.id.btn_settings, configActivityPendingIntent)

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

    @SuppressLint("CheckResult")
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val devDrawerDatabase = db(context)
        for (appWidgetId in appWidgetIds) {
            val widgets = devDrawerDatabase.widgetConfigDao().widgets(appWidgetId).blockingFirst()
            if (widgets.isEmpty()) {
                val widgetConfig = WidgetConfig(name = "unnamed_($appWidgetId)", widgetId = appWidgetId, color = ContextCompat.getColor(context, R.color.primary))
                devDrawerDatabase.widgetConfigDao()
                    .addWidgetAsync(widgetConfig)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        updateWidget(context, widgetConfig, appWidgetManager)
                    }
            } else {
                updateWidget(context, widgets.first(), appWidgetManager)
            }
        }
    }

    private fun updateWidget(context: Context, widgetConfig: WidgetConfig, appWidgetManager: AppWidgetManager) {
        try {
            val widget = createRemoteViews(context, widgetConfig.widgetId, widgetConfig.name)
            appWidgetManager.updateAppWidget(widgetConfig.widgetId, widget)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetConfig.widgetId, R.id.listView)
        } catch (e: Exception) {
            logger.warn(e) { "Error updating widget: ${widgetConfig.widgetId}: ${e.message}" }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val devDrawerDatabase = db(context)
        appWidgetIds.forEach {
            Completable.fromAction {
                devDrawerDatabase.widgetConfigDao().delete(it)
                devDrawerDatabase.packageFilterDao().deleteFiltersForWidget(it)
            }.subscribeOn(Schedulers.io()).subscribe()
        }
    }
}