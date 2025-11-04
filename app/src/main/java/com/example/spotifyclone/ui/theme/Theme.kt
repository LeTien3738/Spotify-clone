package com.example.spotifyclone.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
	primary = SpotifyGreen,
	background = SpotifyBlack,
	surface = SpotifyDarkGray,
	onPrimary = Color.White,
	onBackground = Color.White,
	onSurface = Color.White
)

private val LightColors = lightColorScheme(
	primary = SpotifyGreen,
	background = Color.White,
	surface = Color.White,
	onPrimary = Color.White,
	onBackground = Color.Black,
	onSurface = Color.Black
)

@Composable
fun SpotifyCloneTheme(
	useDarkTheme: Boolean = isSystemInDarkTheme(),
	dynamicColor: Boolean = true,
	content: @Composable () -> Unit
) {
	val context = LocalContext.current
	val colorScheme = when {
		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}
		useDarkTheme -> DarkColors
		else -> LightColors
	}

	// Set status bar color for better immersion
	val window = (context as? Activity)?.window
	window?.statusBarColor = colorScheme.background.toArgb()

	MaterialTheme(
		colorScheme = colorScheme,
		content = content
	)
}
