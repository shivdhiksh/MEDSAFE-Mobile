package com.shivdhikshith.medsafe.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shivdhikshith.medsafe.AppContainer
import com.shivdhikshith.medsafe.feature.about.AboutScreen
import com.shivdhikshith.medsafe.feature.dashboard.DashboardScreen
import com.shivdhikshith.medsafe.feature.dashboard.DashboardViewModel
import com.shivdhikshith.medsafe.feature.inventory.InventoryScreen
import com.shivdhikshith.medsafe.feature.inventory.InventoryViewModel
import com.shivdhikshith.medsafe.feature.medicine.AddMedicineScreen
import com.shivdhikshith.medsafe.feature.medicine.AddMedicineViewModel
import com.shivdhikshith.medsafe.feature.medicine.MedicineDetailScreen
import com.shivdhikshith.medsafe.feature.medicine.MedicineDetailViewModel
import com.shivdhikshith.medsafe.feature.more.MoreScreen
import com.shivdhikshith.medsafe.feature.photos.PhotosScreen
import com.shivdhikshith.medsafe.feature.photos.PhotosViewModel
import com.shivdhikshith.medsafe.feature.reminders.RemindersScreen
import com.shivdhikshith.medsafe.feature.reminders.RemindersViewModel
import com.shivdhikshith.medsafe.feature.scanner.ScannerScreen
import com.shivdhikshith.medsafe.feature.scanner.ScannerViewModel
import com.shivdhikshith.medsafe.feature.settings.SettingsScreen
import com.shivdhikshith.medsafe.feature.settings.SettingsViewModel
import com.shivdhikshith.medsafe.feature.summary.MedicineSummaryScreen
import com.shivdhikshith.medsafe.feature.summary.MedicineSummaryViewModel

