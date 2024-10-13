package com.example.biztravel2.components

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.biztravel2.server.LocationService
import com.example.biztravel2.server.ProfileViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfilePageScreen(navController: NavHostController? = null, viewModel: ProfileViewModel = viewModel()) {
    val userProfile = viewModel.userProfile.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    BackHandler {
        // Ostaviti prazno da onemogući povratak
    }

    if (userProfile != null) {
        // Koristi LazyColumn umesto Column za skrolovanje
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Text(
                    text = "Profile Page",
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Text(text = "First Name:", style = MaterialTheme.typography.subtitle1)
                Text(text = userProfile.firstName, style = MaterialTheme.typography.body1)
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text(text = "Last Name:", style = MaterialTheme.typography.subtitle1)
                Text(text = userProfile.lastName, style = MaterialTheme.typography.body1)
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text(text = "Phone:", style = MaterialTheme.typography.subtitle1)
                Text(text = userProfile.phoneNumber, style = MaterialTheme.typography.body1)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(text = "Profile Picture:", style = MaterialTheme.typography.subtitle1)
                Image(
                    painter = rememberAsyncImagePainter(userProfile.profileImageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (userProfile.service == "on") {
                            stopLocationService(context)
                            viewModel.updateServiceStatus("off")
                        } else {
                            startLocationService(context)
                            viewModel.updateServiceStatus("on")
                        }
                    }
                ) {
                    Text(text = if (userProfile.service == "on") "Stop Location Service" else "Start Location Service")
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController?.navigate("mapscreen")
                    }
                ) {
                    Text(text = "Go to Map")
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController?.navigate("ranking")
                    }
                ) {
                    Text(text = "Ranking")
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.logout()
                        navController?.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Text(text = "Logout")
                }
            }
        }
    } else {
        Text(text = "Loading...", style = MaterialTheme.typography.h6)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun startLocationService(context: Context) {
    val intent = Intent(context, LocationService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

private fun stopLocationService(context: Context) {
    val intent = Intent(context, LocationService::class.java)
    context.stopService(intent)
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ProfilePageScreenPreview() {
    ProfilePageScreen()
}
