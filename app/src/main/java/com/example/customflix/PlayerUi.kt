package com.example.customflix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerUi(
    isPlaying: Boolean,
    isPlayPauseClicked: (Boolean) -> Unit,
    isExpanded: Boolean,
    onExpandCollapseClicked: () -> Unit,
    onReplayClicked: (Long) -> Unit,
    onForwardClicked: (Long) -> Unit,
    onSeekBarPositionChange: (Long) -> Unit,
    onSeekBarPositionChangeFinished: (Long) -> Unit,
    currentPosition: Long,
    duration: Long,
    isBuffering: Boolean,
    onShowBottomSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isBuffering) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { onReplayClicked(currentPosition) },
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.outline_replay_24),
                            contentDescription = "Replay",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "10",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                IconButton(
                    onClick = {
                        isPlayPauseClicked(!isPlaying)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) {
                            ImageVector.vectorResource(id = R.drawable.baseline_pause_24)
                        } else {
                            ImageVector.vectorResource(id = R.drawable.baseline_play_arrow_24)
                        },
                        contentDescription = stringResource(R.string.play_or_pause),
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(
                    onClick = { onForwardClicked(currentPosition) },
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.outline_forward_media_24),
                            contentDescription = "Forward",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "10",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatDuration(currentPosition),
                    color = Color.White,
                )

                Slider(
                    value = currentPosition.toFloat()
                        .coerceIn(0f, duration.coerceAtLeast(1).toFloat()),
                    onValueChange = { newPosition ->
                        onSeekBarPositionChange(newPosition.toLong())
                    },
                    onValueChangeFinished = {
                        onSeekBarPositionChangeFinished(currentPosition)
                    },
                    valueRange = 0f..duration.coerceAtLeast(1).toFloat(),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .shadow(elevation = 4.dp, shape = CircleShape)
                                .background(Color.White, shape = CircleShape)
                        )
                    },
                    track = { sliderState ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        if (duration > 0) {
                                            (sliderState.value / duration.toFloat()).coerceIn(
                                                0f,
                                                1f
                                            )
                                        } else 0f
                                    )
                                    .height(4.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatDuration(duration - currentPosition),
                    color = Color.White,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = onExpandCollapseClicked,
                    shape = CircleShape,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) {
                            ImageVector.vectorResource(id = R.drawable.outline_collapse_content_24)
                        } else {
                            ImageVector.vectorResource(id = R.drawable.outline_expand_content_24)
                        },
                        contentDescription = "Forward",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                }
                IconButton(
                    onClick = onShowBottomSheet,
                    shape = CircleShape,
                    modifier = Modifier.size(30.dp)
                ) {

                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.outline_settings_24),
                        contentDescription = "Forward",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

@Preview
@Composable
private fun PlayerUiPreview() {
    PlayerUi(
        isPlaying = true,
        isPlayPauseClicked = {},
        isExpanded = false,
        onExpandCollapseClicked = {},
        onReplayClicked = {},
        onForwardClicked = {},
        onSeekBarPositionChange = {},
        onSeekBarPositionChangeFinished = {},
        currentPosition = 0,
        duration = 0,
        isBuffering = false,
        onShowBottomSheet = {}
    )
}