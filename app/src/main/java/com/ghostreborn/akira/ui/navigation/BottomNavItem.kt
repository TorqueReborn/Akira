package com.ghostreborn.akira.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    data object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
    data object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}