package com.example.spotifyclone.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.spotifyclone.R
import com.example.spotifyclone.navigation.AppRoute
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LibraryMusic

private data class BottomItem(
	val route: String,
	val labelRes: Int,
	val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun BottomBar(navController: NavHostController) {
	val items = listOf(
		BottomItem(AppRoute.Home.route, R.string.nav_home, Icons.Filled.Home),
		BottomItem(AppRoute.Search.route, R.string.nav_search, Icons.Filled.Search),
		BottomItem(AppRoute.Library.route, R.string.nav_library, Icons.Filled.LibraryMusic)
	)

	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentDestination = navBackStackEntry?.destination

	NavigationBar {
		items.forEach { item ->
			val selected = currentDestination.isTopLevelDestination(item.route)
			NavigationBarItem(
				selected = selected,
				onClick = {
					// If tapping the same tab, pop back to its root route
					val popped = navController.popBackStack(item.route, false)
					if (!popped) {
						navController.navigate(item.route) {
							popUpTo(navController.graph.startDestinationId) { saveState = true }
							launchSingleTop = true
							restoreState = true
						}
					}
				},
				icon = { Icon(item.icon, contentDescription = null) },
				label = { Text(text = stringResource(id = item.labelRes)) }
			)
		}
	}
}

private fun NavDestination?.isTopLevelDestination(route: String): Boolean {
	return this?.hierarchy?.any { it.route == route } == true
}
