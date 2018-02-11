package de.psdev.devdrawer.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import de.psdev.devdrawer.R

class ActivityListAdapter(activity: Activity, private val activityList: List<String>): BaseAdapter() {

    private val layoutInflater: LayoutInflater = activity.layoutInflater

    // ==========================================================================================================================
    // BaseAdapter
    // ==========================================================================================================================

    override fun getCount(): Int = activityList.size

    override fun getItem(position: Int): String = activityList[position]

    override fun getItemId(position: Int): Long = activityList[position].hashCode().toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: createView(parent)
        bindView(position, view)
        return view
    }

    private fun createView(parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.activity_choice_list_item, parent, false).apply {
            tag = ViewHolder(this)
        }
    }

    private fun bindView(position: Int, view: View) {
        val viewHolder = view.tag as ViewHolder
        viewHolder.run {
            txtActivityPath.text = activityList[position].substring(0, activityList[position].lastIndexOf('.'))
            txtActivityName.text = activityList[position].substring(activityList[position].lastIndexOf('.'), activityList[position].length)
        }
    }

    // ==========================================================================================================================
    // Inner classes
    // ==========================================================================================================================

    class ViewHolder(view: View) {
        val txtActivityPath: TextView = view.findViewById(R.id.activityPathTextView)
        val txtActivityName: TextView = view.findViewById(R.id.activityNameTextView)
    }
}
