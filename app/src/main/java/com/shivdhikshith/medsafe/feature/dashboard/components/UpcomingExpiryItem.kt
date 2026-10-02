package com.shivdhikshith.medsafe.feature.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shivdhikshith.medsafe.core.extensions.toDisplayDate
import com.shivdhikshith.medsafe.core.utils.ExpiryCalculator
import com.shivdhikshith.medsafe.domain.model.BatchWithMedicine
import com.shivdhikshith.medsafe.presentation.components.MedicineStatusBadge

@Composable
fun UpcomingExpiryItem(
    item: BatchWithMedicine,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val batch = item.batch
    val medicine = item.medicine
    val daysRemaining = ExpiryCalculator.daysRemaining(batch.expiryDate)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = when {
                    daysRemaining < 0 -> "Expired ${-daysRemaining} day(s) ago (${batch.expiryDate.toDisplayDate()})"
                    daysRemaining == 0L -> "Expires today! (${batch.expiryDate.toDisplayDate()})"
                    daysRemaining == 1L -> "Expires tomorrow (${batch.expiryDate.toDisplayDate()})"
                    else -> "Expires in $daysRemaining days (${batch.expiryDate.toDisplayDate()})"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (batch.batchNumber.isNotBlank() || batch.storageLocation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = listOf(
                            if (batch.batchNumber.isNotBlank()) "Batch: ${batch.batchNumber}" else "",
                            if (batch.storageLocation.isNotBlank()) "Loc: ${batch.storageLocation}" else ""
                        ).filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            MedicineStatusBadge(status = batch.status)
        }
    }
}
