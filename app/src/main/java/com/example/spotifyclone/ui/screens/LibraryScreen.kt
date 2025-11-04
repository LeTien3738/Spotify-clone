package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.spotifyclone.player.FavoritesRepository

private data class Playlist(val id: String, val title: String, val cover: String, val by: String, val streamUrl: String, val downloaded: Boolean)
private data class Artist(val id: String, val name: String, val photo: String, val downloaded: Boolean)
private data class Album(val id: String, val title: String, val cover: String, val artist: String, val streamUrl: String, val downloaded: Boolean)

enum class LibraryTab { Playlists, Artists, Albums }

enum class LibrarySort { AZ, RecentlyAdded }

private val mockPlaylists = listOf(
	Playlist("p2", "Coding Focus", "https://picsum.photos/seed/pl2/300", "Spotify", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", downloaded = false),
	Playlist("p3", "Lo-Fi Beats", "https://picsum.photos/seed/pl3/300", "Spotify", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", downloaded = true)
)

private val mockArtists = listOf(
	Artist("a1", "Drake", "https://picsum.photos/seed/ar1/300", downloaded = false),
	Artist("a2", "Adele", "https://picsum.photos/seed/ar2/300", downloaded = true),
	Artist("a3", "Eminem", "https://picsum.photos/seed/ar3/300", downloaded = false)
)

private val mockAlbums = listOf(
	Album("al1", "Top Hits 2024", "https://picsum.photos/seed/al1/300", "Various Artists", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", downloaded = false),
	Album("al2", "Focus Flow", "https://picsum.photos/seed/al2/300", "Various Artists", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", downloaded = true),
	Album("al3", "Jazz Vibes", "https://picsum.photos/seed/al3/300", "Various Artists", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3", downloaded = false)
)

@Composable
fun LibraryScreen(onOpenPlayer: (String) -> Unit, onPlayQueue: (items: List<Pair<String, String>>, startIndex: Int, artworkAt: (Int) -> String) -> Unit = { _, _, _ -> }) {
	var selectedTab by remember { mutableStateOf(LibraryTab.Playlists) }
	var query by remember { mutableStateOf("") }
	var grid by remember { mutableStateOf(false) }
	var sort by remember { mutableStateOf(LibrarySort.RecentlyAdded) }
	var downloadedOnly by remember { mutableStateOf(false) }
	var creatorOnly by remember { mutableStateOf(false) }
	var sortMenuExpanded by remember { mutableStateOf(false) }

	Column(Modifier.fillMaxSize()) {
		Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
			Text("Your Library", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
			IconButton(onClick = { grid = !grid }) { Icon(if (grid) Icons.Filled.List else Icons.Filled.GridView, contentDescription = null) }
		}

		TabRow(selectedTabIndex = selectedTab.ordinal) {
			Tab(selected = selectedTab == LibraryTab.Playlists, onClick = { selectedTab = LibraryTab.Playlists }, text = { Text("Playlists") })
			Tab(selected = selectedTab == LibraryTab.Artists, onClick = { selectedTab = LibraryTab.Artists }, text = { Text("Artists") })
			Tab(selected = selectedTab == LibraryTab.Albums, onClick = { selectedTab = LibraryTab.Albums }, text = { Text("Albums") })
		}

		Spacer(Modifier.height(8.dp))
		TextField(
			value = query,
			onValueChange = { query = it },
			modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
			placeholder = { Text("Tìm kiếm trong thư viện") }
		)

		Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			Button(onClick = { sortMenuExpanded = true }) { Text("Sort: ${if (sort == LibrarySort.AZ) "A–Z" else "Recently"}") }
			DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
				DropdownMenuItem(text = { Text("A–Z") }, onClick = { sort = LibrarySort.AZ; sortMenuExpanded = false })
				DropdownMenuItem(text = { Text("Recently added") }, onClick = { sort = LibrarySort.RecentlyAdded; sortMenuExpanded = false })
			}
			FilterChip(selected = downloadedOnly, onClick = { downloadedOnly = !downloadedOnly }, label = { Text("Downloaded") })
			if (selectedTab == LibraryTab.Playlists) {
				FilterChip(selected = creatorOnly, onClick = { creatorOnly = !creatorOnly }, label = { Text("Creator: You") })
			}
		}

		when (selectedTab) {
			LibraryTab.Playlists -> PlaylistsSection(query, grid, sort, downloadedOnly, creatorOnly, onOpenPlayer, onPlayQueue)
			LibraryTab.Artists -> ArtistsSection(query, grid, sort, downloadedOnly)
			LibraryTab.Albums -> AlbumsSection(query, grid, sort, downloadedOnly, onOpenPlayer, onPlayQueue)
		}
	}
}

private fun <T> applySort(list: List<T>, sort: LibrarySort, keySelector: (T) -> String): List<T> {
	return when (sort) {
		LibrarySort.AZ -> list.sortedBy { keySelector(it).lowercase() }
		LibrarySort.RecentlyAdded -> list
	}
}

@Composable
private fun PlaylistsSection(query: String, grid: Boolean, sort: LibrarySort, downloadedOnly: Boolean, creatorOnly: Boolean, onOpenPlayer: (String) -> Unit, onPlayQueue: (items: List<Pair<String, String>>, startIndex: Int, artworkAt: (Int) -> String) -> Unit) {
	val favTrigger = FavoritesRepository.favoriteUrls.collectAsState().value
	val favTracks = remember(favTrigger) { FavoritesRepository.allFavorites() }
	val likedCover = favTracks.firstOrNull()?.artworkUrl ?: "https://picsum.photos/seed/liked/300"
	val likedTitle = "Liked Songs (${favTracks.size})"
	val likedPlaylist = Playlist("liked", likedTitle, likedCover, "You", favTracks.firstOrNull()?.url ?: "", downloaded = true)

	var data = remember(query, favTrigger) {
		val base = listOf(likedPlaylist) + mockPlaylists
		if (query.isBlank()) base else base.filter { it.title.contains(query, true) || it.by.contains(query, true) }
	}
	if (downloadedOnly) data = data.filter { it.downloaded }
	if (creatorOnly) data = data.filter { it.by.equals("You", true) }
	data = applySort(data, sort) { it.title }

	val onPlaylistClick: (Playlist) -> Unit = { pl ->
		if (pl.id == "liked" && favTracks.isNotEmpty()) {
			val pairs = favTracks.map { it.url to it.title }
			onPlayQueue(pairs, 0) { i -> favTracks[i].artworkUrl }
			onOpenPlayer(pl.id)
		} else {
			val pairs = data.map { it.streamUrl to it.title }
			onPlayQueue(pairs, data.indexOf(pl)) { i -> data[i].cover }
			onOpenPlayer(pl.id)
		}
	}

	if (grid) {
		LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			items(data) { pl -> PlaylistGridItem(pl) { onPlaylistClick(pl) } }
		}
	} else {
		LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
			items(data) { pl -> PlaylistRowItem(pl) { onPlaylistClick(pl) } }
		}
	}
}

@Composable
private fun ArtistsSection(query: String, grid: Boolean, sort: LibrarySort, downloadedOnly: Boolean) {
	var data = remember(query) { if (query.isBlank()) mockArtists else mockArtists.filter { it.name.contains(query, true) } }
	if (downloadedOnly) data = data.filter { it.downloaded }
	data = applySort(data, sort) { it.name }

	if (grid) {
		LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			items(data) { ar -> ArtistGridItem(ar) }
		}
	} else {
		LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
			items(data) { ar -> ArtistRowItem(ar) }
		}
	}
}

