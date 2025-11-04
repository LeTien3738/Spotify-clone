package com.example.spotifyclone.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spotifyclone.player.PlayerController
import com.example.spotifyclone.ui.screens.HomeScreen
import com.example.spotifyclone.ui.screens.LibraryScreen
import com.example.spotifyclone.ui.screens.PlayerScreen
import com.example.spotifyclone.ui.screens.SearchScreen

sealed class AppRoute(val route: String) {
	data object Home : AppRoute("home")
	data object Search : AppRoute("search")
	data object Library : AppRoute("library")
	data object Player : AppRoute("player/{trackId}") {
		fun build(trackId: String) = "player/$trackId"
	}
}

@Composable
fun AppNavHost(
	navController: NavHostController = rememberNavController(),
	onPlaySample: (url: String, title: String) -> Unit = { _, _ -> },
	onPlayQueue: (items: List<Pair<String, String>>, startIndex: Int, artworkAt: (Int) -> String) -> Unit = { _, _, _ -> },
	onToggleTheme: () -> Unit = {},
	controller: PlayerController? = null
) {
	NavHost(navController = navController, startDestination = AppRoute.Home.route) {
		composable(AppRoute.Home.route) {
			HomeScreen(
				onOpenPlayer = { id -> navController.navigate(AppRoute.Player.build(id)) },
				onPlaySample = onPlaySample,
				onPlayQueue = onPlayQueue,
				onToggleTheme = onToggleTheme
			)
		}
		composable(AppRoute.Search.route) {
			SearchScreen(
				onOpenPlayer = { id -> navController.navigate(AppRoute.Player.build(id)) },
				onPlaySample = onPlaySample
			)
		}
		composable(AppRoute.Library.route) {
			LibraryScreen(
				onOpenPlayer = { id -> navController.navigate(AppRoute.Player.build(id)) },
				onPlayQueue = onPlayQueue
			)
		}
		composable(AppRoute.Player.route) { backStackEntry ->
			val id = backStackEntry.arguments?.getString("trackId").orEmpty()
			PlayerScreen(trackId = id, controller = controller)
		}
	}
}
