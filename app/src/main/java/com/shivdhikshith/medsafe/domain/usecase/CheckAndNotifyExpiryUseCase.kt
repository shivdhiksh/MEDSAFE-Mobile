package com.shivdhikshith.medsafe.domain.usecase

import android.util.Log
import com.shivdhikshith.medsafe.core.notifications.NotificationHelper
import com.shivdhikshith.medsafe.core.utils.DateUtils
import com.shivdhikshith.medsafe.core.utils.ExpiryCalculator
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.repository.AlertHistoryRepository
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.ZoneId

data class NotificationCheckResult(
    val batchesChecked: Int = 0,
    val notificationsSent: Int = 0,
    val duplicatesSuppressed: Int = 0,
    val errorsCount: Int = 0
)

/**
 * Evaluates active medicine batches against configured expiry reminder thresholds
 * (default: 30 days, 7 days, 1 day, and expired).
 *
 * Enforces MEDSAFE principles:
 * - Duplicate prevention via alert_history.
 * - Disposed/inactive batches never generate alerts.
 * - Zero medical advice.
 */
class CheckAndNotifyExpiryUseCase(
    private val medicineRepository: MedicineRepository,
    private val alertHistoryRepository: AlertHistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
) {
    suspend operator fun invoke(
        currentDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
    ): NotificationCheckResult {
        logI("DIAGNOSTIC [CheckAndNotifyExpiryUseCase]: Starting evaluation for currentDate=$currentDate")
        val settings = settingsRepository.getSettings()
        if (!settings.notificationsEnabled) {
            logI("DIAGNOSTIC [Master Toggle]: Notifications are disabled in settings; skipping expiry check")
            return NotificationCheckResult()
        }

        logI("DIAGNOSTIC [Settings]: alertThresholdDays=${settings.alertThresholdDays}, notifyOnExpired=${settings.notifyOnExpired}")

        var checkedCount = 0
        var sentCount = 0
        var duplicateCount = 0
        var errorCount = 0

        try {
            val medicines = medicineRepository.getAllMedicinesWithBatches()
            logI("DIAGNOSTIC [Inventory]: Found ${medicines.size} medicines in inventory")

            for (medWithBatches in medicines) {
                val medicine = medWithBatches.medicine
                for (batch in medWithBatches.batches) {
                    // Critical safety: Disposed or inactive batches must NOT generate notifications
                    if (batch.status == ExpiryStatus.DISPOSED || batch.disposedDate != null) {
                        logD("DIAGNOSTIC [Skipped Disposed]: batchId=${batch.id} for medicine '${medicine.name}'")
                        continue
                    }

                    checkedCount++
                    val daysRemaining = ExpiryCalculator.daysRemaining(batch.expiryDate, currentDate)
                    val expiryDateIso = DateUtils.formatToIsoDate(batch.expiryDate)

                    logI("DIAGNOSTIC [Batch ID]: ${batch.id} (${medicine.name} #${batch.batchNumber})")
                    logI("DIAGNOSTIC [Expiry Date]: millis=${batch.expiryDate}, formatted='$expiryDateIso'")
                    logI("DIAGNOSTIC [Calculated Days Remaining]: $daysRemaining (currentDate=$currentDate, status=${batch.status})")

                    val isThresholdMatch = daysRemaining > 0 && settings.alertThresholdDays.contains(daysRemaining.toInt())
                    val alertType: String? = when {
                        daysRemaining <= 0 && settings.notifyOnExpired -> "EXPIRED"
                        isThresholdMatch -> if (daysRemaining == 1L) "1_DAY" else "${daysRemaining}_DAYS"
                        else -> null
                    }

                    logI("DIAGNOSTIC [Matched Threshold]: $alertType (thresholds=${settings.alertThresholdDays}, notifyOnExpired=${settings.notifyOnExpired})")

                    if (alertType != null) {
                        try {
                            val alreadySent = alertHistoryRepository.hasAlertBeenSent(batch.id, alertType)
                            logI("DIAGNOSTIC [Alert-History Lookup]: batchId=${batch.id}, alertType=$alertType, alreadySent=$alreadySent")

                            if (alreadySent) {
                                logI("DIAGNOSTIC [Alert-History Decision]: DUPLICATE_SUPPRESSED (alert $alertType was already sent for batch ${batch.id})")
                                duplicateCount++
                            } else {
                                val notificationId = generateNotificationId(batch.id, alertType)
                                logI("DIAGNOSTIC [Dispatching]: Attempting notificationId=$notificationId for batchId=${batch.id}, alertType=$alertType")

                                val wasShown = notificationHelper.showExpiryAlert(
                                    notificationId = notificationId,
                                    medicineName = medicine.name,
                                    batchNumber = batch.batchNumber,
                                    daysRemaining = daysRemaining
                                )

                                logI("DIAGNOSTIC [Notification Dispatch Result]: wasShown=$wasShown (notificationId=$notificationId, batchId=${batch.id}, alertType=$alertType)")

                                if (wasShown) {
                                    alertHistoryRepository.recordAlert(
                                        batchId = batch.id,
                                        alertType = alertType
                                    )
                                    logI("DIAGNOSTIC [Alert-History Decision]: RECORDED (batchId=${batch.id}, alertType=$alertType)")
                                    sentCount++
                                } else {
                                    logW("DIAGNOSTIC [Alert-History Decision]: NOT_RECORDED (notification was not shown for batchId=${batch.id})")
                                    errorCount++
                                }
                            }
                        } catch (e: Exception) {
                            logE("DIAGNOSTIC [Alert Error]: Error processing alert for batch ${batch.id}: ${e.message}", e)
                            errorCount++
                        }
                    } else {
                        logD("DIAGNOSTIC [No Threshold Match]: batchId=${batch.id} (${medicine.name}) does not match any active threshold (daysRemaining=$daysRemaining)")
                    }
                }
            }
        } catch (e: Exception) {
            logE("DIAGNOSTIC [Inventory Error]: Failed to load medicines during expiry check: ${e.message}", e)
            errorCount++
        }

        val result = NotificationCheckResult(
            batchesChecked = checkedCount,
            notificationsSent = sentCount,
            duplicatesSuppressed = duplicateCount,
            errorsCount = errorCount
        )
        logI("DIAGNOSTIC [CheckAndNotifyExpiryUseCase]: Completed with result: $result")
        return result
    }

    private fun generateNotificationId(batchId: Long, alertType: String): Int {
        val raw = (batchId.hashCode() * 31 + alertType.hashCode()) and 0x7FFFFFFF
        return if (raw == 0) 1 else raw
    }

    private fun logI(msg: String) {
        try {
            Log.i(TAG, msg)
        } catch (_: Throwable) {}
    }

    private fun logD(msg: String) {
        try {
            Log.d(TAG, msg)
        } catch (_: Throwable) {}
    }

    private fun logW(msg: String) {
        try {
            Log.w(TAG, msg)
        } catch (_: Throwable) {}
    }

    private fun logE(msg: String, throwable: Throwable? = null) {
        try {
            if (throwable != null) Log.e(TAG, msg, throwable) else Log.e(TAG, msg)
        } catch (_: Throwable) {}
    }

    companion object {
        private const val TAG = "CheckExpiryUseCase"
    }
}