@Composable
private fun AlbumsSection(query: String, grid: Boolean, sort: LibrarySort, downloadedOnly: Boolean, onOpenPlayer: (String) -> Unit, onPlayQueue: (items: List<Pair<String, String>>, startIndex: Int, artworkAt: (Int) -> String) -> Unit) {
	var data = remember(query) { if (query.isBlank()) mockAlbums else mockAlbums.filter { it.title.contains(query, true) || it.artist.contains(query, true) } }
	if (downloadedOnly) data = data.filter { it.downloaded }
	data = applySort(data, sort) { it.title }

	if (grid) {
		LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			items(data) { al -> AlbumGridItem(al) {
				val pairs = data.map { it.streamUrl to it.title }
				onPlayQueue(pairs, data.indexOf(al)) { i -> data[i].cover }
				onOpenPlayer(al.id)
			} }
		}
	} else {
		LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
			items(data) { al -> AlbumRowItem(al) {
				val pairs = data.map { it.streamUrl to it.title }
				onPlayQueue(pairs, data.indexOf(al)) { i -> data[i].cover }
				onOpenPlayer(al.id)
			} }
		}
	}
}

@Composable
private fun PlaylistRowItem(pl: Playlist, onClick: () -> Unit) {
	Row(Modifier.fillMaxWidth().clickable { onClick() }) {
		Image(
			painter = rememberAsyncImagePainter(pl.cover),
			contentDescription = pl.title,
			modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium),
			contentScale = ContentScale.Crop
		)
		Spacer(Modifier.width(12.dp))
		Column(Modifier.weight(1f)) {
			Text(pl.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
			Spacer(Modifier.height(4.dp))
			Text("Playlist • ${pl.by}${if (pl.downloaded) " • Downloaded" else ""}", style = MaterialTheme.typography.bodyMedium)
		}
	}
}

