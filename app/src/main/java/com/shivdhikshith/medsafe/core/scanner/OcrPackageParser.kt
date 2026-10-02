package com.shivdhikshith.medsafe.core.scanner

data class ExtractedPackageData(
    val medicineName: String = "",
    val strength: String = "",
    val batchNumber: String = "",
    val expiryDateString: String = "",
    val manufacturer: String = "",
    val rawText: String = ""
)

object OcrPackageParser {

    private val STRENGTH_REGEX = Regex(
        "(?i)\\b(\\d+(?:\\.\\d+)?\\s*(?:mg|mcg|g|ml|iu|%|mg/ml))\\b"
    )

    private val BATCH_REGEX = Regex(
        "(?i)(?:batch\\s*no\\.?|lot/b\\.?no\\.?|b\\.?\\s*no\\.?|b/n|batch|lot)[:\\s]*([a-zA-Z0-9\\-_/]+)"
    )

    private val EXPIRY_REGEX = Regex(
        "(?i)(?:exp(?:\\.|iry)?(?:\\s*date)?|use\\s*before|best\\s*before)[:\\s]*([0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}[/-][0-9]{1,2}|[a-zA-Z]{3,9}[-\\s][0-9]{2,4})"
    )

    private val MFG_REGEX = Regex(
        "(?i)(?:manufactured\\s+by|marketed\\s+by|mfd\\.?\\s*by|mfg\\.?\\s*by)[:\\s]*([a-zA-Z0-9\\s,\\.]+)"
    )

    fun parse(rawText: String): ExtractedPackageData {
        if (rawText.isBlank()) return ExtractedPackageData()

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        var detectedStrength = ""
        var detectedBatch = ""
        var detectedExpiry = ""
        var detectedManufacturer = ""

        for (line in lines) {
            if (detectedStrength.isBlank()) {
                val match = STRENGTH_REGEX.find(line)
                if (match != null) detectedStrength = match.groupValues[1].trim()
            }
            if (detectedBatch.isBlank()) {
                val match = BATCH_REGEX.find(line)
                if (match != null) detectedBatch = match.groupValues[1].trim()
            }
            if (detectedExpiry.isBlank()) {
                val match = EXPIRY_REGEX.find(line)
                if (match != null) detectedExpiry = match.groupValues[1].trim()
            }
            if (detectedManufacturer.isBlank()) {
                val match = MFG_REGEX.find(line)
                if (match != null) detectedManufacturer = match.groupValues[1].trim().take(40)
            }
        }

        val ignoreWords = setOf(
            "rx", "rx only", "mrp", "mfg", "exp", "batch", "lot",
            "tablets", "capsules", "syrup", "for external use only",
            "keep out of reach of children"
        )
        val candidateName = lines.firstOrNull { line ->
            val lower = line.lowercase()
            line.length in 3..40 &&
                    !ignoreWords.contains(lower) &&
                    !lower.startsWith("batch") &&
                    !lower.startsWith("b.no") &&
                    !lower.startsWith("exp") &&
                    !lower.startsWith("mfd") &&
                    !lower.startsWith("mrp") &&
                    !lower.startsWith("composition") &&
                    !line.all { it.isDigit() || it.isWhitespace() || it == '.' }
        } ?: ""

        var cleanedName = candidateName
        if (detectedStrength.isNotBlank()) {
            cleanedName = cleanedName.replace(detectedStrength, "", ignoreCase = true)
        }
        cleanedName = cleanedName
            .replace(STRENGTH_REGEX, "")
            .replace(Regex("(?i)\\b(?:tablets?|capsules?|oral suspension|suspension|syrup|injection|drops?|cream|ointment|gel|ip|usp|bp)\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        return ExtractedPackageData(
            medicineName = if (cleanedName.isNotBlank()) cleanedName else candidateName,
            strength = detectedStrength,
            batchNumber = detectedBatch,
            expiryDateString = normalizeExpiryDate(detectedExpiry),
            manufacturer = detectedManufacturer,
            rawText = rawText
        )
    }

    private fun normalizeExpiryDate(rawDate: String): String {
        if (rawDate.isBlank()) return ""
        val clean = rawDate.replace(" ", "").replace("/", "-")
        val parts = clean.split("-")
        return when (parts.size) {
            2 -> {
                val p1 = parts[0]
                val p2 = parts[1]
                if (p1.length == 4) {
                    val month = p2.padStart(2, '0')
                    "$p1-$month-28"
                } else if (p2.length == 4) {
                    val month = p1.padStart(2, '0')
                    "$p2-$month-28"
                } else if (p2.length == 2) {
                    val year = "20$p2"
                    val month = p1.padStart(2, '0')
                    "$year-$month-28"
                } else {
                    rawDate
                }
            }
            3 -> {
                if (parts[0].length == 4) {
                    "${parts[0]}-${parts[1].padStart(2, '0')}-${parts[2].padStart(2, '0')}"
                } else if (parts[2].length == 4) {
                    "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                } else {
                    rawDate
                }
            }
            else -> rawDate
        }
    }
}
