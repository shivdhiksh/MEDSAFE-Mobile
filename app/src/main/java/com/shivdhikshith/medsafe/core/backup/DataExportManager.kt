package com.shivdhikshith.medsafe.core.backup

import com.shivdhikshith.medsafe.core.extensions.toDisplayDate
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches

object DataExportManager {

    fun exportToCsv(medicines: List<MedicineWithBatches>): String {
        val builder = java.lang.StringBuilder()
        builder.append("Medicine Name,Strength,Form / Type,Manufacturer,Barcode,Notes,Batch Number,Expiry Date,Quantity,Storage Location,Status,Disposed Date\n")

        medicines.forEach { medWithBatches ->
            val med = medWithBatches.medicine
            if (medWithBatches.batches.isEmpty()) {
                builder.append("${escapeCsv(med.name)},${escapeCsv(med.strength)},${escapeCsv(med.medicineType)},${escapeCsv(med.manufacturer)},${escapeCsv(med.barcode)},${escapeCsv(med.notes)},,,,,,\n")
            } else {
                medWithBatches.batches.forEach { batch ->
                    val disposed = batch.disposedDate?.toDisplayDate() ?: ""
                    builder.append(
                        "${escapeCsv(med.name)},${escapeCsv(med.strength)},${escapeCsv(med.medicineType)}," +
                        "${escapeCsv(med.manufacturer)},${escapeCsv(med.barcode)},${escapeCsv(med.notes)}," +
                        "${escapeCsv(batch.batchNumber)},${batch.expiryDate.toDisplayDate()},${batch.quantity}," +
                        "${escapeCsv(batch.storageLocation)},${batch.status.name},$disposed\n"
                    )
                }
            }
        }
        return builder.toString()
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}
