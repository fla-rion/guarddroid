package dev.guarddroid.feature.setup

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.guarddroid.feature.setup.databinding.FragmentPermissionsBinding

class PermissionsFragment : Fragment() {

    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStates()
    }

    private fun setupButtons() {
        binding.btnGrantUsageStats.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        binding.btnGrantAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.btnGrantDeviceAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    ComponentName(requireContext(), "dev.guarddroid.app.receiver.GuardDroidAdminReceiver")
                )
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.permission_device_admin_description))
            }
            startActivity(intent)
        }
    }

    private fun updatePermissionStates() {
        binding.tvUsageStatsStatus.text = if (hasUsageStatsPermission()) "✓ Erteilt" else "✗ Nicht erteilt"
        binding.tvAccessibilityStatus.text = if (isAccessibilityEnabled()) "✓ Aktiv" else "✗ Nicht aktiv"
        binding.tvDeviceAdminStatus.text = if (isDeviceAdmin()) "✓ Aktiv" else "✗ Nicht aktiv"
        binding.tvDeviceOwnerStatus.text = if (isDeviceOwner()) "✓ Aktiv" else "Einrichtung über ADB erforderlich"
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = requireContext().getSystemService(android.app.AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            requireContext().packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun isAccessibilityEnabled(): Boolean = try {
        val serviceName = "${requireContext().packageName}/dev.guarddroid.app.service.GuardDroidAccessibilityService"
        val enabled = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        enabled.contains(serviceName)
    } catch (e: Exception) { false }

    private fun isDeviceAdmin(): Boolean = try {
        val dpm = requireContext().getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(requireContext(), "dev.guarddroid.app.receiver.GuardDroidAdminReceiver")
        dpm.isAdminActive(admin)
    } catch (e: Exception) { false }

    private fun isDeviceOwner(): Boolean = try {
        val dpm = requireContext().getSystemService(DevicePolicyManager::class.java)
        dpm.isDeviceOwnerApp(requireContext().packageName)
    } catch (e: Exception) { false }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
