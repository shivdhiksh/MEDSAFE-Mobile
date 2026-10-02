package com.shivdhikshith.medsafe.core.extensions

import com.shivdhikshith.medsafe.core.utils.DateUtils
import java.time.LocalDate

fun Long.toDisplayDate(): String = DateUtils.formatToDisplayDate(this)

fun Long.toIsoDate(): String = DateUtils.formatToIsoDate(this)

fun LocalDate.toEpochMillis(): Long = DateUtils.localDateToEpochMillis(this)
