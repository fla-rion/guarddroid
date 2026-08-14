package dev.guarddroid.feature.apps

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.guarddroid.feature.apps.databinding.FragmentAllowedAppsBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllowedAppsFragment : Fragment() {

    private var _binding: FragmentAllowedAppsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppListViewModel by viewModels()
    private lateinit var adapter: AllowedAppAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllowedAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeApps()
    }

    private fun setupRecyclerView() {
        adapter = AllowedAppAdapter { packageName ->
            launchApp(packageName)
        }
        binding.rvAllowedApps.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvAllowedApps.adapter = adapter
    }

    private fun observeApps() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allowedApps.collect { apps ->
                    adapter.submitList(apps)
                    binding.tvEmpty.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun launchApp(packageName: String) {
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
            }
        } catch (e: Exception) {
            // App not launchable
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
