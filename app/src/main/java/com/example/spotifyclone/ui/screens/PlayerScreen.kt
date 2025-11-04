package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.spotifyclone.player.FavoritesRepository
import com.example.spotifyclone.player.PlayerController

@Composable
fun PlayerScreen(trackId: String, controller: PlayerController? = null) {
	val ui = controller?.uiState?.collectAsState()?.value
	var shuffle by remember { mutableStateOf(false) }
	var repeat by remember { mutableStateOf(false) }
	val currentTrack = controller?.getCurrentTrack()
	val favorites = FavoritesRepository.favoriteUrls.collectAsState().value
	val isFav = FavoritesRepository.isFavorite(currentTrack?.url)

	Box(Modifier.fillMaxSize()) {
		Image(
			painter = rememberAsyncImagePainter(ui?.artworkUrl?.ifEmpty { "https://picsum.photos/seed/player_$trackId/800" } ?: "https://picsum.photos/seed/player_$trackId/800"),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize().alpha(0.35f)
		)
		Box(
			Modifier.fillMaxSize().background(
				Brush.verticalGradient(
					colors = listOf(
						Color.Black.copy(alpha = 0.5f),
						Color.Transparent,
						MaterialTheme.colorScheme.background
					)
				)
			)
		)

		Column(
			modifier = Modifier.fillMaxSize().padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.SpaceBetween
		) {
			Spacer(Modifier.height(8.dp))
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Image(
					painter = rememberAsyncImagePainter(ui?.artworkUrl?.ifEmpty { "https://picsum.photos/seed/player_$trackId/800" } ?: "https://picsum.photos/seed/player_$trackId/800"),
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier.size(320.dp)
				)
				Spacer(Modifier.height(24.dp))
				Text(ui?.title ?: "Playing track #$trackId", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
				Spacer(Modifier.height(8.dp))
				Text("Artist • Album", style = MaterialTheme.typography.bodyMedium)
			}

			Column(Modifier.fillMaxWidth()) {
				val pos = ui?.positionMs ?: 0L
				val dur = ui?.durationMs ?: 0L
				val progress = if (dur > 0) pos.toFloat() / dur.toFloat() else 0f
				Slider(
					value = progress,
					onValueChange = { controller?.seekTo(it) }
				)
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
					Text(formatTime(pos), style = MaterialTheme.typography.bodySmall)
					Text(formatTime(dur), style = MaterialTheme.typography.bodySmall)
				}
				Spacer(Modifier.height(8.dp))
				Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
					IconButton(onClick = { shuffle = !shuffle }) { Icon(Icons.Filled.Shuffle, contentDescription = null, tint = if (shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
					IconButton(onClick = { controller?.previous() }) { Icon(Icons.Filled.FastRewind, contentDescription = null) }
					IconButton(onClick = { controller?.togglePlayPause() }) { Icon(if (ui?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null) }
					IconButton(onClick = { controller?.next() }) { Icon(Icons.Filled.FastForward, contentDescription = null) }
					IconButton(onClick = { repeat = !repeat }) { Icon(Icons.Filled.Repeat, contentDescription = null, tint = if (repeat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
				}
				Spacer(Modifier.height(8.dp))
				Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
					IconButton(onClick = {
						currentTrack?.let { FavoritesRepository.toggle(it) }
					}) { Icon(if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = null, tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
					Spacer(Modifier.width(1.dp))
				}
			}
		}
	}
}

private fun formatTime(ms: Long): String {
	if (ms <= 0L) return "0:00"
	val totalSeconds = ms / 1000
	val minutes = totalSeconds / 60
	val seconds = totalSeconds % 60
	return "$minutes:${seconds.toString().padStart(2, '0')}"
}
