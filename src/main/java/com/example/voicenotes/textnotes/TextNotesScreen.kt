package com.example.voicenotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.voicenotes.main.TopBar

@Composable
fun TextNotesScreen(navController: NavController, openDrawer: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = "Text Notes",
            onMenuButtonClicked = { openDrawer() },
            searchFieldTextChanged = { searchFieldTextChanged() }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Text Notes Screen")
        }
    }
}

private fun searchFieldTextChanged() {
    TODO("Not yet implemented")
}
