package com.example.customflix

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class MediaPickerUiState(
    val isPlaying: Boolean = false,
    val isSeeking: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isBuffering: Boolean = false,
    val isPlayerUiVisible: Boolean = false,
    val isPlayerExpanded: Boolean = false
)

class MediaPickerViewModel : ViewModel() {
    var uiState by mutableStateOf(MediaPickerUiState())
        private set

    fun onIsPlayingChanged(isPlaying: Boolean) {
        uiState = uiState.copy(isPlaying = isPlaying)
    }

    fun onIsSeekingChanged(isSeeking: Boolean) {
        uiState = uiState.copy(isSeeking = isSeeking)
    }

    fun onCurrentPositionChanged(position: Long) {
        uiState = uiState.copy(currentPosition = position)
    }

    fun onDurationChanged(duration: Long) {
        uiState = uiState.copy(duration = duration)
    }

    fun onIsBufferingChanged(isBuffering: Boolean) {
        uiState = uiState.copy(isBuffering = isBuffering)
    }

    fun onPlayerUiVisibilityChanged(isVisible: Boolean) {
        uiState = uiState.copy(isPlayerUiVisible = isVisible)
    }

    fun onPlayerExpandedChanged(isExpanded: Boolean) {
        uiState = uiState.copy(isPlayerExpanded = isExpanded)
    }
}