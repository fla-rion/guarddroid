package dev.guarddroid.feature.setup

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dev.guarddroid.core.common.AppStatus
import dev.guarddroid.feature.setup.databinding.FragmentAppConfigBinding

class AppConfigFragment : Fragment() {

    private var _binding: FragmentAppConfigBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupViewModel by activityViewModels()
    private lateinit var adapter: SetupAppAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadInstalledApps()
    }

    private fun setupRecyclerView() {
        adapter = SetupAppAdapter { packageName, appName, status, isSystem ->
            viewModel.updateAppRule(packageName, appName, status, isSystem)
        }
        binding.rvApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApps.adapter = adapter
    }

    private fun loadInstalledApps() {
        val pm = requireContext().packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { it.packageName != requireContext().packageName }
            .map { app ->
                SetupAppItem(
                    packageName = app.packageName,
                    appName = pm.getApplicationLabel(app).toString(),
                    isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    currentStatus = AppStatus.ALWAYS_ALLOWED
                )
            }
            .sortedWith(compareBy({ it.isSystem }, { it.appName }))
        adapter.submitList(apps)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class SetupAppItem(
    val packageName: String,
    val appName: String,
    val isSystem: Boolean,
    val currentStatus: AppStatus
)
