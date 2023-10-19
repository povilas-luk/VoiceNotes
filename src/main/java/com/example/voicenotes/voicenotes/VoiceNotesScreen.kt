package com.example.voicenotes

import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.voicenotes.main.TopBar
import com.example.voicenotes.ui.theme.VoiceNotesTheme
import com.example.voicenotes.util.timeMillsToString
import com.example.voicenotes.voicenotes.VoiceNotesRecorder
import com.example.voicenotes.voicenotes.VoiceNotesViewModel
import com.google.accompanist.permissions.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceNotesScreen(
    navController: NavController,
    viewModel: VoiceNotesViewModel = hiltViewModel(),
    openDrawer: () -> Unit,
) {
    //var paddingValues by remember { mutableStateOf(PaddingValues()) }
    val voiceNotesState = viewModel.voiceNotesState.collectAsState().value

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    Scaffold(
        topBar = {
            TopBar(
                title = "Voice Notes",
                onMenuButtonClicked = { openDrawer() },
                searchFieldTextChanged = { viewModel.onEvent(VoiceNotesViewModel.VoiceNotesEvent.SearchVoiceNotesList(it)) }
            )
        },

        floatingActionButton = {
            RecordFloatingActionButton(
                startRecording = { viewModel.onEvent(VoiceNotesViewModel.VoiceNotesEvent.StartRecording) },
                stopRecording = { viewModel.onEvent(VoiceNotesViewModel.VoiceNotesEvent.StopRecording) },
                voiceNotesState = voiceNotesState
            )
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            VoiceNotesScreenBottomBar(
                changeMicrophoneVolume = {

                },
            isRecording = voiceNotesState.currentlyRecording
            )
        }

    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (permissionsState.allPermissionsGranted) {
                when {
                    voiceNotesState.loading -> {
                        VoiceNotesListLoading()
                    }
                    voiceNotesState.voiceNotesList.isNotEmpty() -> {
                        ShowVoiceNotesList(
                            voiceNoteList = voiceNotesState.voiceNotesList,
                            currentlyPlayingVoiceNoteId = voiceNotesState.currentlyPlayingVoiceNoteId,
                            currentVoiceNoteRecordingPosition = voiceNotesState.currentVoiceNoteRecordingPosition,
                            startPlaying = { viewModel.onEvent(VoiceNotesViewModel.VoiceNotesEvent.StartPlaying(it)) },
                            stopPlaying = { viewModel.onEvent(VoiceNotesViewModel.VoiceNotesEvent.StopPlaying(it)) },
                            deleteVoiceNote = { viewModel.onEvent(VoiceNotesViewModel.VoiceNotesEvent.DeleteVoiceNote(it)) }
                        )
                    }
                    voiceNotesState.error != null -> {
                        ErrorMessage(
                            message = voiceNotesState.error,
                            reload = { viewModel.onEvent(VoiceNotesViewModel.VoiceNotesEvent.Reload) }
                        )
                    }
                }

            } else if (permissionsState.shouldShowRationale) {
                SecondPermissionRequest(permissionRequest = {
                    permissionsState.launchMultiplePermissionRequest()
                })

            } else if (!permissionsState.allPermissionsGranted && !permissionsState.shouldShowRationale) {
                PermissionPermanentlyDenied()
            }

        }
    }

}

@Composable
fun SecondPermissionRequest(permissionRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Permissions required")
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Button(
            onClick = {
                permissionRequest()
            },
        ) {
            Text("Request Again")
        }
    }
}

@Composable
fun ErrorMessage(message: String, reload: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = { reload() }
        ) {
            Text(
                text = "Reload"
            )
        }
    }
}

