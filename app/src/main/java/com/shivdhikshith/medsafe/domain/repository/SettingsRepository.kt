package com.shivdhikshith.medsafe.domain.repository

import com.shivdhikshith.medsafe.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<UserSettings>
    suspend fun getSettings(): UserSettings
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setAlertDays(days: Int)
    suspend fun setAlertThresholdDays(thresholds: Set<Int>)
    suspend fun setNotifyOnExpired(enabled: Boolean)
    suspend fun setTheme(theme: String)
}
