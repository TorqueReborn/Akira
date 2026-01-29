package com.ghostreborn.akira.ui.navigation

import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ghostreborn.akira.ui.screens.HomeScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen()
        }
        composable(BottomNavItem.Search.route) {
            HomeScreen()
        }
        composable(BottomNavItem.Favorites.route) {
            HomeScreen()
        }
        composable(BottomNavItem.Settings.route) {
            HomeScreen()
        }
    }
}
