package com.shivdhikshith.medsafe.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Inventory : Screen("inventory")
    object Reminders : Screen("reminders")
    object More : Screen("more")

    object AddMedicine : Screen("add_medicine")
    
    object MedicineDetail : Screen("medicine_detail/{medicineId}") {
        fun createRoute(medicineId: Long): String = "medicine_detail/$medicineId"
    }

    object MedicineSummary : Screen("medicine_summary/{medicineId}") {
        fun createRoute(medicineId: Long): String = "medicine_summary/$medicineId"
    }

    object Photos : Screen("photos/{medicineId}") {
        fun createRoute(medicineId: Long): String = "photos/$medicineId"
    }

    object Scanner : Screen("scanner")
    object Settings : Screen("settings")
    object About : Screen("about")
}
