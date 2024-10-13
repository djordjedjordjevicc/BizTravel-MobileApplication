@file:Suppress("NAME_SHADOWING")

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.biztravel2.components.AddLocationScreen
import com.example.biztravel2.components.LocationDetailsScreen
import com.example.biztravel2.components.LoginScreen
import com.example.biztravel2.components.MapScreen
import com.example.biztravel2.components.ProfilePageScreen
import com.example.biztravel2.components.SignUpScreen
import com.example.biztravel2.components.UserRankingScreen
import com.example.biztravel2.server.LocationCommentsScreen
import com.example.biztravel2.server.LocationDetailsViewModel
import com.google.firebase.auth.FirebaseAuth


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()


    var startDestination by remember { mutableStateOf("login") }


    LaunchedEffect(Unit) {
        // Postavi početnu destinaciju nakon što se izvrši provera prijave
        startDestination = if (auth.currentUser != null) "profile" else "login"
    }


    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("mapscreen") { MapScreen(navController) }

        composable("addlocation/{latitude}/{longitude}") { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDouble() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDouble() ?: 0.0
            AddLocationScreen(
                navController,
                latitude = latitude,
                longitude = longitude
            )
        }
        composable("locationdetails/{latitude}/{longitude}") { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0

            val viewModel: LocationDetailsViewModel = viewModel() // ili bilo koji način dobijanja viewModel-a koji koristite
            LocationDetailsScreen(navController,latitude = latitude, longitude = longitude, viewModel = viewModel)
        }
        composable("locationcomments/{latitude}/{longitude}") { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0
            val viewModel: LocationDetailsViewModel = viewModel()

            LocationCommentsScreen(
                navController,
                latitude = latitude,
                longitude = longitude,
                viewModel = viewModel

            )
        }
        composable("profile") { ProfilePageScreen(navController) }
        composable("ranking") { UserRankingScreen(navController) }

    }
}
