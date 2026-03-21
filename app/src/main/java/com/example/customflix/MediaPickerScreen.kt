package com.example.customflix

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPickerScreen(
    onExpandedChanged: (Boolean) -> Unit,
    onOrientationLandscape: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    viewModel: MediaPickerViewModel = viewModel()
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val options = listOf(1F, 1.25F, 1.5F, 2F)

    val player = retain {
        ExoPlayer
            .Builder(context.applicationContext)
            .build()
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            player.setMediaItem(MediaItem.fromUri(it))
            player.prepare()
            player.play()
        }
    }

    val uiState = viewModel.uiState

    BackHandler(enabled = uiState.isPlayerExpanded) {
        viewModel.onPlayerExpandedChanged(false)
    }

    LaunchedEffect(uiState.isPlayerExpanded) {
        onExpandedChanged(uiState.isPlayerExpanded)
        if (uiState.isPlayerExpanded) {
            onOrientationLandscape((player.videoSize.width.toDouble() / player.videoSize.height.toDouble()) > 1)
        } else {
            onOrientationLandscape(false)
        }
    }

    LaunchedEffect(uiState.isPlayerUiVisible, uiState.isSeeking, uiState.isPlaying) {
        if (uiState.isPlayerUiVisible && !uiState.isSeeking && uiState.isPlaying) {
            delay(5000)
            viewModel.onPlayerUiVisibilityChanged(false)
        }
    }

    LaunchedEffect(player, uiState.isPlaying, uiState.isSeeking) {
        while (uiState.isPlaying) {
            if (!uiState.isSeeking) {
                viewModel.onCurrentPositionChanged(player.currentPosition.coerceAtLeast(0))
            }
            delay(16L)
        }
    }

    RetainedEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                super.onIsPlayingChanged(playing)
                viewModel.onIsPlayingChanged(playing)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                viewModel.onIsBufferingChanged(playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    viewModel.onDurationChanged(player.duration)
                }
            }
        }

        player.addListener(listener)
        onRetire {
            player.removeListener(listener)
            player.release()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            if (uiState.isPlayerExpanded)
                Arrangement.Center
            else
                Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        if (!uiState.isPlayerExpanded) {
            Button(onClick = {
                videoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly
                    )
                )
            }) {
                Text(text = "Pick Video")
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        ) {
            ContentFrame(
                player = player,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = null,
                        indication = null
                    ) {
                        viewModel.onPlayerUiVisibilityChanged(!uiState.isPlayerUiVisible)
                    }
            )
            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AnimatedVisibility(
                    visible = uiState.isPlayerUiVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    PlayerUi(
                        isPlaying = uiState.isPlaying,
                        isPlayPauseClicked = {
                            when {
                                !uiState.isPlaying && player.playbackState == Player.STATE_ENDED -> {
                                    player.seekTo(0)
                                    player.play()
                                }

                                !uiState.isPlaying -> {
                                    player.play()
                                }

                                uiState.isPlaying -> {
                                    player.pause()
                                }
                            }
                        },
                        isExpanded = uiState.isPlayerExpanded,
                        onExpandCollapseClicked = {
                            viewModel.onPlayerExpandedChanged(!uiState.isPlayerExpanded)
                        },
                        onReplayClicked = {
                            player.seekTo(
                                player.currentPosition.minus(10_000L)
                                    .coerceAtLeast(0L)
                            )
                            viewModel.onCurrentPositionChanged(it)
                        },
                        onForwardClicked = {
                            player.seekTo(
                                player.currentPosition.plus(10_000L)
                                    .coerceAtMost(player.duration)
                            )
                            viewModel.onCurrentPositionChanged(it)
                        },
                        onSeekBarPositionChange = {
                            viewModel.onIsSeekingChanged(true)
                            viewModel.onCurrentPositionChanged(it)
                        },
                        onSeekBarPositionChangeFinished = {
                            player.seekTo(it)
                            viewModel.onIsSeekingChanged(false)
                        },
                        currentPosition = uiState.currentPosition,
                        duration = uiState.duration,
                        isBuffering = uiState.isBuffering,

                        onShowBottomSheet = {
                            showBottomSheet = !showBottomSheet
                        }
                    )
                }
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                modifier = Modifier.fillMaxWidth(),
                sheetState = sheetState,
                onDismissRequest = { showBottomSheet = false },
                containerColor = Color(0xFF1C1C1E),
                contentWindowInsets = {
                    BottomSheetDefaults.windowInsets.add(
                        WindowInsets(
                            left = 16.dp,
                            right = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        )
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Playback Speed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    // Custom Segmented Button
                    Row(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C2C2E)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        options.forEachIndexed { index, speed ->
                            val isSelected = index == selectedIndex
                            val label = if (speed == 1F) "1x" else "${speed}x"

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Color(0xFF48484A) else Color.Transparent
                                    )
                                    .clickable {
                                        selectedIndex = index
                                        player.setPlaybackSpeed(speed)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (index != options.lastIndex) {
                                VerticalDivider(
                                    modifier = Modifier.fillMaxHeight(0.5F),
                                    color = Color(0xFF48484A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MediaPickerScreenPreview() {
    MediaPickerScreen(
        onExpandedChanged = {},
        onOrientationLandscape = {}
    )
}