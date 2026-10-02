package com.shivdhikshith.medsafe.data.repository

import com.shivdhikshith.medsafe.core.common.DefaultDispatcherProvider
import com.shivdhikshith.medsafe.core.common.DispatcherProvider
import com.shivdhikshith.medsafe.data.preferences.UserPreferencesDataStore
import com.shivdhikshith.medsafe.domain.model.UserSettings
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val preferencesDataStore: UserPreferencesDataStore,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : SettingsRepository {

    override fun observeSettings(): Flow<UserSettings> {
        return preferencesDataStore.userSettingsFlow.flowOn(dispatchers.io)
    }

    override suspend fun getSettings(): UserSettings = withContext(dispatchers.io) {
        preferencesDataStore.getSettings()
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) = withContext(dispatchers.io) {
        preferencesDataStore.setNotificationsEnabled(enabled)
    }

    override suspend fun setAlertDays(days: Int) = withContext(dispatchers.io) {
        preferencesDataStore.setAlertDays(days)
    }

    override suspend fun setAlertThresholdDays(thresholds: Set<Int>) = withContext(dispatchers.io) {
        preferencesDataStore.setAlertThresholdDays(thresholds)
    }

    override suspend fun setNotifyOnExpired(enabled: Boolean) = withContext(dispatchers.io) {
        preferencesDataStore.setNotifyOnExpired(enabled)
    }

    override suspend fun setTheme(theme: String) = withContext(dispatchers.io) {
        preferencesDataStore.setTheme(theme)
    }
}
