package dev.guarddroid.feature.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dev.guarddroid.core.common.Capability
import dev.guarddroid.core.common.CapabilityStatus
import dev.guarddroid.core.database.entity.ConfigKeys
import dev.guarddroid.feature.setup.databinding.FragmentSystemRulesBinding

class SystemRulesFragment : Fragment() {

    private var _binding: FragmentSystemRulesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSystemRulesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val caps = viewModel.capabilities.value
        val hasInstallRestriction = caps[Capability.APP_INSTALL_RESTRICTION]?.status == CapabilityStatus.SUPPORTED
        val hasSettingsRestriction = caps[Capability.SETTINGS_RESTRICTION]?.status == CapabilityStatus.SUPPORTED

        binding.switchRestrictInstall.isEnabled = hasInstallRestriction
        binding.switchRestrictSettings.isEnabled = hasSettingsRestriction

        if (!hasInstallRestriction) {
            binding.tvInstallRestrictionNote.visibility = View.VISIBLE
            binding.tvInstallRestrictionNote.text = getString(R.string.capability_requires_device_owner)
        }

        binding.switchRestrictInstall.setOnCheckedChangeListener { _, checked ->
            viewModel.setSystemConfig(ConfigKeys.RESTRICT_INSTALL, checked.toString())
        }
        binding.switchRestrictUnknownSources.setOnCheckedChangeListener { _, checked ->
            viewModel.setSystemConfig(ConfigKeys.RESTRICT_UNKNOWN_SOURCES, checked.toString())
        }
        binding.switchRestrictSettings.setOnCheckedChangeListener { _, checked ->
            viewModel.setSystemConfig(ConfigKeys.RESTRICT_SETTINGS, checked.toString())
        }
        binding.switchRestrictUsbDebug.setOnCheckedChangeListener { _, checked ->
            viewModel.setSystemConfig(ConfigKeys.RESTRICT_USB_DEBUG, checked.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
