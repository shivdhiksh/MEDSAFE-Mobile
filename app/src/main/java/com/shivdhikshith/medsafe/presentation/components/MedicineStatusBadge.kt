package com.shivdhikshith.medsafe.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.presentation.theme.StatusDisposedGrey
import com.shivdhikshith.medsafe.presentation.theme.StatusDisposedGreyContainer
import com.shivdhikshith.medsafe.presentation.theme.StatusDisposedGreyText
import com.shivdhikshith.medsafe.presentation.theme.StatusExpiredRed
import com.shivdhikshith.medsafe.presentation.theme.StatusExpiredRedContainer
import com.shivdhikshith.medsafe.presentation.theme.StatusExpiredRedText
import com.shivdhikshith.medsafe.presentation.theme.StatusValidGreen
import com.shivdhikshith.medsafe.presentation.theme.StatusValidGreenContainer
import com.shivdhikshith.medsafe.presentation.theme.StatusValidGreenText
import com.shivdhikshith.medsafe.presentation.theme.StatusWarningAmber
import com.shivdhikshith.medsafe.presentation.theme.StatusWarningAmberContainer
import com.shivdhikshith.medsafe.presentation.theme.StatusWarningAmberText

@Composable
fun MedicineStatusBadge(
    status: ExpiryStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, iconVector) = when (status) {
        ExpiryStatus.VALID -> Triple(StatusValidGreenContainer, StatusValidGreenText, Icons.Default.CheckCircle)
        ExpiryStatus.EXPIRING_SOON -> Triple(StatusWarningAmberContainer, StatusWarningAmberText, Icons.Default.Warning)
        ExpiryStatus.EXPIRED -> Triple(StatusExpiredRedContainer, StatusExpiredRedText, Icons.Default.Error)
        ExpiryStatus.DISPOSED -> Triple(StatusDisposedGreyContainer, StatusDisposedGreyText, Icons.Default.Delete)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { contentDescription = "Status: ${status.label}" },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}
