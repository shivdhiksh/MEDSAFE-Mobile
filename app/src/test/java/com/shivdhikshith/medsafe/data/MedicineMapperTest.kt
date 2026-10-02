package com.shivdhikshith.medsafe.data

import com.shivdhikshith.medsafe.data.local.entity.MedicineEntity
import com.shivdhikshith.medsafe.data.mapper.toDomain
import com.shivdhikshith.medsafe.data.mapper.toEntity
import com.shivdhikshith.medsafe.domain.model.Medicine
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicineMapperTest {

    @Test
    fun entityToDomain_mapsAllFieldsAccurately() {
        val entity = MedicineEntity(
            id = 42L,
            name = "Metformin",
            strength = "500mg",
            medicineType = "Tablet",
            manufacturer = "Sun Pharma",
            barcode = "8901234567890",
            notes = "Take with dinner",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val domain = entity.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("Metformin", domain.name)
        assertEquals("500mg", domain.strength)
        assertEquals("Tablet", domain.medicineType)
        assertEquals("Sun Pharma", domain.manufacturer)
        assertEquals("8901234567890", domain.barcode)
        assertEquals("Take with dinner", domain.notes)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
    }

    @Test
    fun domainToEntity_mapsAllFieldsAccurately() {
        val domain = Medicine(
            id = 15L,
            name = "Cetirizine",
            strength = "10mg",
            medicineType = "Tablet",
            manufacturer = "Dr Reddy",
            barcode = "8909876543210",
            notes = "Night only",
            createdAt = 3000L,
            updatedAt = 4000L
        )

        val entity = domain.toEntity()

        assertEquals(15L, entity.id)
        assertEquals("Cetirizine", entity.name)
        assertEquals("10mg", entity.strength)
        assertEquals("Tablet", entity.medicineType)
        assertEquals("Dr Reddy", entity.manufacturer)
        assertEquals("8909876543210", entity.barcode)
        assertEquals("Night only", entity.notes)
        assertEquals(3000L, entity.createdAt)
        assertEquals(4000L, entity.updatedAt)
    }
}
