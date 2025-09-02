package com.example.saferep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.saferep.ui.proto.CameraScreen
import com.example.saferep.ui.proto.HomeScreen
import com.example.saferep.ui.proto.PhotoSettingsScreen
import com.example.saferep.ui.theme.SaferepTheme
import com.example.saferep.model.PhotoSettingViewModel
import com.example.saferep.ui.proto.PhotoPreviewScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaferepTheme {
                val navController = rememberNavController()

                val photoSettingViewModel: PhotoSettingViewModel = viewModel()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    composable(
                        route = "photo_settings/{siteName}",
                        arguments = listOf(navArgument("siteName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val siteName = backStackEntry.arguments?.getString("siteName") ?: ""
                        // ✅ ViewModel 전달
                        PhotoSettingsScreen(
                            navController = navController,
                            siteName = siteName,
                            viewModel = photoSettingViewModel
                        )
                    }
                    composable("camera_screen") {
                        CameraScreen(
                            navController = navController,
                            viewModel = photoSettingViewModel
                        )
                    }
                    composable("photo_preview") {
                        PhotoPreviewScreen(
                            navController = navController,
                            viewModel = photoSettingViewModel
                        )
                    }
                }
            }
        }
    }
}