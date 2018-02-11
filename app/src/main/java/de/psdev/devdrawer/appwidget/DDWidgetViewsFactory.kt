package de.psdev.devdrawer.appwidget

import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import de.psdev.devdrawer.DevDrawerApplication
import de.psdev.devdrawer.R
import de.psdev.devdrawer.utils.Constants
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import mu.KLogging

class DDWidgetViewsFactory(private val context: Context, intent: Intent): RemoteViewsService.RemoteViewsFactory {

    companion object: KLogging()

    private val appWidgetId: Int by lazy { intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID) }
    private val viewId: Int by lazy { intent.getIntExtra("viewId", -1) }

    private val appWidgetManager: AppWidgetManager by lazy { AppWidgetManager.getInstance(context) }
    private val packageManager: PackageManager by lazy { context.packageManager }
    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    private val apps: MutableList<AppInfo> = mutableListOf()
    private val subscriptions = CompositeDisposable()

    // ==========================================================================================================================
    // RemoteViewsService.RemoteViewsFactory
    // ==========================================================================================================================

    override fun onCreate() {
        logger.debug { "onCreate" }
        subscriptions += Observable.create<RefreshListEvent> { emitter ->
            val receiver = object: BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    logger.debug { "Received $intent" }
                    emitter.onNext(RefreshListEvent)
                }
            }
            emitter.setCancellable { context.unregisterReceiver(receiver) }
            context.registerReceiver(receiver, IntentFilter(Constants.ACTION_REFRESH_APPS))

        }.subscribe {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, viewId)
            }
    }

    override fun onDataSetChanged() {
        logger.debug { "onDataSetChanged" }
        // Update the dataset
        loadApps()
    }

    override fun onDestroy() {
        logger.debug { "onDestroy" }
        subscriptions.clear()
    }

    override fun getCount(): Int = apps.size

    override fun getViewAt(position: Int): RemoteViews? {
        logger.debug { "getViewAt[position=$position]" }
        val (appName, packageName, appIcon) = apps[position]

        val rootClearCache = sharedPreferences.getBoolean("rootClearCache", false)

        // Setup the list item and intents for on click
        val row = RemoteViews(context.packageName,
            if (rootClearCache)
                R.layout.list_item_more
            else
                R.layout.list_item)

        try {
            row.setTextViewText(R.id.packageNameTextView, packageName)
            row.setTextViewText(R.id.appNameTextView, appName)
            row.setImageViewBitmap(R.id.imageView, convertFromDrawable(appIcon))

            if (sharedPreferences.getString("theme", "Light") == "Light") {
                row.setTextColor(R.id.appNameTextView, ContextCompat.getColor(context, R.color.app_name_light))
                row.setImageViewResource(R.id.appDetailsImageButton, R.drawable.settings_imageview)
                row.setImageViewResource(R.id.uninstallImageButton, R.drawable.delete_imageview)
                row.setImageViewResource(R.id.moreImageButton, R.drawable.more_imageview)
            } else {
                row.setTextColor(R.id.appNameTextView, ContextCompat.getColor(context, R.color.app_name_dark))
                row.setImageViewResource(R.id.appDetailsImageButton, R.drawable.settings_imageview_dark)
                row.setImageViewResource(R.id.uninstallImageButton, R.drawable.delete_imageview_dark)
                row.setImageViewResource(R.id.moreImageButton, R.drawable.more_imageview_dark)
            }

            val appDetailsClickIntent = Intent()
            val appDetailsClickExtras = Bundle().apply {
                //putBoolean("appDetails", true);
                putInt("launchType", Constants.LAUNCH_APP_DETAILS)
                putString(DDWidgetProvider.PACKAGE_STRING, packageName)
            }
            appDetailsClickIntent.putExtras(appDetailsClickExtras)
            row.setOnClickFillInIntent(R.id.appDetailsImageButton, appDetailsClickIntent)

            val uninstallClickIntent = Intent()
            val uninstallClickExtras = Bundle().apply {
                //appDetailsClickExtras.putBoolean("appDetails", true);
                putInt("launchType", Constants.LAUNCH_UNINSTALL)
                putString(DDWidgetProvider.PACKAGE_STRING, packageName)
            }
            uninstallClickIntent.putExtras(uninstallClickExtras)
            row.setOnClickFillInIntent(R.id.uninstallImageButton, uninstallClickIntent)

            val moreClickIntent = Intent()
            val moreClickExtras = Bundle().apply {
                putInt("launchType", Constants.LAUNCH_MORE)
                putString(DDWidgetProvider.PACKAGE_STRING, packageName)
            }
            moreClickIntent.putExtras(moreClickExtras)
            row.setOnClickFillInIntent(R.id.moreImageButton, moreClickIntent)

            val rowClickIntent = Intent()
            val rowClickExtras = Bundle().apply {
                //rowClickExtras.putBoolean("appDetails", false);
                putInt("launchType", Constants.LAUNCH_APP)
                putString(DDWidgetProvider.PACKAGE_STRING, packageName)
            }
            rowClickIntent.putExtras(rowClickExtras)
            row.setOnClickFillInIntent(R.id.touchArea, rowClickIntent)

            return row
        } catch (e: IndexOutOfBoundsException) {
            return null
        }

    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = apps[position].packageName.hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    // ==========================================================================================================================
    // Private APi
    // ==========================================================================================================================

    /**
     * Method to get all apps from the app database and add to the dataset
     */
    private fun loadApps() {
        val application = context.applicationContext as DevDrawerApplication

        val devDrawerDatabase = application.devDrawerDatabase

        val packageFilters = devDrawerDatabase.packageFilterDao()
            .filters()
            .blockingFirst(emptyList())
            .map { it.filter.replace("*", ".*").toRegex() }

        val appList = packageManager.getInstalledPackages(0)
            .filter {
                return@filter packageFilters.any { filter ->
                    return@any filter.matches(it.packageName)
                }
            }
            .mapNotNull {
                try {
                    val applicationInfo: ApplicationInfo = packageManager.getPackageInfo(it.packageName, PackageManager.GET_ACTIVITIES).applicationInfo
                    val appName = applicationInfo.loadLabel(packageManager).toString()
                    val appIcon = applicationInfo.loadIcon(packageManager)
                    return@mapNotNull AppInfo(appName, it.packageName, appIcon, it.firstInstallTime)
                } catch (e: Exception) {
                    logger.warn("Error: {}", e.message, e)
                }
                return@mapNotNull null
            }
            .sortedWith(getAppComparator())
            .distinct()
            .toList()

        apps.clear()
        apps.addAll(appList)
    }

    private fun getAppComparator(): Comparator<AppInfo> {
        val sortOrder = sharedPreferences.getString(Constants.PREF_SORT_ORDER, Constants.ORDER_INSTALLED)
        return when (sortOrder) {
            Constants.ORDER_INSTALLED -> compareBy { it.firstInstallTime }
            else -> compareBy { it.name }
        }
    }

    /**
     * Method to return a bitmap from drawable
     */
    private fun convertFromDrawable(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            getBitmapFromDrawable(drawable)
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    // ==========================================================================================================================
    // Inner classes
    // ==========================================================================================================================

    data class AppInfo(val name: String, val packageName: String, val appIcon: Drawable, val firstInstallTime: Long)

    object RefreshListEvent
}