@Composable
private fun PlaylistGridItem(pl: Playlist, onClick: () -> Unit) {
	Card(onClick = onClick) {
		Column(Modifier.fillMaxWidth().padding(8.dp)) {
			Image(
				painter = rememberAsyncImagePainter(pl.cover),
				contentDescription = pl.title,
				modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.medium),
				contentScale = ContentScale.Crop
			)
			Spacer(Modifier.height(8.dp))
			Text(pl.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
			Spacer(Modifier.height(2.dp))
			Text("${pl.by}${if (pl.downloaded) " • Downloaded" else ""}", style = MaterialTheme.typography.bodySmall)
		}
	}
}

@Composable
private fun ArtistRowItem(ar: Artist) {
	Row(Modifier.fillMaxWidth()) {
		Image(
			painter = rememberAsyncImagePainter(ar.photo),
			contentDescription = ar.name,
			modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.large),
			contentScale = ContentScale.Crop
		)
		Spacer(Modifier.width(12.dp))
		Column(Modifier.weight(1f)) { Text(ar.name, style = MaterialTheme.typography.titleMedium) }
	}
}

@Composable
private fun ArtistGridItem(ar: Artist) {
	Card { Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
		Image(
			painter = rememberAsyncImagePainter(ar.photo),
			contentDescription = ar.name,
			modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.large),
			contentScale = ContentScale.Crop
		)
		Spacer(Modifier.height(8.dp))
		Text(ar.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
	} }
}

@Composable
private fun AlbumRowItem(al: Album, onClick: () -> Unit) {
	Row(Modifier.fillMaxWidth().clickable { onClick() }) {
		Image(
			painter = rememberAsyncImagePainter(al.cover),
			contentDescription = al.title,
			modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium),
			contentScale = ContentScale.Crop
		)
		Spacer(Modifier.width(12.dp))
		Column(Modifier.weight(1f)) {
			Text(al.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
			Spacer(Modifier.height(4.dp))
			Text("${al.artist}${if (al.downloaded) " • Downloaded" else ""}", style = MaterialTheme.typography.bodyMedium)
		}
	}
}

@Composable
private fun AlbumGridItem(al: Album, onClick: () -> Unit) {
	Card(onClick = onClick) {
		Column(Modifier.fillMaxWidth().padding(8.dp)) {
			Image(
				painter = rememberAsyncImagePainter(al.cover),
				contentDescription = al.title,
				modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.medium),
				contentScale = ContentScale.Crop
			)
			Spacer(Modifier.height(8.dp))
			Text(al.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
			Spacer(Modifier.height(2.dp))
			Text("${al.artist}${if (al.downloaded) " • Downloaded" else ""}", style = MaterialTheme.typography.bodySmall)
		}
	}
}
