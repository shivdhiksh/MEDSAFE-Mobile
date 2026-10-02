package com.shivdhikshith.medsafe.domain.model

data class UserSettings(
    val notificationsEnabled: Boolean = true,
    val alertThresholdDays: Set<Int> = DEFAULT_THRESHOLDS,
    val notifyOnExpired: Boolean = true,
    val theme: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val alertDays: Int = alertThresholdDays.maxOrNull() ?: DEFAULT_ALERT_DAYS
) {
    companion object {
        const val DEFAULT_ALERT_DAYS = 30
        val DEFAULT_THRESHOLDS: Set<Int> = sortedSetOf(Comparator.reverseOrder(), 30, 7, 1)
    }
}
