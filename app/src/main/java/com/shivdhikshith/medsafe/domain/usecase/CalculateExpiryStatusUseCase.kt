package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.core.utils.ExpiryCalculator
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import java.time.LocalDate

class CalculateExpiryStatusUseCase {
    operator fun invoke(
        expiryDateEpochMillis: Long,
        isDisposed: Boolean = false,
        warningThresholdDays: Int = 30,
        currentLocalDate: LocalDate = LocalDate.now()
    ): ExpiryStatus {
        return ExpiryCalculator.computeStatus(
            expiryDateEpochMillis = expiryDateEpochMillis,
            isManuallyDisposed = isDisposed,
            warningThresholdDays = warningThresholdDays,
            currentLocalDate = currentLocalDate
        )
    }
}
