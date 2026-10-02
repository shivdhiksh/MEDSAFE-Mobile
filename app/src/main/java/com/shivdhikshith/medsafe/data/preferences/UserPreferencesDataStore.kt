package com.shivdhikshith.medsafe.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shivdhikshith.medsafe.core.common.Constants
import com.shivdhikshith.medsafe.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFERENCES_NAME)

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ALERT_DAYS = intPreferencesKey("alert_days")
        val ALERT_THRESHOLDS = stringPreferencesKey("alert_thresholds")
        val NOTIFY_ON_EXPIRED = booleanPreferencesKey("notify_on_expired")
        val THEME = stringPreferencesKey("theme")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val rawThresholds = preferences[PreferencesKeys.ALERT_THRESHOLDS]
        val legacyAlertDays = preferences[PreferencesKeys.ALERT_DAYS]
        val thresholds = parseThresholds(rawThresholds, legacyAlertDays)
        val notifyOnExpired = preferences[PreferencesKeys.NOTIFY_ON_EXPIRED] ?: true
        val alertDays = legacyAlertDays ?: (thresholds.maxOrNull() ?: Constants.DEFAULT_ALERT_DAYS)

        UserSettings(
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            alertThresholdDays = thresholds,
            notifyOnExpired = notifyOnExpired,
            theme = preferences[PreferencesKeys.THEME] ?: "SYSTEM",
            alertDays = alertDays
        )
    }

    private fun parseThresholds(raw: String?, legacyAlertDays: Int?): Set<Int> {
        if (!raw.isNullOrBlank()) {
            val parsed = raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it > 0 }
                .toSet()
            if (parsed.isNotEmpty()) return parsed
        }
        return if (legacyAlertDays != null && legacyAlertDays > 0) {
            UserSettings.DEFAULT_THRESHOLDS + legacyAlertDays
        } else {
            UserSettings.DEFAULT_THRESHOLDS
        }
    }

    suspend fun getSettings(): UserSettings {
        return userSettingsFlow.first()
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setAlertThresholdDays(thresholds: Set<Int>) {
        context.dataStore.edit { preferences ->
            val formatted = thresholds.sortedDescending().joinToString(",")
            preferences[PreferencesKeys.ALERT_THRESHOLDS] = formatted
            thresholds.maxOrNull()?.let {
                preferences[PreferencesKeys.ALERT_DAYS] = it
            }
        }
    }

    suspend fun setNotifyOnExpired(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_ON_EXPIRED] = enabled
        }
    }

    suspend fun setAlertDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALERT_DAYS] = days
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }
}
