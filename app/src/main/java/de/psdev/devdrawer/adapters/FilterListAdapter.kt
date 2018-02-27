package de.psdev.devdrawer.adapters

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import de.psdev.devdrawer.R
import de.psdev.devdrawer.activities.EditDialog
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.PackageFilter

class FilterListAdapter(private val activity: Activity,
                        private val devDrawerDatabase: DevDrawerDatabase): BaseAdapter() {

    private val filters = mutableListOf<PackageFilter>()
    private val layoutInflater = activity.layoutInflater

    var data: List<PackageFilter>
        get() = filters.toList()
        set(value) {
            filters.clear()
            filters.addAll(value)
            notifyDataSetChanged()
        }

    // ==========================================================================================================================
    // BaseAdapter
    // ==========================================================================================================================

    override fun getCount(): Int = filters.size

    override fun getItem(position: Int): PackageFilter = filters[position]

    override fun getItemId(position: Int): Long = filters[position].id.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: createView(parent)
        bindView(view, position)
        return view
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private fun createView(parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.package_list_item, parent, false).apply {
            tag = FilterViewHolder(this)
        }
    }

    private fun bindView(view: View, position: Int) {
        val holder: FilterViewHolder = view.tag as FilterViewHolder

        val packageFilter = filters[position]
        holder.packageNameTextView.text = packageFilter.filter

        // OnClick action for Delete Button
        holder.deleteImageButton.setOnClickListener {
            devDrawerDatabase.packageFilterDao().deleteAsync(packageFilter).subscribe()

            val appWidgetManager = AppWidgetManager.getInstance(activity)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(activity, DDWidgetProvider::class.java))
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView)
        }

        // OnClick action for Edit Button
        holder.editImageButton.setOnClickListener {
            val editDialogIntent = EditDialog.createStartIntent(activity, packageFilter.id, packageFilter.filter)
            activity.startActivityForResult(editDialogIntent, 0)
        }
    }

    // ==========================================================================================================================
    // Inner classes
    // ==========================================================================================================================

    class FilterViewHolder(view: View) {
        val packageNameTextView: TextView = view.findViewById(R.id.packageNameTextView)
        val deleteImageButton: ImageButton = view.findViewById(R.id.deleteImageButton)
        val editImageButton: ImageButton = view.findViewById(R.id.editImageButton)
    }

}