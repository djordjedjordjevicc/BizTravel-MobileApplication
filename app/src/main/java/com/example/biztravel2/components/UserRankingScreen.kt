package com.example.biztravel2.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.biztravel2.server.UserRankingViewModel

@Composable
fun UserRankingScreen(navController: NavHostController? = null, viewModel: UserRankingViewModel = viewModel()) {
    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    BackHandler {
        // Ostaviti prazno da onemogući povratak
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Ranking Page",
                style = MaterialTheme.typography.h4,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        itemsIndexed(users) { index, (userId, user) ->
            val isCurrentUser = currentUser?.let { it.firstName == user.firstName && it.lastName == user.lastName } ?: false
            val backgroundColor = if (isCurrentUser) Color.Yellow else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(8.dp)
            ) {
                Text(text = "${index + 1}. ${user.firstName} ${user.lastName}")
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "${user.points} points")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                    navController?.navigate("profile")
                }
            ) {
                Text(text = "Go to Profile")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserRankingScreenPreview() {
    UserRankingScreen()
}
