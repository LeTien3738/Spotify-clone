package com.example.spotifyclone.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession

object PlayerHolder {
	@Volatile
	private var exoPlayerInternal: ExoPlayer? = null
	@Volatile
	private var mediaSessionInternal: MediaSession? = null

	fun exoPlayer(context: Context): ExoPlayer {
		return exoPlayerInternal ?: synchronized(this) {
			exoPlayerInternal ?: ExoPlayer.Builder(context.applicationContext).build().also { exoPlayerInternal = it }
		}
	}

	fun ensureMediaSession(context: Context): MediaSession {
		return mediaSessionInternal ?: synchronized(this) {
			mediaSessionInternal ?: MediaSession.Builder(context.applicationContext, exoPlayer(context)).build().also { mediaSessionInternal = it }
		}
	}

	fun mediaSessionOrNull(): MediaSession? = mediaSessionInternal
}
