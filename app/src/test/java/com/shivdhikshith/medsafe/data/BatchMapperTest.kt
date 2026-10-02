package com.shivdhikshith.medsafe.data

import com.shivdhikshith.medsafe.data.local.entity.BatchEntity
import com.shivdhikshith.medsafe.data.mapper.toDomain
import com.shivdhikshith.medsafe.data.mapper.toEntity
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class BatchMapperTest {

    @Test
    fun entityToDomain_mapsAllFieldsAndStatusCorrectly() {
        val entity = BatchEntity(
            id = 99L,
            medicineId = 42L,
            batchNumber = "LOT-555",
            expiryDate = 1750000000000L,
            quantity = 3,
            storageLocation = "Top Shelf",
            status = "EXPIRING_SOON",
            openedDate = 1740000000000L,
            disposedDate = null,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val domain = entity.toDomain()

        assertEquals(99L, domain.id)
        assertEquals(42L, domain.medicineId)
        assertEquals("LOT-555", domain.batchNumber)
        assertEquals(1750000000000L, domain.expiryDate)
        assertEquals(3, domain.quantity)
        assertEquals("Top Shelf", domain.storageLocation)
        assertEquals(ExpiryStatus.EXPIRING_SOON, domain.status)
        assertEquals(1740000000000L, domain.openedDate)
        assertEquals(null, domain.disposedDate)
    }

    @Test
    fun domainToEntity_mapsStatusEnumToStringCorrectly() {
        val domain = Batch(
            id = 77L,
            medicineId = 12L,
            batchNumber = "LOT-777",
            expiryDate = 1760000000000L,
            quantity = 5,
            storageLocation = "Drawer",
            status = ExpiryStatus.DISPOSED,
            openedDate = null,
            disposedDate = 1755000000000L,
            createdAt = 3000L,
            updatedAt = 4000L
        )

        val entity = domain.toEntity()

        assertEquals(77L, entity.id)
        assertEquals(12L, entity.medicineId)
        assertEquals("LOT-777", entity.batchNumber)
        assertEquals("DISPOSED", entity.status)
        assertEquals(1755000000000L, entity.disposedDate)
    }
}
