package com.example.spotifyclone.player

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.spotifyclone.service.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Track(
	val url: String,
	val title: String,
	val artworkUrl: String
)

data class PlayerUiState(
	val title: String = "",
	val artworkUrl: String = "",
	val isPlaying: Boolean = false,
	val progress: Float = 0f,
	val positionMs: Long = 0L,
	val durationMs: Long = 0L
)

class PlayerController(private val appContext: Context) {
	private val exoPlayer = PlayerHolder.exoPlayer(appContext)
	private val scope = CoroutineScope(Dispatchers.Main)
	private var progressJob: Job? = null

	private val _uiState = MutableStateFlow(PlayerUiState())
	val uiState: StateFlow<PlayerUiState> = _uiState

	private var queue: List<Track> = emptyList()
	private var currentIndex: Int = -1

	init {
		PlayerHolder.ensureMediaSession(appContext)
		exoPlayer.addListener(object : Player.Listener {
			override fun onIsPlayingChanged(isPlaying: Boolean) {
				_uiState.value = _uiState.value.copy(isPlaying = isPlaying)
				if (isPlaying) startProgress() else stopProgress()
			}
			override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
				if (mediaItem != null) {
					val url = mediaItem.localConfiguration?.uri.toString()
					val idx = queue.indexOfFirst { it.url == url }
					if (idx >= 0) applyTrack(idx)
				}
			}
		})
	}

	fun play(url: String, title: String) {
		playQueue(listOf(Track(url, title, artworkUrl = "")), 0)
	}

	fun playQueue(tracks: List<Track>, startIndex: Int) {
		queue = tracks
		currentIndex = startIndex.coerceIn(0, tracks.lastIndex)
		exoPlayer.setMediaItems(tracks.map { MediaItem.fromUri(it.url) }, currentIndex, 0)
		exoPlayer.prepare()
		exoPlayer.playWhenReady = true
		applyTrack(currentIndex)
		startService()
	}

	fun next() { if (queue.isNotEmpty()) seekToIndex((currentIndex + 1).coerceAtMost(queue.lastIndex)) }
	fun previous() { if (queue.isNotEmpty()) seekToIndex((currentIndex - 1).coerceAtLeast(0)) }

	private fun seekToIndex(index: Int) {
		currentIndex = index
		exoPlayer.seekTo(index, 0)
		exoPlayer.playWhenReady = true
		applyTrack(index)
	}

	private fun applyTrack(index: Int) {
		val t = queue.getOrNull(index) ?: return
		_uiState.value = _uiState.value.copy(
			title = t.title,
			artworkUrl = t.artworkUrl,
			progress = 0f,
			positionMs = 0L,
			durationMs = exoPlayer.duration.takeIf { it > 0 } ?: 0L
		)
	}

	fun togglePlayPause() { exoPlayer.playWhenReady = !exoPlayer.playWhenReady }

	fun seekTo(progressFraction: Float) {
		val duration = exoPlayer.duration
		if (duration > 0) exoPlayer.seekTo((duration * progressFraction).toLong())
	}

	fun getCurrentTrack(): Track? = queue.getOrNull(currentIndex)

	private fun startProgress() {
		progressJob?.cancel()
		progressJob = scope.launch {
			while (true) {
				val d = exoPlayer.duration
				val p = exoPlayer.currentPosition
				val fraction = if (d > 0) p.toFloat() / d.toFloat() else 0f
				_uiState.value = _uiState.value.copy(progress = fraction, positionMs = p, durationMs = d.takeIf { it > 0 } ?: 0L)
				kotlinx.coroutines.delay(250)
			}
		}
	}

	private fun stopProgress() { progressJob?.cancel(); progressJob = null }

	private fun startService() {
		try {
			val intent = Intent(appContext, PlaybackService::class.java)
			if (Build.VERSION.SDK_INT >= 26) {
				ContextCompat.startForegroundService(appContext, intent)
			} else {
				appContext.startService(intent)
			}
		} catch (_: Exception) {
		}
	}

	fun release() { /* no-op to allow background playback */ }
}
