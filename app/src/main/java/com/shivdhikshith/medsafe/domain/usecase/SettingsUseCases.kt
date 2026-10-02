package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.domain.model.UserSettings
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<UserSettings> = settingsRepository.observeSettings()
}

class UpdateSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
    }

    suspend fun setAlertDays(days: Int) {
        settingsRepository.setAlertDays(days)
    }

    suspend fun setAlertThresholdDays(thresholds: Set<Int>) {
        settingsRepository.setAlertThresholdDays(thresholds)
    }

    suspend fun setNotifyOnExpired(enabled: Boolean) {
        settingsRepository.setNotifyOnExpired(enabled)
    }

    suspend fun setTheme(theme: String) {
        settingsRepository.setTheme(theme)
    }
}
