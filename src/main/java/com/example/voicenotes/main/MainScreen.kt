package com.example.voicenotes.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voicenotes.Drawer
import com.example.voicenotes.HomeScreen
import com.example.voicenotes.R
import com.example.voicenotes.TextNotesScreen
import com.example.voicenotes.VoiceNotesScreen
import com.example.voicenotes.main.drawer.DrawerItem
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Surface(color = MaterialTheme.colors.background,
            modifier = Modifier.fillMaxSize()
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val openDrawer = {
            scope.launch {
                drawerState.open()
            }
        }
        ModalDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                Drawer(
                    drawerItems = getDrawerItems(),
                    onDestinationClicked = {
                        scope.launch {
                            drawerState.close()
                        }
                        navController.navigate(it) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = "home_screen"
            ) {
                composable("home_screen") {
                    HomeScreen(navController = navController, openDrawer = {openDrawer()})
                }
                composable("text_notes_screen") {
                    TextNotesScreen(navController = navController, openDrawer = {openDrawer()})
                }
                composable("voice_notes_screen") {
                    VoiceNotesScreen(navController = navController, openDrawer = {openDrawer()})
                }
            }
        }
    }
}

@Composable
fun getDrawerItems(): List<DrawerItem> {
    return listOf(
        DrawerItem(
            route = "home_screen",
            title = "Home",
            contentDescription = "Go to home screen",
            icon = Icons.Default.Home
        ),
        DrawerItem(
            route = "text_notes_screen",
            title = "Text Notes",
            contentDescription = "Go to text notes screen",
            icon = ImageVector.vectorResource(id = R.drawable.ic_baseline_text_snippet_24)
        ),
        DrawerItem(
            route = "voice_notes_screen",
            title = "Voice Notes",
            contentDescription = "Go to voice notes screen",
            icon = ImageVector.vectorResource(id = R.drawable.ic_baseline_record_voice_over_24)
        ),
    )
}