@Composable
fun MedSafeNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarRoutes = listOf(
        Screen.Dashboard.route,
        Screen.Inventory.route,
        Screen.Reminders.route,
        Screen.More.route
    )
    val shouldShowBottomBar = currentDestination?.route in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    BottomNavItem.items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(container.getDashboardSummaryUseCase)
                )
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddMedicine = { navController.navigate(Screen.AddMedicine.route) },
                    onNavigateToMedicineDetail = { id ->
                        navController.navigate(Screen.MedicineDetail.createRoute(id))
                    },
                    onNavigateToScanner = {
                        navController.navigate(Screen.Scanner.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Inventory.route) {
                val viewModel: InventoryViewModel = viewModel(
                    factory = InventoryViewModel.Factory(container.observeInventoryUseCase)
                )
                InventoryScreen(
                    viewModel = viewModel,
                    onNavigateToAddMedicine = { navController.navigate(Screen.AddMedicine.route) },
                    onNavigateToMedicineDetail = { id ->
                        navController.navigate(Screen.MedicineDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Reminders.route) {
                val viewModel: RemindersViewModel = viewModel(
                    factory = RemindersViewModel.Factory(
                        reminderRepository = container.reminderRepository,
                        medicineRepository = container.medicineRepository,
                        reminderScheduler = container.reminderScheduler
                    )
                )
                RemindersScreen(viewModel = viewModel)
            }

            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToScanner = { navController.navigate(Screen.Scanner.route) }
                )
            }

            composable(Screen.AddMedicine.route) {
                val viewModel: AddMedicineViewModel = viewModel(
                    factory = AddMedicineViewModel.Factory(container.addMedicineWithBatchUseCase)
                )

                val prevHandle = navController.previousBackStackEntry?.savedStateHandle
                val prefillName = prevHandle?.remove<String>("prefill_name")
                val prefillStrength = prevHandle?.remove<String>("prefill_strength")
                val prefillBatch = prevHandle?.remove<String>("prefill_batch")
                val prefillExpiry = prevHandle?.remove<String>("prefill_expiry")
                val prefillMfg = prevHandle?.remove<String>("prefill_mfg")
                val prefillBarcode = prevHandle?.remove<String>("prefill_barcode")

                LaunchedEffect(prefillName, prefillStrength, prefillBatch, prefillExpiry, prefillMfg, prefillBarcode) {
                    if (prefillName != null || prefillStrength != null || prefillBatch != null || prefillExpiry != null || prefillMfg != null || prefillBarcode != null) {
                        viewModel.prefill(
                            name = prefillName ?: "",
                            strength = prefillStrength ?: "",
                            batchNumber = prefillBatch ?: "",
                            expiryDateString = prefillExpiry ?: "",
                            manufacturer = prefillMfg ?: "",
                            barcode = prefillBarcode ?: ""
                        )
                    }
                }

                AddMedicineScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.MedicineDetail.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val medicineId = backStackEntry.arguments?.getLong("medicineId") ?: 0L
                val viewModel: MedicineDetailViewModel = viewModel(
                    factory = MedicineDetailViewModel.Factory(
                        medicineId = medicineId,
                        getMedicineDetailUseCase = container.getMedicineDetailUseCase,
                        updateBatchStatusUseCase = container.updateBatchStatusUseCase,
                        deleteMedicineUseCase = container.deleteMedicineUseCase,
                        updateMedicineUseCase = container.updateMedicineUseCase,
                        addBatchUseCase = container.addBatchUseCase,
                        updateBatchUseCase = container.updateBatchUseCase,
                        deleteBatchUseCase = container.deleteBatchUseCase
                    )
                )
                MedicineDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSummary = { id ->
                        navController.navigate(Screen.MedicineSummary.createRoute(id))
                    },
                    onNavigateToPhotos = { id ->
                        navController.navigate(Screen.Photos.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.MedicineSummary.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val medicineId = backStackEntry.arguments?.getLong("medicineId") ?: 0L
                val viewModel: MedicineSummaryViewModel = viewModel(
                    factory = MedicineSummaryViewModel.Factory(
                        medicineId = medicineId,
                        getMedicineDetailUseCase = container.getMedicineDetailUseCase,
                        updateMedicineUseCase = container.updateMedicineUseCase
                    )
                )
                MedicineSummaryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Photos.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val medicineId = backStackEntry.arguments?.getLong("medicineId") ?: 0L
                val viewModel: PhotosViewModel = viewModel(
                    factory = PhotosViewModel.Factory(
                        medicineId = medicineId,
                        observePhotosUseCase = container.observePhotosUseCase,
                        addPhotoUseCase = container.addPhotoUseCase,
                        deletePhotoUseCase = container.deletePhotoUseCase
                    )
                )
                PhotosScreen(
                    medicineId = medicineId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Scanner.route) {
                val viewModel: ScannerViewModel = viewModel(
                    factory = ScannerViewModel.Factory(container.medicineRepository)
                )
                ScannerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMedicineDetail = { id ->
                        navController.navigate(Screen.MedicineDetail.createRoute(id))
                    },
                    onNavigateToAddMedicineWithPrefill = { name, strength, batch, expiry, mfg, barcode ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("prefill_name", name)
                        navController.currentBackStackEntry?.savedStateHandle?.set("prefill_strength", strength)
                        navController.currentBackStackEntry?.savedStateHandle?.set("prefill_batch", batch)
                        navController.currentBackStackEntry?.savedStateHandle?.set("prefill_expiry", expiry)
                        navController.currentBackStackEntry?.savedStateHandle?.set("prefill_mfg", mfg)
                        navController.currentBackStackEntry?.savedStateHandle?.set("prefill_barcode", barcode)
                        navController.navigate(Screen.AddMedicine.route)
                    },
                    onNavigateToAddMedicine = {
                        navController.navigate(Screen.AddMedicine.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        observeSettingsUseCase = container.observeSettingsUseCase,
                        updateSettingsUseCase = container.updateSettingsUseCase,
                        dataManagementUseCases = container.dataManagementUseCases
                    )
                )
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.About.route) {
                AboutScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
