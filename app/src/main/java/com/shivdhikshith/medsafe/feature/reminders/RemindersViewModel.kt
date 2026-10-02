package com.shivdhikshith.medsafe.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shivdhikshith.medsafe.core.notifications.ReminderScheduler
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.Reminder
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import com.shivdhikshith.medsafe.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class RemindersUiState(
    val isLoading: Boolean = true,
    val reminders: List<Reminder> = emptyList(),
    val medicines: List<Medicine> = emptyList(),
    val errorMessage: String? = null
)

class RemindersViewModel(
    private val reminderRepository: ReminderRepository,
    private val medicineRepository: MedicineRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            reminderRepository.observeAllReminders(),
            medicineRepository.observeAllMedicinesWithBatches()
        ) { remindersList, medicinesWithBatches ->
            val selectableMedicines = medicinesWithBatches
                .filter { it.batches.any { batch -> batch.status != ExpiryStatus.DISPOSED } }
                .map { it.medicine }
            RemindersUiState(
                isLoading = false,
                reminders = remindersList,
                medicines = selectableMedicines,
                errorMessage = null
            )
        }
            .onEach { newState ->
                _uiState.value = newState
            }
            .catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Failed to load reminders"
                )
            }
            .launchIn(viewModelScope)
    }

    fun addReminder(
        medicineId: Long,
        title: String,
        timeInMillis: Long,
        recurrence: String
    ) {
        viewModelScope.launch {
            val newReminder = Reminder(
                medicineId = medicineId,
                title = title.trim(),
                timeInMillis = timeInMillis,
                recurrence = recurrence,
                isEnabled = true
            )
            val generatedId = reminderRepository.insertReminder(newReminder)
            val savedReminder = newReminder.copy(id = generatedId)

            val med = medicineRepository.getMedicineWithBatches(medicineId)
            val medName = med?.medicine?.name ?: ""
            reminderScheduler.schedule(savedReminder, medName)
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updatedState = !reminder.isEnabled
            reminderRepository.updateReminderEnabled(reminder.id, updatedState)
            val updatedReminder = reminder.copy(isEnabled = updatedState)

            if (updatedState) {
                val med = medicineRepository.getMedicineWithBatches(reminder.medicineId)
                val medName = med?.medicine?.name ?: ""
                reminderScheduler.schedule(updatedReminder, medName)
            } else {
                reminderScheduler.cancel(reminder.id)
            }
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            reminderScheduler.cancel(reminderId)
            reminderRepository.deleteReminder(reminderId)
        }
    }

    class Factory(
        private val reminderRepository: ReminderRepository,
        private val medicineRepository: MedicineRepository,
        private val reminderScheduler: ReminderScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RemindersViewModel(
                reminderRepository = reminderRepository,
                medicineRepository = medicineRepository,
                reminderScheduler = reminderScheduler
            ) as T
        }
    }
}
