package com.groupa.presentation

import com.groupa.coreui.util.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun systemDefaultResolvesDeviceLanguage() {
        assertEquals(
            "zh-CN",
            AppLanguage.resolveStoredTag(
                storedLanguageTag = AppLanguage.SYSTEM_DEFAULT,
                systemLanguageTag = "zh-CN",
            ),
        )
    }

    @Test
    fun systemDefaultFallsBackWhenDeviceLanguageIsUnavailable() {
        assertEquals(
            "en-US",
            AppLanguage.resolveStoredTag(
                storedLanguageTag = AppLanguage.SYSTEM_DEFAULT,
                systemLanguageTag = "",
            ),
        )
    }

    @Test
    fun systemDefaultSupportsDeviceLanguageWithoutRegion() {
        assertEquals(
            "zh",
            AppLanguage.resolveStoredTag(
                storedLanguageTag = AppLanguage.SYSTEM_DEFAULT,
                systemLanguageTag = "zh",
            ),
        )
    }

    @Test
    fun explicitLanguageOverridesSystemLanguage() {
        assertEquals(
            "en-US",
            AppLanguage.resolveStoredTag(
                storedLanguageTag = "en-US",
                systemLanguageTag = "zh-CN",
            ),
        )
    }
}
