package com.owentech.devdrawer.adapters

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.owentech.devdrawer.R
import com.owentech.devdrawer.activities.EditDialog
import com.owentech.devdrawer.appwidget.DDWidgetProvider
import com.owentech.devdrawer.database.DevDrawerDatabase
import com.owentech.devdrawer.database.PackageFilter

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
        val view = convertView ?: createView(position, parent)
        bindView(view, position)
        return view
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private fun createView(position: Int, parent: ViewGroup): View {
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
            val intent = Intent(activity, EditDialog::class.java)
            val bundle = Bundle().apply {
                putString("text", packageFilter.filter)
                putString("id", packageFilter.id.toString())
            }
            intent.putExtras(bundle)

            activity.startActivityForResult(intent, 0)
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