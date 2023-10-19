package com.example.voicenotes.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicenotes.ui.theme.VoiceNotesTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopBar(title: String = "", onMenuButtonClicked: () -> Unit, searchFieldTextChanged: ((String) -> Unit)? = null) {

    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            IconButton(onClick = { onMenuButtonClicked() } ) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "$title screen")
            }
        },
        backgroundColor = MaterialTheme.colors.primaryVariant,
        actions = {
            if (searchFieldTextChanged != null) {
                SearchTextField(searchFieldTextChanged = {
                    searchFieldTextChanged(it)
                })
            }
            IconButton(onClick = { /*TODO*/ },
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 4.dp))
            {
                Icon(imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More button",
                    tint = MaterialTheme.colors.onPrimary,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    )
}

@Composable
fun SearchTextField(searchFieldTextChanged: ((String) -> Unit)?) {
    var showSearchTextField by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var currentValue by remember { mutableStateOf("") }

    if (showSearchTextField) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                },
            value = currentValue,
            onValueChange = {
                currentValue = it
                if (searchFieldTextChanged != null) {
                    searchFieldTextChanged(it)
                } },
            placeholder = {
                Text(text = "Search")
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                backgroundColor = Color.Transparent,
                cursorColor = MaterialTheme.colors.onPrimary,
                textColor = MaterialTheme.colors.onPrimary,
                trailingIconColor = MaterialTheme.colors.onPrimary,
                placeholderColor = MaterialTheme.colors.onPrimary
            ),
            trailingIcon = {
                IconButton(onClick = {
                    showSearchTextField = false
                    currentValue = ""
                    if (searchFieldTextChanged != null) {
                        searchFieldTextChanged(currentValue)
                    }
                    focusRequester.freeFocus()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear button",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        )
    } else {
        IconButton(onClick = {
            showSearchTextField = true
        }, modifier = Modifier.size(24.dp)
        ) {
            Icon(imageVector = Icons.Filled.Search,
                contentDescription = "Search button",
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    VoiceNotesTheme {
        TopBar("Test", {})
    }

}