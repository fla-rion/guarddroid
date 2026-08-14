package dev.guarddroid.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dev.guarddroid.app.R
import dev.guarddroid.app.databinding.ActivitySetupBinding
import dev.guarddroid.feature.setup.SetupPagerAdapter

@AndroidEntryPoint
class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = SetupPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false // Prevent swipe navigation

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateNavigationButtons(position, adapter.itemCount)
            }
        })

        binding.btnNext.setOnClickListener {
            val next = binding.viewPager.currentItem + 1
            if (next < adapter.itemCount) {
                binding.viewPager.currentItem = next
            } else {
                completeSetup()
            }
        }

        binding.btnBack.setOnClickListener {
            val prev = binding.viewPager.currentItem - 1
            if (prev >= 0) binding.viewPager.currentItem = prev
        }

        updateNavigationButtons(0, adapter.itemCount)
    }

    private fun updateNavigationButtons(position: Int, total: Int) {
        binding.btnBack.isEnabled = position > 0
        binding.btnNext.text = if (position == total - 1) {
            getString(R.string.setup_complete)
        } else {
            getString(R.string.next)
        }
        binding.progressIndicator.progress = ((position + 1) * 100) / total
    }

    fun navigateNext() {
        val next = binding.viewPager.currentItem + 1
        if (next < (binding.viewPager.adapter?.itemCount ?: 0)) {
            binding.viewPager.currentItem = next
        }
    }

    private fun completeSetup() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
