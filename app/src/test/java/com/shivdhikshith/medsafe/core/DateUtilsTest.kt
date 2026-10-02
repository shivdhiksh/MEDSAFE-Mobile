package com.shivdhikshith.medsafe.core

import com.shivdhikshith.medsafe.core.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun parseIsoDate_validDate_returnsLocalDate() {
        val result = DateUtils.parseIsoDate("2026-12-31")
        assertNotNull(result)
        assertEquals(2026, result?.year)
        assertEquals(12, result?.monthValue)
        assertEquals(31, result?.dayOfMonth)
    }

    @Test
    fun parseIsoDate_invalidDate_returnsNull() {
        val result = DateUtils.parseIsoDate("invalid-date")
        assertNull(result)
    }

    @Test
    fun localDateToEpochMillis_convertsAndFormatsCorrectly() {
        val date = LocalDate.of(2026, 6, 15)
        val epochMillis = DateUtils.localDateToEpochMillis(date)
        val formatted = DateUtils.formatToIsoDate(epochMillis)
        assertEquals("2026-06-15", formatted)
    }

    @Test
    fun daysBetween_computesExactDifference() {
        val date1 = DateUtils.localDateToEpochMillis(LocalDate.of(2026, 1, 1))
        val date2 = DateUtils.localDateToEpochMillis(LocalDate.of(2026, 1, 11))
        val days = DateUtils.daysBetween(date1, date2)
        assertEquals(10L, days)
    }
}