@Composable
fun PermissionPermanentlyDenied() {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        Text(
            text = "Permissions were permanently denied. To use Voice Notes permissions must be enabled in settings.",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VoiceNotesScreenBottomBar(
    changeMicrophoneVolume: () -> Unit,
    isRecording: Boolean,
) {
    BottomAppBar(
        cutoutShape = MaterialTheme.shapes.small.copy(
            CornerSize(percent = 50)
        ),
        backgroundColor = MaterialTheme.colors.primaryVariant,
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecordingTimer(isRecording = isRecording)
            Spacer(modifier = Modifier.padding(8.dp))
            IconButton(onClick = {
                changeMicrophoneVolume()
            }) {
                Icon(imageVector = ImageVector.vectorResource(
                    id = R.drawable.ic_baseline_mic_none_24),
                    contentDescription = "Microphone volume",
                    tint = MaterialTheme.colors.onPrimary,)
            }
        }
    }
}

@Composable 
fun RecordingTimer(isRecording: Boolean) {
    var currentTime by remember { mutableStateOf(0) }
    LaunchedEffect(key1 = isRecording, key2 = currentTime) {
        if (isRecording) {
            delay(100)
            currentTime += 100
        } else {
            currentTime = 0
        }
    }
    Log.i("timertimer", currentTime.toString())
    Text(text = timeMillsToString(currentTime.toLong()))
}

@Composable
fun RecordFloatingActionButton(
    startRecording: () -> Unit,
    stopRecording: () -> Unit,
    voiceNotesState: VoiceNotesViewModel.VoiceNotesState
) {
    val interactionSource = remember { MutableInteractionSource() }
    val recordButtonPressed by interactionSource.collectIsPressedAsState()
    var recordIconColor by remember { mutableStateOf(Color.Red) }
    var recordButtonColor by remember { mutableStateOf(Color.White) }
    var recordButtonBoarderColor by remember { mutableStateOf(Color.Black) }
    var isRecording by remember { mutableStateOf(false) }

    if (recordButtonPressed) {
        if (!isRecording) {
            startRecording()
        }
        isRecording = true
        DisposableEffect(Unit) {
            onDispose {
                isRecording = false
                stopRecording()
            }
        }
    }

    if (isRecording) {
        recordIconColor = Color.Black
        recordButtonColor = Color.Red
        recordButtonBoarderColor = Color.Red
    }
    if (!isRecording) {
        recordIconColor = Color.Red
        recordButtonColor = Color.White
        recordButtonBoarderColor = Color.Black
    }

    FloatingActionButton(
        modifier = Modifier
            .border(width = 4.dp, color = recordButtonBoarderColor, shape = CircleShape),
        onClick = {
            /*if (permissionsGranted) {
                //startRecording()
            }*/
        },
        backgroundColor = recordButtonColor,
        interactionSource = interactionSource
    ) {
        Icon(imageVector = ImageVector.vectorResource(
            id = R.drawable.ic_baseline_fiber_manual_record_24),
            contentDescription = "Record note",
            tint = recordIconColor
        )
    }
}

@Composable
fun VoiceNotesListLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = modifier)
    }

}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShowVoiceNotesList(
    modifier: Modifier = Modifier,
    voiceNoteList: List<VoiceNote>,
    currentlyPlayingVoiceNoteId: String?,
    currentVoiceNoteRecordingPosition: Int,
    startPlaying: (String) -> Unit,
    stopPlaying: (String) -> Unit,
    deleteVoiceNote: (VoiceNote) -> Unit,
) {

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        itemsIndexed(
            items = voiceNoteList,
            key = { index, item ->
                item.hashCode()
            }
        ) { index, item ->

            val dismissState = rememberDismissState()
            if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                deleteVoiceNote(item)
            }

            SwipeToDismiss(
                state = dismissState,
                directions = setOf(DismissDirection.EndToStart),
                dismissThresholds = { direction ->
                    FractionalThreshold(if (direction == DismissDirection.StartToEnd) 0.25f else 0.5f)
                },
                background = {
                    val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                    val color by animateColorAsState(
                        when (dismissState.targetValue) {
                            DismissValue.Default -> Color.LightGray
                            DismissValue.DismissedToEnd -> Color.Green
                            DismissValue.DismissedToStart -> Color.Red
                        }
                    )
                    val alignment = when (direction) {
                        DismissDirection.StartToEnd -> Alignment.CenterStart
                        DismissDirection.EndToStart -> Alignment.CenterEnd
                    }
                    val icon = when (direction) {
                        DismissDirection.StartToEnd -> Icons.Default.Done
                        DismissDirection.EndToStart -> Icons.Default.Delete
                    }
                    val scale by animateFloatAsState(
                        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                    )

                    Card(
                        elevation = 8.dp,
                        backgroundColor = color,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Transparent),
                            contentAlignment = alignment
                        ) {
                            Icon(
                                icon,
                                contentDescription = "Delete voice note",
                                modifier = Modifier
                                    .size(32.dp)
                                    .scale(scale)
                            )
                        }
                    }
                },
                dismissContent = {
                    VoiceNoteListItem(
                        voiceNote = item,
                        isPlaying = item.noteId == currentlyPlayingVoiceNoteId,
                        startPlaying = {
                            startPlaying(it)
                        },
                        stopPlaying = {
                            stopPlaying(it)
                        },
                        currentVoiceNoteRecordingPosition = if (item.noteId == currentlyPlayingVoiceNoteId) currentVoiceNoteRecordingPosition else item.currentPosition
                    )
                },
            )
        }
    }
    Spacer(modifier = Modifier.padding(vertical = 100.dp))
}


@Composable
fun VoiceNoteListItem(
    voiceNote: VoiceNote,
    isPlaying: Boolean,
    currentVoiceNoteRecordingPosition: Int,
    startPlaying: (String) -> Unit,
    stopPlaying: (String) -> Unit,
) {

    Card(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(),
        elevation = 8.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
        ) {
            Row(modifier = Modifier
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp)
                ) {
                    Text(text = voiceNote.title)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.ENGLISH).format(Date(voiceNote.createdAt)))
                }
                IconButton(onClick = {
                    if (isPlaying) {
                        stopPlaying(voiceNote.noteId)
                    } else {
                        startPlaying(voiceNote.noteId)
                    }
                }) {
                    Icon(imageVector = ImageVector.vectorResource(
                        id = if (isPlaying) {
                            R.drawable.ic_baseline_pause_24
                        } else {
                            R.drawable.ic_baseline_play_arrow_24
                               }),
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colors.onBackground,)
                    }

            }
            Row(modifier = Modifier
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(value = if (isPlaying && currentVoiceNoteRecordingPosition > 0) currentVoiceNoteRecordingPosition.toFloat() else voiceNote.currentPosition.toFloat(),
                    valueRange = 0f..voiceNote.duration.toFloat(),
                    onValueChange = { //sliderPosition = it
                        //Log.i("sliderrange", sliderPosition.toString())
                    },
                    modifier = Modifier
                        .weight(1f)
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = timeMillsToString(voiceNote.duration.toLong()) , //SimpleDateFormat("mm:ss", Locale.ENGLISH).format(Date(voiceNote.recordLength)),
                    modifier =  Modifier
                        .wrapContentWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceNotesPreview() {
    VoiceNotesTheme {
        //VoiceNotesScreen(navController = NavController(LocalContext.current), {})
    }

}