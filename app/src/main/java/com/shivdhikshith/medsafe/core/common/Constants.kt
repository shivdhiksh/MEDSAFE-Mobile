package com.shivdhikshith.medsafe.core.common

object Constants {
    const val DATABASE_NAME = "medsafe_database"
    const val PREFERENCES_NAME = "medsafe_preferences"
    const val DEFAULT_ALERT_DAYS = 30

    // Safety disclaimer required across all informational contexts
    const val SAFETY_DISCLAIMER =
        "This application helps organize medicine expiry information. It does not provide medical advice. Always verify the original packaging and consult a pharmacist or healthcare professional if you are unsure whether a medicine can be used."

    // Status display titles - Notice: VALID explicitly does NOT claim "safe to use"
    const val STATUS_VALID_TEXT = "Expiry date not reached"
    const val STATUS_EXPIRING_SOON_TEXT = "Expiring Soon"
    const val STATUS_EXPIRED_TEXT = "Expired"
    const val STATUS_DISPOSED_TEXT = "Disposed"
}
