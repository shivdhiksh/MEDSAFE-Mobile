package com.shivdhikshith.medsafe.feature.photos

import com.shivdhikshith.medsafe.domain.model.MedicinePhoto

data class PhotosUiState(
    val isLoading: Boolean = true,
    val photos: List<MedicinePhoto> = emptyList(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)
