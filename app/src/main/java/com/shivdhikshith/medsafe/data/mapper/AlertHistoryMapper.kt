package com.shivdhikshith.medsafe.data.mapper

import com.shivdhikshith.medsafe.data.local.entity.AlertHistoryEntity
import com.shivdhikshith.medsafe.domain.model.AlertHistory

fun AlertHistoryEntity.toDomain(): AlertHistory {
    return AlertHistory(
        id = id,
        batchId = batchId,
        alertType = alertType,
        sentAt = sentAt
    )
}

fun AlertHistory.toEntity(): AlertHistoryEntity {
    return AlertHistoryEntity(
        id = id,
        batchId = batchId,
        alertType = alertType,
        sentAt = sentAt
    )
}
