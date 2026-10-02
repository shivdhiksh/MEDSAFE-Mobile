package com.shivdhikshith.medsafe.domain

import com.shivdhikshith.medsafe.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserSettingsTest {

    @Test
    fun `default settings contain 30, 7, 1 thresholds and expired enabled`() {
        val settings = UserSettings()
        assertEquals(setOf(30, 7, 1), settings.alertThresholdDays)
        assertTrue(settings.notifyOnExpired)
        assertTrue(settings.notificationsEnabled)
        assertEquals(30, settings.alertDays)
    }

    @Test
    fun `alertDays reflects max threshold when custom thresholds set`() {
        val settings = UserSettings(alertThresholdDays = setOf(60, 14, 3))
        assertEquals(60, settings.alertDays)
        assertEquals(setOf(60, 14, 3), settings.alertThresholdDays)
    }

    @Test
    fun `thresholds model is extensible to additional days`() {
        val initial = UserSettings()
        val extended = initial.copy(alertThresholdDays = initial.alertThresholdDays + 14)
        assertTrue(extended.alertThresholdDays.contains(14))
        assertTrue(extended.alertThresholdDays.contains(30))
        assertTrue(extended.alertThresholdDays.contains(7))
        assertTrue(extended.alertThresholdDays.contains(1))
    }
}
