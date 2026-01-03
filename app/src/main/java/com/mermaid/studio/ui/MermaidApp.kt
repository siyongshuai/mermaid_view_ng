package com.mermaid.studio.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mermaid.studio.ui.screens.EditorScreen

/**
 * Main app composable with navigation
 */
@Composable
fun MermaidApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "editor",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("editor") {
            EditorScreen()
        }
        
        // Future screens can be added here:
        // composable("diagrams") { DiagramsScreen() }
        // composable("settings") { SettingsScreen() }
    }
}

/**
 * Navigation destinations
 */
sealed class Screen(val route: String) {
    data object Editor : Screen("editor")
    data object Diagrams : Screen("diagrams")
    data object Settings : Screen("settings")
}

