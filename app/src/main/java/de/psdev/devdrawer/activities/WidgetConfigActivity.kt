package de.psdev.devdrawer.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import de.psdev.devdrawer.DevDrawerApplication
import de.psdev.devdrawer.R
import de.psdev.devdrawer.adapters.FilterListAdapter
import de.psdev.devdrawer.adapters.PartialMatchAdapter
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.PackageFilterDao
import de.psdev.devdrawer.database.WidgetConfigDao
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.utils.Constants
import de.psdev.devdrawer.utils.consume
import de.psdev.devdrawer.utils.getExistingPackages
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_widget_config.*
import mu.KLogging

class WidgetConfigActivity: AppCompatActivity(), TextWatcher {

    companion object: KLogging() {

        fun createStartIntent(context: Context, appWidgetId: Int): Intent = Intent(context, WidgetConfigActivity::class.java).apply {
            action = "${Constants.ACTION_EDIT_FILTER}$appWidgetId"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
    }

    private val appWidgetId by lazy { getWidgetId() }
    private val devDrawerDatabase by lazy { (applicationContext as DevDrawerApplication).devDrawerDatabase }
    private val packageFilterDao: PackageFilterDao by lazy { devDrawerDatabase.packageFilterDao() }
    private val widgetConfigDao: WidgetConfigDao by lazy { devDrawerDatabase.widgetConfigDao() }
    private val appPackages: List<String> by lazy { packageManager.getExistingPackages() }
    private val packageNameCompletionAdapter: PartialMatchAdapter by lazy { PartialMatchAdapter(this, appPackages, devDrawerDatabase) }
    private val filterListAdapter: FilterListAdapter by lazy { FilterListAdapter(this, devDrawerDatabase, appWidgetId) }
    private val subscriptions = CompositeDisposable()
    private var widgetColor = -1
    private var widgetConfigId = -1

    // ==========================================================================================================================
    // Activity Lifecycle
    // ==========================================================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_widget_config)

        actionBar?.apply {
            setDisplayShowTitleEnabled(true)
            title = "Configure Widget"
        }
    }

    override fun onContentChanged() {
        super.onContentChanged()
        logger.error { "onContentChanged" }
        subscriptions += packageFilterDao.filtersForWidget(appWidgetId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                filterListAdapter.data = it
                logger.error { "setdata" }
            }

        subscriptions += widgetConfigDao.widgets(appWidgetId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val widgetConfig = it.firstOrNull()
                if (widgetConfig != null) {
                    txt_title.setText(widgetConfig.name, TextView.BufferType.EDITABLE)
                    widgetColor = widgetConfig.color
                    widgetConfigId = widgetConfig.id
                } else {
                    // should not happen
                    throw RuntimeException("No widget found")
                }
            }

        packagesFilterListView.adapter = filterListAdapter

        addPackageEditText.setAdapter(packageNameCompletionAdapter)
        addPackageEditText.addTextChangedListener(this)
        addButton.setOnClickListener { _ ->
            addFilter(addPackageEditText.text.toString(), appWidgetId)
        }
    }

    override fun onDestroy() {
        subscriptions.clear()
        logger.error { "onDestroy" }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Constants.EDIT_DIALOG_CHANGE -> {
                if (appWidgetId == data?.getIntExtra("appWidgetId", -1)) {
                    val id = data.getIntExtra("id", -1)
                    val newFilter = data.getStringExtra("newText")
                    packageFilterDao.updateFilter(id, newFilter, appWidgetId)
                }
            }
        }
    }

    // ==========================================================================================================================
    // TextWatcher
    // ==========================================================================================================================

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}

    override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}

    override fun afterTextChanged(editable: Editable) {
        packageNameCompletionAdapter.filter.filter(editable.toString())
    }

    // ==========================================================================================================================
    // Public API
    // ==========================================================================================================================

    private fun addFilter(filter: String, widgetId: Int) {
        if (filter.isNotEmpty() && widgetId == this.appWidgetId) {
            if (!filterListAdapter.data.map { it.filter }.contains(filter)) {
                logger.warn { "add filter ($filter) for widget:$widgetId" }
                subscriptions += packageFilterDao.addFilterAsync(PackageFilter(filter = filter, widgetId = widgetId))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onComplete = {
                        addPackageEditText?.setText("")
                        UpdateReceiver.send(this)
                    })
            } else {
                Toast.makeText(this, "Filter already exists", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, null)
        super.onBackPressed()
    }

    private fun saveWidget() {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            widgetConfigDao.updateWidget(widgetConfigId, txt_title.text.toString(), appWidgetId, widgetColor)
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val widget = DDWidgetProvider.createRemoteViews(this, appWidgetId, txt_title.text.toString())
            appWidgetManager.updateAppWidget(appWidgetId, widget)
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_configure_widget, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_confirm -> consume { saveWidget() }
        R.id.action_settings -> consume { TODO("Implement widget config screen") }
        else -> super.onOptionsItemSelected(item)
    }

    private fun getWidgetId(): Int = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        ?: AppWidgetManager.INVALID_APPWIDGET_ID

}