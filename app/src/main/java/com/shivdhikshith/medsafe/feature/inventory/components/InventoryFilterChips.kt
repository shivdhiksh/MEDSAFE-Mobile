package com.shivdhikshith.medsafe.feature.inventory.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus

@Composable
fun InventoryFilterChips(
    selectedStatus: ExpiryStatus?,
    onStatusSelected: (ExpiryStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedStatus == null,
            onClick = { onStatusSelected(null) },
            label = { Text("All") }
        )

        FilterChip(
            selected = selectedStatus == ExpiryStatus.VALID,
            onClick = { onStatusSelected(ExpiryStatus.VALID) },
            label = { Text("Valid") }
        )

        FilterChip(
            selected = selectedStatus == ExpiryStatus.EXPIRING_SOON,
            onClick = { onStatusSelected(ExpiryStatus.EXPIRING_SOON) },
            label = { Text("Expiring Soon") }
        )

        FilterChip(
            selected = selectedStatus == ExpiryStatus.EXPIRED,
            onClick = { onStatusSelected(ExpiryStatus.EXPIRED) },
            label = { Text("Expired") }
        )

        FilterChip(
            selected = selectedStatus == ExpiryStatus.DISPOSED,
            onClick = { onStatusSelected(ExpiryStatus.DISPOSED) },
            label = { Text("Disposed") }
        )
    }
}
