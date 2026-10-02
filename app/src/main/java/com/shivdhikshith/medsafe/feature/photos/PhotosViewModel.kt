package com.shivdhikshith.medsafe.feature.photos

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shivdhikshith.medsafe.domain.model.MedicinePhoto
import com.shivdhikshith.medsafe.domain.usecase.AddPhotoUseCase
import com.shivdhikshith.medsafe.domain.usecase.DeletePhotoUseCase
import com.shivdhikshith.medsafe.domain.usecase.ObservePhotosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PhotosViewModel(
    private val medicineId: Long,
    private val observePhotosUseCase: com.shivdhikshith.medsafe.domain.usecase.ObservePhotosUseCase,
    private val addPhotoUseCase: AddPhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotosUiState())
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

    init {
        observePhotos()
    }

    private fun observePhotos() {
        observePhotosUseCase(medicineId)
            .onEach { photos ->
                _uiState.value = PhotosUiState(
                    isLoading = false,
                    photos = photos,
                    errorMessage = null
                )
            }
            .catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Failed to load photos"
                )
            }
            .launchIn(viewModelScope)
    }

    fun addPhotoFromUri(uri: Uri, caption: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                addPhotoUseCase.fromUri(medicineId, uri, caption)
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save photo"
                )
            }
        }
    }

    fun addPhotoFromBitmap(bitmap: Bitmap, caption: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                addPhotoUseCase.fromBitmap(medicineId, bitmap, caption)
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to capture photo"
                )
            }
        }
    }

    fun deletePhoto(photo: MedicinePhoto) {
        viewModelScope.launch {
            try {
                deletePhotoUseCase(photo)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to delete photo"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val medicineId: Long,
        private val observePhotosUseCase: com.shivdhikshith.medsafe.domain.usecase.ObservePhotosUseCase,
        private val addPhotoUseCase: AddPhotoUseCase,
        private val deletePhotoUseCase: DeletePhotoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhotosViewModel(
                medicineId = medicineId,
                observePhotosUseCase = observePhotosUseCase,
                addPhotoUseCase = addPhotoUseCase,
                deletePhotoUseCase = deletePhotoUseCase
            ) as T
        }
    }
}
