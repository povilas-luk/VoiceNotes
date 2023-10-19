package com.example.voicenotes.main.drawer

import androidx.compose.ui.graphics.vector.ImageVector

data class DrawerItem(
    val route: String,
    val title: String,
    val contentDescription: String,
    val icon: ImageVector
)