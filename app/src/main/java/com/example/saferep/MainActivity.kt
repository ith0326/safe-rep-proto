package com.example.saferep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.saferep.ui.proto.HomeScreen
import com.example.saferep.ui.proto.PhotoSettingsScreen
import com.example.saferep.ui.theme.SaferepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { // 👈 모든 Composable 코드는 이 블록 안에서 시작해야 합니다.

            SaferepTheme {
                // ✅ NavController와 NavHost를 setContent 블록 안으로 이동
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
                }
            }
        }
    }
}