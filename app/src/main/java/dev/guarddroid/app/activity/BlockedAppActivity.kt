package dev.guarddroid.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.guarddroid.app.R
import dev.guarddroid.app.databinding.ActivityBlockedAppBinding

@AndroidEntryPoint
class BlockedAppActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
    }

    private lateinit var binding: ActivityBlockedAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: getString(R.string.unknown_app)
        binding.tvBlockedAppName.text = appName
        binding.tvBlockedMessage.text = getString(R.string.app_blocked_message, appName)

        binding.btnGoHome.setOnClickListener { goHome() }
        binding.btnAdminAccess.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        goHome()
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}
