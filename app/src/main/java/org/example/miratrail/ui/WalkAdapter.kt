package org.example.miratrail.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.example.miratrail.R
import org.example.miratrail.data.Walk

class WalkAdapter(
    private val onOpen: (Walk) -> Unit,
    private val onSelect: ((Walk) -> Unit)? = null
) : ListAdapter<Walk, WalkAdapter.Holder>(Diff) {
    object Diff : DiffUtil.ItemCallback<Walk>() {
        override fun areItemsTheSame(oldItem: Walk, newItem: Walk) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Walk, newItem: Walk) = oldItem == newItem
    }

    class Holder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_walk, parent, false)
    ) {
        val title: TextView = itemView.findViewById(R.id.title)
        val subtitle: TextView = itemView.findViewById(R.id.subtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(parent)

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val walk = getItem(position)
        holder.title.text = walk.title
        holder.subtitle.text = "${walk.area} · ${walk.completedStages}/${walk.stages.size} Etappen"
        holder.itemView.contentDescription = "${walk.title}, ${walk.area}, Details öffnen"
        holder.itemView.isClickable = true
        holder.itemView.isFocusable = true
        holder.itemView.setOnClickListener { onOpen(walk) }
        holder.itemView.setOnLongClickListener {
            onSelect?.invoke(walk)
            onSelect != null
        }
    }
}
