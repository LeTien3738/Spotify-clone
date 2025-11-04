package com.example.spotifyclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotifyclone.navigation.AppNavHost
import com.example.spotifyclone.navigation.AppRoute
import com.example.spotifyclone.player.FavoritesRepository
import com.example.spotifyclone.player.PlayerController
import com.example.spotifyclone.player.Track
import com.example.spotifyclone.ui.components.BottomBar
import com.example.spotifyclone.ui.components.MiniPlayer
import com.example.spotifyclone.ui.theme.SpotifyCloneTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent { App() }
	}
}

@Composable
fun App() {
	var useDarkTheme by rememberSaveable { mutableStateOf(true) }
	SpotifyCloneTheme(useDarkTheme = useDarkTheme) {
		val navController = rememberNavController()
		val controller = remember { PlayerController(appContext = navController.context.applicationContext) }
		FavoritesRepository.initialize(navController.context.applicationContext)
		val navBackStackEntry by navController.currentBackStackEntryAsState()
		val currentRoute = navBackStackEntry?.destination?.route.orEmpty()
		val isOnPlayer = currentRoute.startsWith("player/") || currentRoute == AppRoute.Player.route
		Scaffold(
			bottomBar = { BottomBar(navController) }
		) { padding ->
			Column(Modifier.padding(padding)) {
				if (!isOnPlayer) {
					MiniPlayer(controller = controller) { navController.navigate(AppRoute.Player.build("current")) }
				}
				AppNavHost(
					navController = navController,
					onPlaySample = { url, title -> controller.play(url, title) },
					onPlayQueue = { items, startIndex, artworkAt ->
						val tracks = items.mapIndexed { i, pair -> Track(url = pair.first, title = pair.second, artworkUrl = artworkAt(i)) }
						controller.playQueue(tracks, startIndex)
					},
					onToggleTheme = { useDarkTheme = !useDarkTheme },
					controller = controller
				)
			}
		}
	}
}
