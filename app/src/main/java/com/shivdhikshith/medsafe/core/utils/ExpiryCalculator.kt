package com.shivdhikshith.medsafe.core.utils

import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object ExpiryCalculator {

    fun computeStatus(
        expiryDateEpochMillis: Long,
        isManuallyDisposed: Boolean = false,
        warningThresholdDays: Int = 30,
        currentLocalDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
    ): ExpiryStatus {
        if (isManuallyDisposed) {
            return ExpiryStatus.DISPOSED
        }

        val expiryLocalDate = DateUtils.parseIsoDate(DateUtils.formatToIsoDate(expiryDateEpochMillis))
            ?: return ExpiryStatus.VALID

        val daysUntilExpiry = ChronoUnit.DAYS.between(currentLocalDate, expiryLocalDate)

        return when {
            daysUntilExpiry < 0 -> ExpiryStatus.EXPIRED
            daysUntilExpiry <= warningThresholdDays -> ExpiryStatus.EXPIRING_SOON
            else -> ExpiryStatus.VALID
        }
    }

    fun daysRemaining(
        expiryDateEpochMillis: Long,
        currentLocalDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
    ): Long {
        val expiryLocalDate = DateUtils.parseIsoDate(DateUtils.formatToIsoDate(expiryDateEpochMillis))
            ?: return 0L
        return ChronoUnit.DAYS.between(currentLocalDate, expiryLocalDate)
    }
}
