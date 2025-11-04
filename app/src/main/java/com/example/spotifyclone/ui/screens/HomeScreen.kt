package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlin.math.min
import java.util.Calendar

private data class HomeAlbum(val id: String, val title: String, val cover: String, val artist: String, val streamUrl: String)
private data class Shortcut(val id: String, val title: String, val cover: String)
private data class ContinueItem(val id: String, val title: String, val artist: String, val cover: String, val progress: Float, val streamUrl: String)

private val sectionRecent = listOf(
	HomeAlbum("r1", "Daily Mix 1", "https://picsum.photos/seed/recent_1/300", "Various Artists", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
	HomeAlbum("r2", "Lo-Fi Beats", "https://picsum.photos/seed/recent_2/300", "Lo-Fi", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"),
	HomeAlbum("r3", "Coding Mode", "https://picsum.photos/seed/recent_3/300", "Various Artists", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"),
	HomeAlbum("r4", "RapCaviar", "https://picsum.photos/seed/recent_4/300", "Various Artists", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3")
)

private val sectionMadeForYou = listOf(
	HomeAlbum("m1", "Focus Flow", "https://picsum.photos/seed/mfy_1/300", "Various Artists", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"),
	HomeAlbum("m2", "Deep Focus", "https://picsum.photos/seed/mfy_2/300", "Various Artists", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"),
	HomeAlbum("m3", "Chill Hits", "https://picsum.photos/seed/mfy_3/300", "Various Artists", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3")
)

private val sectionTrending = listOf(
	HomeAlbum("t1", "Top 50 Global", "https://picsum.photos/seed/top_1/300", "Charts", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"),
	HomeAlbum("t2", "Hot Hits", "https://picsum.photos/seed/top_2/300", "Charts", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3"),
	HomeAlbum("t3", "Viral 50", "https://picsum.photos/seed/top_3/300", "Charts", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3")
)

private val shortcuts = listOf(
	Shortcut("s1", "Liked Songs", "https://picsum.photos/seed/short_1/200"),
	Shortcut("s2", "Daily Mix 1", "https://picsum.photos/seed/short_2/200"),
	Shortcut("s3", "Lo-Fi Beats", "https://picsum.photos/seed/short_3/200"),
	Shortcut("s4", "Coding Mode", "https://picsum.photos/seed/short_4/200"),
	Shortcut("s5", "Jazz Vibes", "https://picsum.photos/seed/short_5/200"),
	Shortcut("s6", "Release Radar", "https://picsum.photos/seed/short_6/200")
)

private val continueItems = listOf(
	ContinueItem("c1", "Daily Mix 1", "Various Artists", "https://picsum.photos/seed/continue_1/300", 0.35f, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
	ContinueItem("c2", "Lo-Fi Beats", "Lo-Fi", "https://picsum.photos/seed/continue_2/300", 0.62f, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"),
	ContinueItem("c3", "Coding Mode", "Various Artists", "https://picsum.photos/seed/continue_3/300", 0.12f, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3")
)

@Composable
fun HomeScreen(
	onOpenPlayer: (String) -> Unit,
	onPlaySample: (url: String, title: String) -> Unit = { _, _ -> },
	onPlayQueue: (items: List<Pair<String, String /*title*/>>, startIndex: Int, artworkAt: (Int) -> String) -> Unit = { _, _, _ -> },
	onToggleTheme: () -> Unit = {}
) {
	val listState = rememberLazyListState()
	val collapseRangePx = 140f
	val offset = min(collapseRangePx, listState.firstVisibleItemScrollOffset.toFloat())
	val collapseFraction = 1f - (offset / collapseRangePx)
	LazyColumn(
		state = listState,
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(bottom = 16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		item { GreetingHeader(collapseFraction = collapseFraction, avatarUrl = "https://i.pravatar.cc/150?img=12", onToggleTheme = onToggleTheme) }
		item { ShortcutsGrid(onClick = { sc ->
			val pairs = sectionRecent.map { it.streamUrl to it.title }
			onPlayQueue(pairs, 0) { i -> sectionRecent[i].cover }
			onOpenPlayer("recent")
		}) }

		item { ContinueListeningSection(items = continueItems, onOpenPlayer = onOpenPlayer, onPlayQueue = onPlayQueue) }
		item { Section(title = "Recently played", items = sectionRecent, onOpenPlayer = onOpenPlayer, onPlayQueue = onPlayQueue) }
		item { Section(title = "Made for you", items = sectionMadeForYou, onOpenPlayer = onOpenPlayer, onPlayQueue = onPlayQueue) }
		item { Section(title = "Trending now", items = sectionTrending, onOpenPlayer = onOpenPlayer, onPlayQueue = onPlayQueue) }
	}
}

private fun greetingText(): String {
	val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
	return when (hour) {
		in 5..11 -> "Good morning"
		in 12..17 -> "Good afternoon"
		else -> "Good evening"
	}
}

@Composable
private fun GreetingHeader(collapseFraction: Float, avatarUrl: String, onToggleTheme: () -> Unit) {
	val paddingVertical = (8 + 16 * collapseFraction).dp
	val titleStyle = MaterialTheme.typography.headlineSmall
	Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = paddingVertical), verticalAlignment = Alignment.CenterVertically) {
		Column(Modifier.weight(1f)) {
			Text(greetingText(), style = titleStyle, fontWeight = FontWeight.Bold)
			Spacer(Modifier.height(6.dp))
			Text("Enjoy your music", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
		}
		Row(verticalAlignment = Alignment.CenterVertically) {
			IconButton(onClick = onToggleTheme) { Icon(Icons.Filled.LightMode, contentDescription = null) }
			Image(
				painter = rememberAsyncImagePainter(avatarUrl),
				contentDescription = "Avatar",
				modifier = Modifier.size((40 + 8 * collapseFraction).dp).clip(MaterialTheme.shapes.large),
				contentScale = ContentScale.Crop
			)
		}
	}
}

@Composable
private fun ShortcutsGrid(onClick: (Shortcut) -> Unit) {
	// Bound the grid height to avoid infinite constraints inside LazyColumn
	val rows = (shortcuts.size + 1) / 2
	val gridHeight = (rows * 72).dp
	LazyVerticalGrid(
		columns = GridCells.Fixed(2),
		modifier = Modifier.fillMaxWidth().height(gridHeight),
		contentPadding = PaddingValues(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		gridItems(shortcuts) { sc ->
			Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.clickable { onClick(sc) }) {
				Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
					Image(
						painter = rememberAsyncImagePainter(sc.cover),
						contentDescription = sc.title,
						modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.small),
						contentScale = ContentScale.Crop
					)
					Spacer(Modifier.width(8.dp))
					Text(sc.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
				}
			}
		}
	}
}

@Composable
private fun ContinueListeningSection(
	items: List<ContinueItem>,
	onOpenPlayer: (String) -> Unit,
	onPlayQueue: (items: List<Pair<String, String>>, startIndex: Int, artworkAt: (Int) -> String) -> Unit
) {
	Column(Modifier.fillMaxWidth()) {
		Text("Continue listening", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp))
		Spacer(Modifier.height(8.dp))
		LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			itemsIndexed(items) { index, item ->
				Card(onClick = {
					val pairs = items.map { it.streamUrl to it.title }
					onPlayQueue(pairs, index) { i -> items[i].cover }
					onOpenPlayer(item.id)
				}, modifier = Modifier.width(220.dp)) {
					Column(Modifier.fillMaxWidth().padding(8.dp)) {
						Row(verticalAlignment = Alignment.CenterVertically) {
							Image(
								painter = rememberAsyncImagePainter(item.cover),
								contentDescription = item.title,
								modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.medium),
								contentScale = ContentScale.Crop
							)
							Spacer(Modifier.width(10.dp))
							Column(Modifier.weight(1f)) {
								Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
								Spacer(Modifier.height(2.dp))
								Text(item.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
							}
						}
						Spacer(Modifier.height(8.dp))
						LinearProgressIndicator(progress = { item.progress }, modifier = Modifier.fillMaxWidth())
					}
				}
			}
		}
	}
}

@Composable
private fun Section(
	title: String,
	items: List<HomeAlbum>,
	onOpenPlayer: (String) -> Unit,
	onPlayQueue: (items: List<Pair<String, String>>, startIndex: Int, artworkAt: (Int) -> String) -> Unit
) {
	Column(Modifier.fillMaxWidth()) {
		Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp))
		Spacer(Modifier.height(8.dp))
		LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			itemsIndexed(items) { index, album ->
				AlbumCard(album = album, onClick = {
					val pairs = items.map { it.streamUrl to it.title }
					onPlayQueue(pairs, index) { i -> items[i].cover }
					onOpenPlayer(album.id)
				})
			}
		}
	}
}

@Composable
private fun AlbumCard(album: HomeAlbum, onClick: () -> Unit) {
	Card(onClick = onClick, modifier = Modifier.width(140.dp)) {
		Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.Start) {
			Image(
				painter = rememberAsyncImagePainter(album.cover),
				contentDescription = album.title,
				modifier = Modifier.size(140.dp).clip(MaterialTheme.shapes.medium),
				contentScale = ContentScale.Crop
			)
			Spacer(Modifier.height(8.dp))
			Text(album.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
			Spacer(Modifier.height(2.dp))
			Text(album.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
		}
	}
}
