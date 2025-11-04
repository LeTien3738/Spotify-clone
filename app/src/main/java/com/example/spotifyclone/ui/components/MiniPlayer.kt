package com.example.spotifyclone.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.spotifyclone.player.PlayerController

@Composable
fun MiniPlayer(
	controller: PlayerController,
	onOpenPlayer: () -> Unit
) {
	val ui = controller.uiState.collectAsState().value
	if (ui.title.isEmpty()) return
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onOpenPlayer() }
	) {
		Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Image(
					painter = rememberAsyncImagePainter(ui.artworkUrl.ifEmpty { "https://picsum.photos/seed/miniplayer/200" }),
					contentDescription = null,
					modifier = Modifier.size(40.dp),
					contentScale = ContentScale.Crop
				)
				Spacer(Modifier.width(8.dp))
				Text(ui.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
				IconButton(onClick = { controller.previous() }) { Icon(Icons.Filled.SkipPrevious, contentDescription = null) }
				IconButton(onClick = { controller.togglePlayPause() }) {
					Icon(if (ui.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
				}
				IconButton(onClick = { controller.next() }) { Icon(Icons.Filled.SkipNext, contentDescription = null) }
			}
			Spacer(Modifier.height(4.dp))
			LinearProgressIndicator(progress = { ui.progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
		}
	}
}
