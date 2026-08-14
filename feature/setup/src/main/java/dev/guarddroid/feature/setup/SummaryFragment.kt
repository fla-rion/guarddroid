package dev.guarddroid.feature.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dev.guarddroid.core.common.CapabilityStatus
import dev.guarddroid.feature.setup.databinding.FragmentSummaryBinding

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deviceInfo = viewModel.deviceInfo.value
        val caps = viewModel.capabilities.value
        val supported = caps.values.count { it.status == CapabilityStatus.SUPPORTED }

        binding.tvSummaryDevice.text = "${deviceInfo?.manufacturer} ${deviceInfo?.model} (Android ${deviceInfo?.androidVersion})"
        binding.tvSummaryCapabilities.text = "$supported / ${caps.size} Funktionen verfügbar"
        binding.tvSummaryManagement.text = when {
            deviceInfo?.isDeviceOwner == true -> "Gerätebesitzer - Vollständige Kontrolle"
            deviceInfo?.isDeviceAdmin == true -> "Geräteadministrator - Eingeschränkte Kontrolle"
            else -> "Nur Überwachung - Eingeschränkter Schutz"
        }

        binding.tvSummaryGms.text = if (deviceInfo?.hasGms == true) "Google Play Services verfügbar" else "Kein Google Play Services (HMS/reines Android)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
