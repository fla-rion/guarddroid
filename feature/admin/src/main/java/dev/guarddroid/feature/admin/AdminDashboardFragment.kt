package dev.guarddroid.feature.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.guarddroid.feature.admin.databinding.FragmentAdminDashboardBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: AdminAppAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupUpdateSection()
        observeApps()
        observeUpdateState()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.admin_title)
        binding.toolbar.setNavigationOnClickListener { activity?.finish() }
    }

    private fun setupRecyclerView() {
        adapter = AdminAppAdapter(
            onBlock = { viewModel.blockApp(it) },
            onUnblock = { viewModel.unblockApp(it) },
            onHide = { viewModel.hideApp(it) },
            onShow = { viewModel.showApp(it) }
        )
        binding.rvApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApps.adapter = adapter
    }

    private fun setupUpdateSection() {
        binding.buttonCheckUpdate.setOnClickListener {
            viewModel.checkForUpdate()
        }
    }

    private fun observeApps() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allApps.collect { apps ->
                    adapter.submitList(apps)
                }
            }
        }
    }

    private fun observeUpdateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateState.collect { state ->
                    val statusView = binding.textUpdateStatus
                    when (state) {
                        is UpdateState.Idle -> statusView.isVisible = false
                        is UpdateState.Checking -> {
                            statusView.isVisible = true
                            statusView.text = getString(dev.guarddroid.core.update.R.string.update_checking)
                        }
                        is UpdateState.UpToDate -> {
                            statusView.isVisible = true
                            statusView.text = getString(dev.guarddroid.core.update.R.string.update_up_to_date)
                        }
                        is UpdateState.Available -> {
                            statusView.isVisible = true
                            statusView.text = getString(
                                dev.guarddroid.core.update.R.string.update_available_text,
                                state.info.versionName
                            )
                            // Open release URL in browser for download
                            statusView.setOnClickListener {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.info.releaseUrl)))
                            }
                        }
                        is UpdateState.Error -> {
                            statusView.isVisible = true
                            statusView.text = getString(dev.guarddroid.core.update.R.string.update_check_failed)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
