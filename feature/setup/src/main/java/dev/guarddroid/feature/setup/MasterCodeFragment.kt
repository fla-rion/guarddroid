package dev.guarddroid.feature.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import dev.guarddroid.feature.setup.databinding.FragmentMasterCodeBinding

class MasterCodeFragment : Fragment() {

    private var _binding: FragmentMasterCodeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetupViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMasterCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSetCode.setOnClickListener { attemptSetCode() }
    }

    private fun attemptSetCode() {
        val code = binding.etMasterCode.text?.toString() ?: ""
        val confirm = binding.etConfirmCode.text?.toString() ?: ""

        when {
            code.length < 4 -> showError(getString(R.string.master_code_too_short))
            code != confirm -> showError(getString(R.string.master_code_mismatch))
            else -> {
                val result = viewModel.setMasterCode(code)
                if (result.isSuccess) {
                    binding.etMasterCode.isEnabled = false
                    binding.etConfirmCode.isEnabled = false
                    binding.btnSetCode.isEnabled = false
                    Snackbar.make(binding.root, getString(R.string.master_code_set_success), Snackbar.LENGTH_SHORT).show()
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Fehler")
                }
            }
        }
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
