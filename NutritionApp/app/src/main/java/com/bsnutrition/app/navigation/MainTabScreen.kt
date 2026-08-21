package com.bsnutrition.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bsnutrition.app.core.model.User
import com.bsnutrition.app.feature.add.AddScreen
import com.bsnutrition.app.feature.diary.DiaryScreen
import com.bsnutrition.app.feature.home.HomeScreen
import com.bsnutrition.app.feature.more.MoreScreen
import com.bsnutrition.app.feature.progress.ProgressScreen

@Composable
fun MainTabScreen(
    user: User?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDestination by rememberSaveable { mutableStateOf(TopLevelDestination.HOME) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    val selected = selectedDestination == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = { Text(text = destination.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedDestination) {
                TopLevelDestination.HOME -> HomeScreen(user = user)
                TopLevelDestination.DIARY -> DiaryScreen()
                TopLevelDestination.ADD -> AddScreen()
                TopLevelDestination.PROGRESS -> ProgressScreen()
                TopLevelDestination.MORE -> MoreScreen(user = user, onLogout = onLogout)
            }
        }
    }
}
