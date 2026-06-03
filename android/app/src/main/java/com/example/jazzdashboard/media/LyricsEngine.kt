package com.example.jazzdashboard.media
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object LyricsEngine {
    private const val TAG = "LyricsEngine"

    fun fetchLyrics(title: String, artist: String, onResult: (List<LyricLine>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val qTitle = URLEncoder.encode(title, "UTF-8")
                val qArtist = URLEncoder.encode(artist, "UTF-8")
                val url = URL("https://lrclib.net/api/search?track_name=$qTitle&artist_name=$qArtist")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "JazzDashboard/1.0")
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(response)
                    
                    var syncedLyricsText: String? = null
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        if (!obj.isNull("syncedLyrics")) {
                            syncedLyricsText = obj.getString("syncedLyrics")
                            break
                        }
                    }
                    
                    if (syncedLyricsText != null) {
                        val parsed = parseLrc(syncedLyricsText)
                        CoroutineScope(Dispatchers.Main).launch {
                            onResult(parsed)
                        }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch lyrics", e)
            }
            CoroutineScope(Dispatchers.Main).launch {
                onResult(emptyList())
            }
        }
    }

    private fun parseLrc(lrc: String): List<LyricLine> {
        val lines = lrc.split("\n")
        val result = mutableListOf<LyricLine>()
        // Format: [mm:ss.xx] Text
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
        
        for (line in lines) {
            val match = regex.find(line)
            if (match != null) {
                val min = match.groupValues[1].toLong()
                val sec = match.groupValues[2].toLong()
                val msStr = match.groupValues[3]
                // Handle .xx (10s of ms) vs .xxx (ms)
                val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                val text = match.groupValues[4].trim()
                
                val timeMs = (min * 60 * 1000) + (sec * 1000) + ms
                if (text.isNotEmpty()) {
                    result.add(LyricLine(timeMs, text))
                }
            }
        }
        return result.sortedBy { it.timeMs }
    }
}

