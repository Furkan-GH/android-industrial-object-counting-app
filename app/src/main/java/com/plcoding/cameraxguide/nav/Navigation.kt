package com.plcoding.cameraxguide.nav


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plcoding.cameraxguide.screens.CameraScreen
import com.plcoding.cameraxguide.screens.LogScreen
import com.plcoding.cameraxguide.screens.LoginScreen
import com.plcoding.cameraxguide.screens.RegisterScreen

object Routes {
    const val REGISTER = "register"
    const val LOGIN = "login"
    const val CAMERA = "camera"
    const val LOG = "log"
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToCamera = {
                    navController.navigate(Routes.CAMERA) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBackToRegister = {
                    navController.navigate(Routes.REGISTER) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CAMERA) {
            CameraScreen(navController)
        }
        composable(Routes.LOG) {
            LogScreen(navController)
        }
    }
}
