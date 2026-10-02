package com.shivdhikshith.medsafe.domain

import com.shivdhikshith.medsafe.domain.usecase.ValidateMedicineInputUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateMedicineInputUseCaseTest {

    private val useCase = ValidateMedicineInputUseCase()

    @Test
    fun invoke_allValidInputs_returnsValid() {
        val result = useCase(
            name = "Paracetamol",
            expiryDateEpochMillis = 1750000000000L,
            quantity = 2
        )

        assertTrue(result.isValid)
        assertNull(result.nameError)
        assertNull(result.expiryError)
        assertNull(result.quantityError)
    }

    @Test
    fun invoke_blankName_returnsInvalid() {
        val result = useCase(
            name = "   ",
            expiryDateEpochMillis = 1750000000000L,
            quantity = 2
        )

        assertFalse(result.isValid)
        assertNotNull(result.nameError)
    }

    @Test
    fun invoke_nullOrZeroExpiry_returnsInvalid() {
        val result = useCase(
            name = "Amoxicillin",
            expiryDateEpochMillis = 0L,
            quantity = 1
        )

        assertFalse(result.isValid)
        assertNotNull(result.expiryError)
    }

    @Test
    fun invoke_zeroOrNegativeQuantity_returnsInvalid() {
        val result = useCase(
            name = "Amoxicillin",
            expiryDateEpochMillis = 1750000000000L,
            quantity = 0
        )

        assertFalse(result.isValid)
        assertNotNull(result.quantityError)
    }
}
