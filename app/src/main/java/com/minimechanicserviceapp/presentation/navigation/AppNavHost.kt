package com.minimechanicserviceapp.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.minimechanicserviceapp.presentation.details.MechanicDetailsRoute
import com.minimechanicserviceapp.presentation.home.HomeRoute
import com.minimechanicserviceapp.presentation.request.RequestServiceRoute
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeDestination,
    ) {
        composable<HomeDestination> {
            HomeRoute(
                onMechanicClick = { mechanicId ->
                    navController.navigate(MechanicDetailsDestination(mechanicId))
                },
            )
        }
        composable<MechanicDetailsDestination> {
            MechanicDetailsRoute(
                onBack = { navController.popBackStack() },
                onRequestService = { mechanicId ->
                    navController.navigate(RequestServiceDestination(mechanicId))
                },
            )
        }

        composable<RequestServiceDestination> {
            RequestServiceRoute(
                onBack = { navController.popBackStack() },
            )
        }
    }
}