package dev.guarddroid.feature.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.guarddroid.core.common.AppStatus
import dev.guarddroid.core.database.entity.AppRuleEntity
import dev.guarddroid.feature.admin.databinding.ItemAdminAppBinding

class AdminAppAdapter(
    private val onBlock: (String) -> Unit,
    private val onUnblock: (String) -> Unit,
    private val onHide: (String) -> Unit,
    private val onShow: (String) -> Unit
) : ListAdapter<AppRuleEntity, AdminAppAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemAdminAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppRuleEntity) {
            val ctx = binding.root.context
            val name = item.appName.ifEmpty { item.packageName }
            binding.tvAppName.text = name
            binding.tvPackageName.text = item.packageName
            binding.tvStatus.text = statusLabel(ctx, item.status)

            val isBlocked = item.status == AppStatus.BLOCKED
            val isHidden = item.status == AppStatus.HIDDEN

            binding.btnBlock.text = if (isBlocked)
                ctx.getString(R.string.action_unblock) else ctx.getString(R.string.status_blocked)
            binding.btnBlock.contentDescription = if (isBlocked)
                ctx.getString(R.string.cd_unblock_app, name)
            else
                ctx.getString(R.string.cd_block_app, name)

            binding.btnHide.text = if (isHidden)
                ctx.getString(R.string.action_show) else ctx.getString(R.string.status_hidden)
            binding.btnHide.contentDescription = if (isHidden)
                ctx.getString(R.string.cd_show_app, name)
            else
                ctx.getString(R.string.cd_hide_app, name)

            binding.btnBlock.setOnClickListener {
                if (isBlocked) onUnblock(item.packageName) else onBlock(item.packageName)
            }
            binding.btnHide.setOnClickListener {
                if (isHidden) onShow(item.packageName) else onHide(item.packageName)
            }

            binding.root.contentDescription =
                "$name, ${ctx.getString(R.string.cd_status_label, statusLabel(ctx, item.status))}"
        }

        private fun statusLabel(ctx: android.content.Context, status: AppStatus): String =
            when (status) {
                AppStatus.ALWAYS_ALLOWED -> ctx.getString(R.string.status_always_allowed)
                AppStatus.SCHEDULED -> ctx.getString(R.string.status_scheduled)
                AppStatus.ADMIN_ONLY -> ctx.getString(R.string.status_admin_only)
                AppStatus.BLOCKED -> ctx.getString(R.string.status_blocked)
                AppStatus.HIDDEN -> ctx.getString(R.string.status_hidden)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AppRuleEntity>() {
        override fun areItemsTheSame(a: AppRuleEntity, b: AppRuleEntity) = a.packageName == b.packageName
        override fun areContentsTheSame(a: AppRuleEntity, b: AppRuleEntity) = a == b
    }
}
