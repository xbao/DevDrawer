package de.psdev.devdrawer.activities

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.WidgetConfig
import kotlinx.android.synthetic.main.list_item_widget.view.*
import mu.KLogging

class WidgetsListAdapter(private val clickListener: (WidgetConfig) -> Unit): RecyclerView.Adapter<WidgetsListAdapter.WidgetsListViewHolder>() {

    companion object: KLogging()

    private val items = arrayListOf<WidgetConfig>()

    // ==========================================================================================================================
    // RecyclerView.Adapter
    // ==========================================================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetsListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_widget, parent, false)
        return WidgetsListViewHolder(view, clickListener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: WidgetsListViewHolder, position: Int) {
        holder.display(items[position])
    }

    // ==========================================================================================================================
    // Public API
    // ==========================================================================================================================

    fun setItems(widgets: List<WidgetConfig>) {
        items.apply {
            clear()
            addAll(widgets)
        }
        notifyDataSetChanged()
    }

    // ==========================================================================================================================
    // WidgetsListViewHolder
    // ==========================================================================================================================

    class WidgetsListViewHolder(itemView: View, private val clickListener: (WidgetConfig) -> Unit): RecyclerView.ViewHolder(itemView) {

        private val textName = itemView.text_name
        private val viewColor = itemView.color

        fun display(widget: WidgetConfig) {
            textName.text = widget.name
            viewColor.setBackgroundColor(widget.color)
            itemView.setOnClickListener { clickListener(widget) }
        }
    }
}

