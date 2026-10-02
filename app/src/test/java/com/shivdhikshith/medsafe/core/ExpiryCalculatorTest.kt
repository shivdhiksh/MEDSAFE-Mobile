package com.shivdhikshith.medsafe.core

import com.shivdhikshith.medsafe.core.utils.DateUtils
import com.shivdhikshith.medsafe.core.utils.ExpiryCalculator
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate

class ExpiryCalculatorTest {

    private val fixedCurrentDate = LocalDate.of(2026, 6, 1)

    @Test
    fun computeStatus_futureDateBeyondThreshold_returnsValid() {
        val expiryDate = LocalDate.of(2026, 8, 1) // 61 days ahead
        val millis = DateUtils.localDateToEpochMillis(expiryDate)

        val status = ExpiryCalculator.computeStatus(
            expiryDateEpochMillis = millis,
            isManuallyDisposed = false,
            warningThresholdDays = 30,
            currentLocalDate = fixedCurrentDate
        )

        assertEquals(ExpiryStatus.VALID, status)
        assertEquals("Expiry date not reached", status.label)
        assertNotEquals("Safe to use", status.label)
    }

    @Test
    fun computeStatus_futureDateWithinThreshold_returnsExpiringSoon() {
        val expiryDate = LocalDate.of(2026, 6, 20) // 19 days ahead
        val millis = DateUtils.localDateToEpochMillis(expiryDate)

        val status = ExpiryCalculator.computeStatus(
            expiryDateEpochMillis = millis,
            isManuallyDisposed = false,
            warningThresholdDays = 30,
            currentLocalDate = fixedCurrentDate
        )

        assertEquals(ExpiryStatus.EXPIRING_SOON, status)
    }

    @Test
    fun computeStatus_pastDate_returnsExpired() {
        val expiryDate = LocalDate.of(2026, 5, 20) // 12 days in past
        val millis = DateUtils.localDateToEpochMillis(expiryDate)

        val status = ExpiryCalculator.computeStatus(
            expiryDateEpochMillis = millis,
            isManuallyDisposed = false,
            warningThresholdDays = 30,
            currentLocalDate = fixedCurrentDate
        )

        assertEquals(ExpiryStatus.EXPIRED, status)
    }

    @Test
    fun computeStatus_manuallyDisposed_returnsDisposedRegardlessOfDate() {
        val futureDate = LocalDate.of(2026, 12, 1)
        val millis = DateUtils.localDateToEpochMillis(futureDate)

        val status = ExpiryCalculator.computeStatus(
            expiryDateEpochMillis = millis,
            isManuallyDisposed = true,
            warningThresholdDays = 30,
            currentLocalDate = fixedCurrentDate
        )

        assertEquals(ExpiryStatus.DISPOSED, status)
    }

    @Test
    fun daysRemaining_calculatesCorrectPositiveAndNegativeDays() {
        val futureMillis = DateUtils.localDateToEpochMillis(LocalDate.of(2026, 6, 10))
        val pastMillis = DateUtils.localDateToEpochMillis(LocalDate.of(2026, 5, 25))

        assertEquals(9L, ExpiryCalculator.daysRemaining(futureMillis, fixedCurrentDate))
        assertEquals(-7L, ExpiryCalculator.daysRemaining(pastMillis, fixedCurrentDate))
    }
}
