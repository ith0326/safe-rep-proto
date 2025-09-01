package com.example.saferep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.saferep.ui.proto.CameraScreen
import com.example.saferep.ui.proto.HomeScreen
import com.example.saferep.ui.proto.PhotoSettingsScreen
import com.example.saferep.ui.theme.SaferepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaferepTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    composable(
                        route = "photo_settings/{siteName}",
                        arguments = listOf(navArgument("siteName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val siteName = backStackEntry.arguments?.getString("siteName") ?: ""
                        PhotoSettingsScreen(navController = navController, siteName = siteName)
                    }
                    composable("camera_screen") {
                        CameraScreen(navController = navController)
                    }
                }
            }
        }
    }
}