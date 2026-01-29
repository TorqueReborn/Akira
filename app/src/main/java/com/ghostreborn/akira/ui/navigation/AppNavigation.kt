package com.ghostreborn.akira.ui.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.padding
import com.ghostreborn.akira.ui.screens.HomeScreen
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun AppNavigation(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = Modifier.padding(paddingValues)
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
