package dev.guarddroid.feature.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.guarddroid.core.common.CapabilityStatus
import dev.guarddroid.feature.setup.databinding.FragmentDeviceAnalysisBinding
import kotlinx.coroutines.launch

class DeviceAnalysisFragment : Fragment() {

    private var _binding: FragmentDeviceAnalysisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeviceAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDeviceInfo()
    }

    private fun observeDeviceInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.deviceInfo.collect { info ->
                        info ?: return@collect
                        binding.tvManufacturer.text = info.manufacturer
                        binding.tvModel.text = info.model
                        binding.tvAndroidVersion.text = info.androidVersion
                        binding.tvApiLevel.text = info.apiLevel.toString()
                        binding.tvSystemUi.text = info.systemUi.name
                        binding.tvHasGms.text = if (info.hasGms) "Ja" else "Nein"
                        binding.tvHasHms.text = if (info.hasHms) "Ja" else "Nein"
                        binding.tvIsDeviceAdmin.text = if (info.isDeviceAdmin) "Ja" else "Nein"
                        binding.tvIsDeviceOwner.text = if (info.isDeviceOwner) "Ja" else "Nein"
                    }
                }
                launch {
                    viewModel.capabilities.collect { caps ->
                        val supported = caps.values.count { it.status == CapabilityStatus.SUPPORTED }
                        val total = caps.size
                        binding.tvCapabilitySummary.text = "$supported von $total Funktionen verfügbar"
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
