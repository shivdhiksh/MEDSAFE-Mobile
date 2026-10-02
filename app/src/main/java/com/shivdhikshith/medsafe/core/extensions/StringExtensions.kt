package com.shivdhikshith.medsafe.core.extensions

fun String?.orDefault(default: String): String =
    if (this.isNullOrBlank()) default else this
