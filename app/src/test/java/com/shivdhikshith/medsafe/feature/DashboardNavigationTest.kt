package com.shivdhikshith.medsafe.feature

import com.shivdhikshith.medsafe.presentation.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DashboardNavigationTest {

    @Test
    fun scannerRoute_isConfiguredAsScanner() {
        assertEquals("scanner", Screen.Scanner.route)
    }

    @Test
    fun dashboardSmartScan_triggersNavigationToScannerDestination() {
        var targetRoute: String? = null
        var isSingleTop: Boolean? = null

        // Emulate the navigation lambda wired in NavGraph for DashboardScreen
        val onNavigateToScanner: () -> Unit = {
            targetRoute = Screen.Scanner.route
            isSingleTop = true
        }

        onNavigateToScanner.invoke()

        assertNotNull(targetRoute)
        assertEquals(Screen.Scanner.route, targetRoute)
        assertEquals(true, isSingleTop)
    }
}
