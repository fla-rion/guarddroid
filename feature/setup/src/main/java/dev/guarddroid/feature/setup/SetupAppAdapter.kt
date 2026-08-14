package dev.guarddroid.feature.setup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.guarddroid.core.common.AppStatus
import dev.guarddroid.feature.setup.databinding.ItemSetupAppBinding

class SetupAppAdapter(
    private val onStatusChanged: (String, String, AppStatus, Boolean) -> Unit
) : ListAdapter<SetupAppItem, SetupAppAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemSetupAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SetupAppItem) {
            binding.tvAppName.text = item.appName
            binding.tvPackageName.text = item.packageName
            binding.chipGroup.check(statusToChipId(item.currentStatus))
            binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                val status = chipIdToStatus(checkedIds.firstOrNull() ?: -1)
                onStatusChanged(item.packageName, item.appName, status, item.isSystem)
            }
        }

        private fun statusToChipId(status: AppStatus): Int = when (status) {
            AppStatus.ALWAYS_ALLOWED -> R.id.chipAlwaysAllowed
            AppStatus.SCHEDULED -> R.id.chipScheduled
            AppStatus.ADMIN_ONLY -> R.id.chipAdminOnly
            AppStatus.BLOCKED -> R.id.chipBlocked
            AppStatus.HIDDEN -> R.id.chipHidden
        }

        private fun chipIdToStatus(id: Int): AppStatus = when (id) {
            R.id.chipAlwaysAllowed -> AppStatus.ALWAYS_ALLOWED
            R.id.chipScheduled -> AppStatus.SCHEDULED
            R.id.chipAdminOnly -> AppStatus.ADMIN_ONLY
            R.id.chipBlocked -> AppStatus.BLOCKED
            R.id.chipHidden -> AppStatus.HIDDEN
            else -> AppStatus.ALWAYS_ALLOWED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetupAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SetupAppItem>() {
        override fun areItemsTheSame(oldItem: SetupAppItem, newItem: SetupAppItem) =
            oldItem.packageName == newItem.packageName
        override fun areContentsTheSame(oldItem: SetupAppItem, newItem: SetupAppItem) =
            oldItem == newItem
    }
}
