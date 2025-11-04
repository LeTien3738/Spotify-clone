package com.example.spotifyclone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.spotifyclone.R
import com.example.spotifyclone.player.PlayerHolder

@UnstableApi
class PlaybackService : MediaSessionService() {
	private var mediaSession: MediaSession? = null
	private var notificationManager: PlayerNotificationManager? = null

	override fun onCreate() {
		super.onCreate()
		mediaSession = PlayerHolder.ensureMediaSession(this)
		createChannel()
		try {
			notificationManager = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, CHANNEL_ID)
				.setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
					override fun getCurrentContentTitle(player: androidx.media3.common.Player): CharSequence {
						return mediaSession?.player?.mediaMetadata?.title ?: getString(R.string.app_name)
					}
					override fun createCurrentContentIntent(player: androidx.media3.common.Player) = mediaSession?.sessionActivity
					override fun getCurrentContentText(player: androidx.media3.common.Player): CharSequence? = mediaSession?.player?.mediaMetadata?.artist
					override fun getCurrentLargeIcon(player: androidx.media3.common.Player, callback: PlayerNotificationManager.BitmapCallback) = null
				})
				.setSmallIconResourceId(R.drawable.ic_notification)
				.build()

			notificationManager?.setPlayer(mediaSession!!.player)
			startForeground(NOTIFICATION_ID, buildServiceNotification())
		} catch (_: Exception) {
			// Notification permission not granted or other restrictions; keep service alive without crashing
		}
	}

	private fun buildServiceNotification(): Notification {
		return NotificationCompat.Builder(this, CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_notification)
			.setContentTitle(getString(R.string.app_name))
			.setOngoing(true)
			.build()
	}

	private fun createChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			val channel = NotificationChannel(CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW)
			manager.createNotificationChannel(channel)
		}
	}

	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

	override fun onDestroy() {
		try { notificationManager?.setPlayer(null) } catch (_: Exception) {}
		notificationManager = null
		mediaSession?.release()
		mediaSession = null
		super.onDestroy()
	}

	companion object {
		private const val CHANNEL_ID = "playback"
		private const val NOTIFICATION_ID = 1001
	}
}
