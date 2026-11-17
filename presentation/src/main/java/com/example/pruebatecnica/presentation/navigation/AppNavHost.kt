package com.example.pruebatecnica.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pruebatecnica.presentation.detail.DetailRoute
import com.example.pruebatecnica.presentation.search.SearchRoute

/**
 * App navigation graph. Each destination's ViewModel is provided by Hilt and scoped to
 * its back stack entry. Pure wiring (no UI of its own), so it has no preview.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = SearchDestination) {
        composable<SearchDestination> { entry ->
            SearchRoute(
                onNavigateToDetail = { id ->
                    if (entry.isResumed()) navController.navigate(DetailDestination(id))
                },
            )
        }

        composable<DetailDestination> { entry ->
            DetailRoute(
                onNavigateBack = { if (entry.isResumed()) navController.popBackStack() },
            )
        }
    }
}

/** Ignores duplicated navigation events (e.g. double taps) while a transition is running. */
private fun NavBackStackEntry.isResumed(): Boolean =
    lifecycle.currentState == Lifecycle.State.RESUMED
