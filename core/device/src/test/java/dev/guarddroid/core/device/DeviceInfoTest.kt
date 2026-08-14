package dev.guarddroid.core.device

import org.junit.Assert.*
import org.junit.Test

class DeviceInfoTest {

    @Test
    fun `DeviceInfo stores all fields correctly`() {
        val info = DeviceInfo(
            manufacturer = "Huawei",
            model = "P40 lite",
            device = "HWANNE",
            androidVersion = "10",
            apiLevel = 29,
            buildId = "HUAWEIANNE-L01",
            securityPatch = "2021-03-01",
            systemUi = SystemUi.EMUI,
            hasGms = false,
            hasPlayStore = false,
            hasHms = true,
            hasAppGallery = true,
            isDeviceAdmin = false,
            isDeviceOwner = false
        )

        assertEquals("Huawei", info.manufacturer)
        assertEquals("P40 lite", info.model)
        assertEquals(29, info.apiLevel)
        assertEquals(SystemUi.EMUI, info.systemUi)
        assertFalse(info.hasGms)
        assertTrue(info.hasHms)
    }

    @Test
    fun `SystemUi enum covers all expected values`() {
        val values = SystemUi.values()
        assertTrue(values.contains(SystemUi.STOCK))
        assertTrue(values.contains(SystemUi.EMUI))
        assertTrue(values.contains(SystemUi.HARMONY_OS))
        assertTrue(values.contains(SystemUi.ONE_UI))
        assertTrue(values.contains(SystemUi.MIUI_HYPER_OS))
        assertTrue(values.contains(SystemUi.OTHER))
    }

    @Test
    fun `GMS and HMS can both be false on pure AOSP`() {
        val info = DeviceInfo(
            manufacturer = "Google",
            model = "Pixel 7",
            device = "panther",
            androidVersion = "13",
            apiLevel = 33,
            buildId = "TQ3A",
            securityPatch = "2023-01-01",
            systemUi = SystemUi.STOCK,
            hasGms = true,
            hasPlayStore = true,
            hasHms = false,
            hasAppGallery = false,
            isDeviceAdmin = false,
            isDeviceOwner = false
        )
        assertTrue(info.hasGms)
        assertFalse(info.hasHms)
    }
}
