package com.shivdhikshith.medsafe.domain.model

enum class ExpiryStatus(val label: String) {
    VALID("Expiry date not reached"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
    DISPOSED("Disposed");

    val isExpired: Boolean get() = this == EXPIRED
    val isExpiringSoon: Boolean get() = this == EXPIRING_SOON
    val isValid: Boolean get() = this == VALID
    val isDisposed: Boolean get() = this == DISPOSED
}
