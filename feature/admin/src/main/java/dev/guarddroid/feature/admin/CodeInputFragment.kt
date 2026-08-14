package dev.guarddroid.feature.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.guarddroid.feature.admin.databinding.FragmentCodeInputBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CodeInputFragment : Fragment() {

    private var _binding: FragmentCodeInputBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCodeInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        binding.btnVerify.setOnClickListener {
            val code = binding.etCode.text?.toString() ?: ""
            viewModel.verifyCode(code)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AdminUiState.Authenticated -> {
                            setFragmentResult(AUTH_RESULT_KEY, bundleOf(AUTH_RESULT_OK to true))
                        }
                        is AdminUiState.Error -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            binding.etCode.text?.clear()
                        }
                        is AdminUiState.LockedOut -> {
                            binding.btnVerify.isEnabled = false
                            binding.etCode.isEnabled = false
                            binding.tvLockoutInfo.visibility = View.VISIBLE
                            binding.tvLockoutInfo.text = "Zu viele Fehlversuche. Bitte warten Sie ${state.remainingSeconds / 60} Minuten."
                        }
                        else -> {
                            binding.btnVerify.isEnabled = true
                            binding.etCode.isEnabled = true
                            binding.tvLockoutInfo.visibility = View.GONE
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

    companion object {
        const val AUTH_RESULT_KEY = "auth_result"
        const val AUTH_RESULT_OK = "authenticated"
    }
}
