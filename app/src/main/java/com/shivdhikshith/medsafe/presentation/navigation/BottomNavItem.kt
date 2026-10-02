package com.shivdhikshith.medsafe.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Dashboard.route, "Home", Icons.Default.Dashboard)
    object Inventory : BottomNavItem(Screen.Inventory.route, "Inventory", Icons.Default.Inventory2)
    object Reminders : BottomNavItem(Screen.Reminders.route, "Reminders", Icons.Default.Alarm)
    object More : BottomNavItem(Screen.More.route, "More", Icons.Default.MoreHoriz)

    companion object {
        val items = listOf(Home, Inventory, Reminders, More)
    }
}
