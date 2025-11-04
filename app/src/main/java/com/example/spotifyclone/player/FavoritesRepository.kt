package com.example.spotifyclone.player

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val Context.favoritesDataStore by preferencesDataStore(name = "favorites")

object FavoritesRepository {
	private const val KEY_JSON = "favorites_json"
	private val jsonKey = stringPreferencesKey(KEY_JSON)

	private val _favoriteUrls = MutableStateFlow<Set<String>>(emptySet())
	val favoriteUrls: StateFlow<Set<String>> = _favoriteUrls

	private val urlToTrack = LinkedHashMap<String, Track>()

	private var appContext: Context? = null
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	fun initialize(context: Context) {
		if (appContext != null) return
		appContext = context.applicationContext
		scope.launch { loadFromStore() }
	}

	fun isFavorite(url: String?): Boolean {
		if (url.isNullOrEmpty()) return false
		return _favoriteUrls.value.contains(url)
	}

	fun toggle(track: Track) {
		val url = track.url
		val current = _favoriteUrls.value
		if (current.contains(url)) {
			_favoriteUrls.value = current - url
			urlToTrack.remove(url)
		} else {
			_favoriteUrls.value = current + url
			urlToTrack[url] = track
		}
		scope.launch { saveToStore() }
	}

	fun allFavorites(): List<Track> {
		return _favoriteUrls.value.mapNotNull { urlToTrack[it] }
	}

	private suspend fun loadFromStore() {
		val ctx = appContext ?: return
		val prefs = ctx.favoritesDataStore.data.first()
		val json = prefs[jsonKey] ?: return
		try {
			val arr = JSONArray(json)
			urlToTrack.clear()
			val urls = LinkedHashSet<String>()
			for (i in 0 until arr.length()) {
				val obj = arr.getJSONObject(i)
				val url = obj.optString("url")
				val title = obj.optString("title")
				val artwork = obj.optString("artwork")
				if (url.isNotEmpty()) {
					urlToTrack[url] = Track(url = url, title = title, artworkUrl = artwork)
					urls.add(url)
				}
			}
			_favoriteUrls.value = urls
		} catch (_: Exception) {
			// ignore corrupt data
		}
	}

	private suspend fun saveToStore() {
		val ctx = appContext ?: return
		val arr = JSONArray()
		for ((_, track) in urlToTrack) {
			val obj = JSONObject()
			obj.put("url", track.url)
			obj.put("title", track.title)
			obj.put("artwork", track.artworkUrl)
			arr.put(obj)
		}
		ctx.favoritesDataStore.edit { prefs ->
			prefs[jsonKey] = arr.toString()
		}
	}
}
