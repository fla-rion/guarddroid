package dev.guarddroid.core.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE = "guarddroid_security"
        private const val KEY_HASH = "master_hash"
        private const val KEY_SALT = "master_salt"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"
        private const val KEY_SETUP_COMPLETE = "setup_complete"

        private const val PBKDF2_ITERATIONS = 100_000
        private const val SALT_BYTES = 16
        private const val HASH_BYTES = 32
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30L * 60 * 1000 // 30 minutes
    }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular prefs if KeyStore unavailable (very old devices)
            context.getSharedPreferences("${PREFS_FILE}_fallback", Context.MODE_PRIVATE)
        }
    }

    fun setMasterCode(code: String): Result<Unit> = runCatching {
        require(code.length >= 4) { "Master code must be at least 4 characters" }
        val salt = generateSalt()
        val hash = hashCode(code, salt)
        prefs.edit()
            .putString(KEY_SALT, bytesToHex(salt))
            .putString(KEY_HASH, bytesToHex(hash))
            .putBoolean(KEY_SETUP_COMPLETE, true)
            .apply()
        resetFailedAttempts()
    }

    fun verifyMasterCode(code: String): Boolean {
        if (isLockedOut()) return false
        val saltHex = prefs.getString(KEY_SALT, null) ?: return false
        val hashHex = prefs.getString(KEY_HASH, null) ?: return false
        val salt = hexToBytes(saltHex)
        val expectedHash = hexToBytes(hashHex)
        val actualHash = hashCode(code, salt)
        val match = constantTimeEqual(actualHash, expectedHash)
        if (!match) {
            recordFailedAttempt()
        } else {
            resetFailedAttempts()
        }
        return match
    }

    fun isMasterCodeSet(): Boolean = prefs.contains(KEY_HASH)

    fun isSetupComplete(): Boolean = prefs.getBoolean(KEY_SETUP_COMPLETE, false)

    fun recordFailedAttempt() {
        val current = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, current).apply()
        if (current >= MAX_ATTEMPTS) {
            val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            prefs.edit().putLong(KEY_LOCKOUT_UNTIL, lockoutUntil).apply()
        }
    }

    fun isLockedOut(): Boolean {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        if (lockoutUntil == 0L) return false
        return if (System.currentTimeMillis() < lockoutUntil) {
            true
        } else {
            resetFailedAttempts()
            false
        }
    }

    fun getLockoutRemainingSeconds(): Long {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val remaining = lockoutUntil - System.currentTimeMillis()
        return if (remaining > 0) remaining / 1000 else 0
    }

    fun getFailedAttempts(): Int = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

    private fun resetFailedAttempts() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        return ByteArray(SALT_BYTES).also { random.nextBytes(it) }
    }

    private fun hashCode(code: String, salt: ByteArray): ByteArray {
        val spec: KeySpec = PBEKeySpec(code.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTES * 8)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    private fun constantTimeEqual(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
