package com.shivdhikshith.medsafe.core.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun parseIsoDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString.trim(), ISO_DATE_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }

    fun formatToIsoDate(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(ISO_DATE_FORMATTER)
    }

    fun formatToDisplayDate(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DISPLAY_DATE_FORMATTER)
    }

    fun localDateToEpochMillis(localDate: LocalDate): Long {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun daysBetween(fromEpochMillis: Long, toEpochMillis: Long): Long {
        val fromDate = Instant.ofEpochMilli(fromEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val toDate = Instant.ofEpochMilli(toEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(fromDate, toDate)
    }
}
