package de.psdev.devdrawer.activities

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import de.psdev.devdrawer.R
import de.psdev.devdrawer.adapters.ActivityListAdapter
import kotlinx.android.synthetic.main.activity_choice.*
import mu.KLogging

class ChooseActivityDialog: Activity(), AdapterView.OnItemClickListener {

    companion object: KLogging() {
        const val EXTRA_PACKAGE_NAME = "packageName"

        @JvmStatic
        fun createStartIntent(context: Context, packageName: String) = Intent(context, ChooseActivityDialog::class.java).apply {
            putExtra(EXTRA_PACKAGE_NAME, packageName)
        }
    }

    private val appPackageName: String by lazy { intent.getStringExtra(EXTRA_PACKAGE_NAME) }
    private lateinit var activitiesList: List<String>

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        try {
            activitiesList = getActivityList(appPackageName)
        } catch (e: PackageManager.NameNotFoundException) {
            finish()
        }
        listView.adapter = ActivityListAdapter(this, activitiesList)
        listView.onItemClickListener = this
    }

    // ==========================================================================================================================
    // AdapterView.OnItemClickListener
    // ==========================================================================================================================

    override fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        val intent = Intent()
        intent.component = ComponentName(packageName, activitiesList[i])
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getActivityList(packageName: String): List<String> {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        return packageInfo.activities.orEmpty()
            .filter { it.exported }
            .map { it.name }
    }

}
