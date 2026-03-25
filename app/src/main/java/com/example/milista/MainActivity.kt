package com.example.milista

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.milista.ui.AddReminderScreen
import com.example.milista.ui.AddTaskScreen
import com.example.milista.ui.HomeScreen
import com.example.milista.ui.ListasScreen
import com.example.milista.ui.MiListaViewModel
import com.example.milista.ui.theme.MiListaTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MiListaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiListaTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController, 
                    startDestination = "home",
                    enterTransition = { 
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn() 
                    },
                    exitTransition = { 
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut() 
                    },
                    popEnterTransition = { 
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn() 
                    },
                    popExitTransition = { 
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut() 
                    }
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onAddLista = { navController.navigate("listas") },
                            onAddReminder = { tipo -> navController.navigate("add_reminder/$tipo") }
                        )
                    }
                    composable("listas") {
                        ListasScreen(
                            viewModel = viewModel,
                            onNavigateToListDetails = { listaId ->
                                navController.navigate("add_task/$listaId")
                            }
                        )
                    }
                    composable(
                        "add_task/{listaId}",
                        arguments = listOf(navArgument("listaId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val listaId = backStackEntry.arguments?.getInt("listaId") ?: 0
                        AddTaskScreen(
                            viewModel = viewModel,
                            listaId = listaId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        "add_reminder/{tipo}",
                        arguments = listOf(navArgument("tipo") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val tipo = backStackEntry.arguments?.getString("tipo") ?: "Otro"
                        AddReminderScreen(
                            viewModel = viewModel,
                            tipo = tipo,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
