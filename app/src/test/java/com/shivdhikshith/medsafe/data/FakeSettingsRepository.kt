package com.shivdhikshith.medsafe.data

import com.shivdhikshith.medsafe.domain.model.UserSettings
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    initialSettings: UserSettings = UserSettings()
) : SettingsRepository {

    private val settingsFlow = MutableStateFlow(initialSettings)

    override fun observeSettings(): Flow<UserSettings> = settingsFlow

    override suspend fun getSettings(): UserSettings = settingsFlow.value

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(notificationsEnabled = enabled)
    }

    override suspend fun setAlertDays(days: Int) {
        settingsFlow.value = settingsFlow.value.copy(alertDays = days)
    }

    override suspend fun setAlertThresholdDays(thresholds: Set<Int>) {
        settingsFlow.value = settingsFlow.value.copy(
            alertThresholdDays = thresholds,
            alertDays = thresholds.maxOrNull() ?: settingsFlow.value.alertDays
        )
    }

    override suspend fun setNotifyOnExpired(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(notifyOnExpired = enabled)
    }

    override suspend fun setTheme(theme: String) {
        settingsFlow.value = settingsFlow.value.copy(theme = theme)
    }
}
