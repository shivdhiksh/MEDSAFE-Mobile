package com.shivdhikshith.medsafe.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shivdhikshith.medsafe.core.backup.BackupManager
import com.shivdhikshith.medsafe.core.backup.BackupPayload
import com.shivdhikshith.medsafe.core.backup.BackupValidationResult
import com.shivdhikshith.medsafe.domain.model.UserSettings
import com.shivdhikshith.medsafe.domain.usecase.DataManagementUseCases
import com.shivdhikshith.medsafe.domain.usecase.ObserveSettingsUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: UserSettings = UserSettings(),
    val actionMessage: String? = null,
    val isExporting: Boolean = false,
    val pendingRestoreValidation: BackupValidationResult? = null
)

class SettingsViewModel(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val dataManagementUseCases: DataManagementUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettingsUseCase()
            .onEach { settings ->
                _uiState.value = _uiState.value.copy(isLoading = false, settings = settings)
            }
            .launchIn(viewModelScope)
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.setNotificationsEnabled(enabled)
        }
    }

    fun updateAlertDays(days: Int) {
        viewModelScope.launch {
            updateSettingsUseCase.setAlertDays(days)
        }
    }

    fun toggleThreshold(days: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val currentThresholds = _uiState.value.settings.alertThresholdDays
            val updated = if (isEnabled) {
                currentThresholds + days
            } else {
                val filtered = currentThresholds - days
                if (filtered.isNotEmpty()) filtered else currentThresholds
            }
            updateSettingsUseCase.setAlertThresholdDays(updated)
        }
    }

    fun toggleNotifyOnExpired(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.setNotifyOnExpired(enabled)
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            updateSettingsUseCase.setTheme(theme)
        }
    }

    fun exportCsv(onReady: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val csv = dataManagementUseCases.exportCsv()
                _uiState.value = _uiState.value.copy(isExporting = false)
                onReady(csv)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    actionMessage = "Failed to export CSV: ${e.message}"
                )
            }
        }
    }

    fun exportJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val json = dataManagementUseCases.exportJson()
                _uiState.value = _uiState.value.copy(isExporting = false)
                onReady(json)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    actionMessage = "Failed to export JSON: ${e.message}"
                )
            }
        }
    }

    fun createBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val backup = dataManagementUseCases.createBackup()
                _uiState.value = _uiState.value.copy(isExporting = false)
                onReady(backup)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    actionMessage = "Failed to create backup: ${e.message}"
                )
            }
        }
    }

    fun getBackupFileName(): String = BackupManager.generateBackupFileName()

    fun onBackupFileSaved(fileName: String) {
        _uiState.value = _uiState.value.copy(
            actionMessage = "Backup saved successfully: $fileName"
        )
    }

    fun onRestoreFileSelected(jsonString: String) {
        val result = dataManagementUseCases.validateBackup(jsonString)
        if (result.isValid) {
            _uiState.value = _uiState.value.copy(pendingRestoreValidation = result)
        } else {
            _uiState.value = _uiState.value.copy(
                actionMessage = result.errorMessage ?: "Invalid backup file"
            )
        }
    }

    fun dismissRestoreDialog() {
        _uiState.value = _uiState.value.copy(pendingRestoreValidation = null)
    }

    fun confirmRestore(replaceAll: Boolean) {
        val payload = _uiState.value.pendingRestoreValidation?.payload ?: return
        viewModelScope.launch {
            try {
                dataManagementUseCases.restoreBackup(payload, replaceAll)
                _uiState.value = _uiState.value.copy(
                    pendingRestoreValidation = null,
                    actionMessage = "Data restored successfully (${payload.medicines.size} medicines, ${payload.batches.size} batches)"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    pendingRestoreValidation = null,
                    actionMessage = "Failed to restore backup: ${e.message}"
                )
            }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }

    class Factory(
        private val observeSettingsUseCase: ObserveSettingsUseCase,
        private val updateSettingsUseCase: UpdateSettingsUseCase,
        private val dataManagementUseCases: DataManagementUseCases
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                observeSettingsUseCase,
                updateSettingsUseCase,
                dataManagementUseCases
            ) as T
        }
    }
}
