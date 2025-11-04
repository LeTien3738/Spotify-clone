package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Category(val id: String, val name: String, val color: Color)

private val mockCategories = listOf(
	Category("c1", "Podcasts", Color(0xFF27856A)),
	Category("c2", "Made for You", Color(0xFF1E3264)),
	Category("c3", "Charts", Color(0xFFE91429)),
	Category("c4", "New Releases", Color(0xFF8D67AB)),
	Category("c5", "Discover", Color(0xFFBA5D07)),
	Category("c6", "Hip-Hop", Color(0xFFAF2896)),
	Category("c7", "Pop", Color(0xFF0D73EC)),
	Category("c8", "Rock", Color(0xFF8C8C8C)),
	Category("c9", "Jazz", Color(0xFF1DB954)),
	Category("c10", "Classical", Color(0xFFB49BC8))
)

@Composable
fun SearchScreen(onOpenPlayer: (String) -> Unit, onPlaySample: (url: String, title: String) -> Unit = { _, _ -> }) {
	var query by remember { mutableStateOf("") }
	val filtered = remember(query) {
		if (query.isBlank()) mockCategories
		else mockCategories.filter { it.name.contains(query, ignoreCase = true) }
	}
	Column(Modifier.fillMaxSize().padding(16.dp)) {
		Text("Search", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
		Spacer(Modifier.height(12.dp))
		TextField(
			value = query,
			onValueChange = { query = it },
			modifier = Modifier.fillMaxWidth(),
			placeholder = { Text("Tìm kiếm theo thể loại") }
		)
		Spacer(Modifier.height(16.dp))
		LazyVerticalGrid(columns = GridCells.Adaptive(140.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
			items(filtered) { item ->
				Surface(tonalElevation = 2.dp, modifier = Modifier.height(100.dp).clickable {
					// Play a sample when tapping a category, then open player
					onPlaySample("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", item.name)
					onOpenPlayer(item.id)
				}) {
					Box(Modifier.fillMaxSize().background(item.color)) {
						Text(item.name, color = Color.White, modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), style = MaterialTheme.typography.titleMedium)
					}
				}
			}
		}
	}
}
