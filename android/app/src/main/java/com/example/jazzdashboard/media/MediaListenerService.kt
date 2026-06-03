package com.example.jazzdashboard.media
import android.content.ComponentName
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LyricLine(val timeMs: Long, val text: String)

data class MediaState(
    val title: String = "Not Playing",
    val artist: String = "Tap to connect",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val lastPositionUpdateTime: Long = 0L,
    val playbackSpeed: Float = 1f,
    val lyrics: List<LyricLine> = emptyList()
)

class MediaListenerService : NotificationListenerService() {
    
    companion object {
        private val _mediaState = MutableStateFlow(MediaState())
        val mediaState: StateFlow<MediaState> = _mediaState
        
        var activeController: MediaController? = null

        fun playPause() {
            activeController?.transportControls?.let {
                if (_mediaState.value.isPlaying) it.pause() else it.play()
            }
        }
        
        fun skipNext() {
            activeController?.transportControls?.skipToNext()
        }
        
        fun skipPrevious() {
            activeController?.transportControls?.skipToPrevious()
        }
    }

    private var sessionManager: MediaSessionManager? = null

    private val activeSessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        activeController?.unregisterCallback(mediaControllerCallback)
        activeController = controllers?.firstOrNull()
        activeController?.registerCallback(mediaControllerCallback)
        updateState()
    }

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: android.media.session.PlaybackState?) {
            updateState()
        }

        override fun onMetadataChanged(metadata: android.media.MediaMetadata?) {
            updateState()
        }
    }

    override fun onListenerConnected() {
        sessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        try {
            val component = ComponentName(this, MediaListenerService::class.java)
            sessionManager?.addOnActiveSessionsChangedListener(activeSessionsListener, component)
            // Initialize with current session
            val sessions = sessionManager?.getActiveSessions(component)
            activeSessionsListener.onActiveSessionsChanged(sessions)
        } catch (e: SecurityException) {
            // Permission not granted yet
            _mediaState.value = MediaState("Permission Required", "Grant Notification Access", false)
        }
    }

    override fun onListenerDisconnected() {
        sessionManager?.removeOnActiveSessionsChangedListener(activeSessionsListener)
        activeController?.unregisterCallback(mediaControllerCallback)
    }

    private var lastTrackId: String = ""

    private fun updateState() {
        val controller = activeController
        if (controller == null) {
            _mediaState.value = MediaState("Not Playing", "No active media", false)
            lastTrackId = ""
            return
        }
        
        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown Track"
        val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
        val isPlaying = playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING ||
                        playbackState?.state == android.media.session.PlaybackState.STATE_BUFFERING
        
        val positionMs = playbackState?.position ?: 0L
        val lastUpdateTime = playbackState?.lastPositionUpdateTime ?: 0L
        val speed = playbackState?.playbackSpeed ?: 1f

        val trackId = "$title-$artist"
        
        // Preserve lyrics if track hasn't changed
        val currentLyrics = if (trackId == lastTrackId) _mediaState.value.lyrics else emptyList()
        
        _mediaState.value = _mediaState.value.copy(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            positionMs = positionMs,
            lastPositionUpdateTime = lastUpdateTime,
            playbackSpeed = speed,
            lyrics = currentLyrics
        )

        // Fetch lyrics if track changed
        if (trackId != lastTrackId && title != "Unknown Track") {
            lastTrackId = trackId
            LyricsEngine.fetchLyrics(title, artist) { lyrics ->
                if (lastTrackId == trackId) {
                    _mediaState.value = _mediaState.value.copy(lyrics = lyrics)
                }
            }
        }
    }
}

