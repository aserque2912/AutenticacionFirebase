package com.adrianserranoquero.autenticacionfirebase.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adrianserranoquero.autenticacionfirebase.data.AuthManager
import com.adrianserranoquero.autenticacionfirebase.data.HomeViewModel
import com.adrianserranoquero.autenticacionfirebase.screen.ForgotPasswordScreen
import com.adrianserranoquero.autenticacionfirebase.screen.HomeScreen
import com.adrianserranoquero.autenticacionfirebase.screen.LoginScreen
import com.adrianserranoquero.autenticacionfirebase.screen.SignUpScreen

@Composable
fun Navegacion(auth: AuthManager) {
    val navController = rememberNavController()


    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                auth,
                { navController.navigate(SignUp) },
                {
                    navController.navigate(Home) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                { navController.navigate(ForgotPassword) }
            )
        }

        composable<SignUp> {
            SignUpScreen(
                auth,
                navigateToLogin = { navController.navigate(Login) },
                navigateToHome = { navController.navigate(Home) }
            )
        }


        composable<Home> {
            HomeScreen(
                auth,
                {
                    navController.navigate(Login) {
                        popUpTo(Home){ inclusive = true }
                    }
                },
                viewModel = HomeViewModel()
            )
        }

        composable <ForgotPassword> {
            ForgotPasswordScreen(
                auth
            ) { navController.navigate(Login) {
                popUpTo(Login){ inclusive = true }
            } }
        }
    }
}