package com.example.voicenotes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicenotes.main.drawer.DrawerItem
import com.example.voicenotes.main.getDrawerItems
import com.example.voicenotes.ui.theme.VoiceNotesTheme

@Composable
fun Drawer(
    drawerItems: List<DrawerItem>,
    onDestinationClicked: (route: String) -> Unit
) {
    Column(
        Modifier
            .padding(start = 24.dp, top = 48.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "App icon",
            modifier = Modifier
                .fillMaxWidth()
        )
        LazyColumn() {
            items(drawerItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDestinationClicked(item.route)
                        }
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.title,
                        style = TextStyle(fontSize = 18.sp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VoiceNotesTheme {
        Drawer(getDrawerItems(), {})
    }

}