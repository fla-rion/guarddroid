package dev.guarddroid.feature.apps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.guarddroid.feature.apps.databinding.ItemAllowedAppBinding

class AllowedAppAdapter(
    private val onAppClick: (String) -> Unit
) : ListAdapter<AppItem, AllowedAppAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemAllowedAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppItem) {
            binding.tvAppName.text = item.appName
            binding.tvPackageName?.text = item.packageName
            // Screen reader: announce app name as the card's action
            binding.root.contentDescription = item.appName
            binding.root.setOnClickListener { onAppClick(item.packageName) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAllowedAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AppItem>() {
        override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem) =
            oldItem.packageName == newItem.packageName
        override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem) =
            oldItem == newItem
    }
}
