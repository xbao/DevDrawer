package de.psdev.devdrawer.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import de.psdev.devdrawer.BaseActivity
import de.psdev.devdrawer.DevDrawerApplication
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.WidgetConfig
import de.psdev.devdrawer.database.WidgetConfigDao
import de.psdev.devdrawer.settings.SettingsActivity
import de.psdev.devdrawer.utils.consume
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import mu.KLogging

class MainActivity: BaseActivity() {

    companion object: KLogging() {
        private const val REQUEST_CODE = 42
    }

    private val onItemClickListener = { widget: WidgetConfig ->
        startActivityForResult(WidgetConfigActivity.createStartIntent(this, widget.widgetId), REQUEST_CODE)
    }

    private val devDrawerDatabase by lazy { (application as DevDrawerApplication).devDrawerDatabase }
    private val widgetConfigDao: WidgetConfigDao by lazy { devDrawerDatabase.widgetConfigDao() }
    private val subscriptions = CompositeDisposable()
    private val widgetAdapter by lazy { WidgetsListAdapter(onItemClickListener) }

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_main)

        actionBar?.apply {
            setDisplayShowTitleEnabled(true)
            title = "DevDrawer"
        }
        updateList()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        recycler.apply {
            adapter = widgetAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> consume { startActivity(Intent(this, SettingsActivity::class.java)) }
        R.id.action_info -> consume { TODO("Implement app info screen") }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            updateList()
        }
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private fun updateList() {
        subscriptions += widgetConfigDao.widgets()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                widgetAdapter.setItems(it)
            }
    }
}

