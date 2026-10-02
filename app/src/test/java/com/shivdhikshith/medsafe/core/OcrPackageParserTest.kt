package com.shivdhikshith.medsafe.core

import com.shivdhikshith.medsafe.core.scanner.OcrPackageParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrPackageParserTest {

    @Test
    fun `parse extracts medicine name, strength, batch number, expiry date and manufacturer`() {
        val sampleText = """
            Paracetamol Tablets IP 500 mg
            Composition: Each tablet contains Paracetamol IP 500mg
            B.No: BT-98421
            Mfg. Date: 02/2024
            Exp. Date: 12/2026
            Mfd by: Cipla Pharmaceuticals Ltd
            Keep out of reach of children
        """.trimIndent()

        val result = OcrPackageParser.parse(sampleText)

        assertEquals("Paracetamol", result.medicineName)
        assertEquals("500 mg", result.strength)
        assertEquals("BT-98421", result.batchNumber)
        assertEquals("2026-12-28", result.expiryDateString)
        assertTrue(result.manufacturer.contains("Cipla"))
    }

    @Test
    fun `parse handles liquid dosage forms and alternate prefixes`() {
        val sampleText = """
            Amoxicillin Oral Suspension
            125 mg
            Batch No: AMX-442
            Expiry: 08-2025
            Manufactured by: GSK Healthcare
        """.trimIndent()

        val result = OcrPackageParser.parse(sampleText)

        assertTrue(result.medicineName.contains("Amoxicillin"))
        assertEquals("125 mg", result.strength)
        assertEquals("AMX-442", result.batchNumber)
        assertEquals("2025-08-28", result.expiryDateString)
    }

    @Test
    fun `parse with empty string returns blank structured data`() {
        val result = OcrPackageParser.parse("")
        assertEquals("", result.medicineName)
        assertEquals("", result.strength)
        assertEquals("", result.batchNumber)
        assertEquals("", result.expiryDateString)
    }
}
