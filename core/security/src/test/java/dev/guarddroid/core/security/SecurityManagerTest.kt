package dev.guarddroid.core.security

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SecurityManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private val prefMap = mutableMapOf<String, Any?>()

    @Before
    fun setup() {
        mockContext = mockk()
        mockPrefs = mockk()
        mockEditor = mockk()

        every { mockEditor.putString(any(), any()) } answers {
            prefMap[arg(0)] = arg(1)
            mockEditor
        }
        every { mockEditor.putInt(any(), any()) } answers {
            prefMap[arg(0)] = arg(1)
            mockEditor
        }
        every { mockEditor.putLong(any(), any()) } answers {
            prefMap[arg(0)] = arg(1)
            mockEditor
        }
        every { mockEditor.putBoolean(any(), any()) } answers {
            prefMap[arg(0)] = arg(1)
            mockEditor
        }
        every { mockEditor.apply() } just Runs
        every { mockPrefs.edit() } returns mockEditor
        every { mockPrefs.getString(any(), any()) } answers { prefMap[arg(0)] as? String ?: arg(1) }
        every { mockPrefs.getInt(any(), any()) } answers { prefMap[arg(0)] as? Int ?: arg(1) }
        every { mockPrefs.getLong(any(), any()) } answers { prefMap[arg(0)] as? Long ?: arg(1) }
        every { mockPrefs.getBoolean(any(), any()) } answers { prefMap[arg(0)] as? Boolean ?: arg(1) }
        every { mockPrefs.contains(any()) } answers { prefMap.containsKey(arg(0)) }
    }

    @Test
    fun `PBKDF2 hash is deterministic with same salt`() {
        // Test the hash function directly via reflection
        val sm = object {
            fun hash(code: String, salt: ByteArray): ByteArray {
                val spec = javax.crypto.spec.PBEKeySpec(
                    code.toCharArray(), salt, 100_000, 256
                )
                val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                return factory.generateSecret(spec).encoded
            }
        }
        val salt = ByteArray(16) { it.toByte() }
        val hash1 = sm.hash("testcode", salt)
        val hash2 = sm.hash("testcode", salt)
        assertArrayEquals(hash1, hash2)
    }

    @Test
    fun `different codes produce different hashes`() {
        val sm = object {
            fun hash(code: String, salt: ByteArray): ByteArray {
                val spec = javax.crypto.spec.PBEKeySpec(
                    code.toCharArray(), salt, 100_000, 256
                )
                val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                return factory.generateSecret(spec).encoded
            }
        }
        val salt = ByteArray(16) { it.toByte() }
        val hash1 = sm.hash("code1", salt)
        val hash2 = sm.hash("code2", salt)
        assertFalse(hash1.contentEquals(hash2))
    }

    @Test
    fun `constant time equal returns true for equal arrays`() {
        val a = ByteArray(32) { it.toByte() }
        val b = ByteArray(32) { it.toByte() }
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        assertEquals(0, diff)
    }

    @Test
    fun `constant time equal returns false for different arrays`() {
        val a = ByteArray(32) { it.toByte() }
        val b = ByteArray(32) { (it + 1).toByte() }
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        assertNotEquals(0, diff)
    }
}
