package dev.guarddroid.app.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.guarddroid.app.R
import dev.guarddroid.app.databinding.ActivityAdminBinding
import dev.guarddroid.feature.admin.AdminDashboardFragment
import dev.guarddroid.feature.admin.CodeInputFragment

@AndroidEntryPoint
class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private var timeoutMinutes = 5L
    private var isAuthenticated = false

    private val timeoutRunnable = Runnable {
        if (isAuthenticated) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showCodeInput()
    }

    fun onAuthenticationSuccess() {
        isAuthenticated = true
        resetTimeout()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AdminDashboardFragment())
            .commit()
    }

    private fun showCodeInput() {
        val fragment = CodeInputFragment()
        supportFragmentManager.setFragmentResultListener(
            CodeInputFragment.AUTH_RESULT_KEY, this
        ) { _, result ->
            if (result.getBoolean(CodeInputFragment.AUTH_RESULT_OK)) {
                onAuthenticationSuccess()
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun resetTimeout() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        timeoutHandler.postDelayed(timeoutRunnable, timeoutMinutes * 60 * 1000)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (isAuthenticated) resetTimeout()
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        super.onDestroy()
    }
}
