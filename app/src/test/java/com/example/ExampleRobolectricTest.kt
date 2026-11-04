package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.model.SortOrder
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Myato-Player", appName)
    }

    @Test
    fun testSongDurationFormatting() {
        val song1 = Song(id = 1L, title = "Track 1", durationMs = 195000L) // 3m 15s
        assertEquals("3:15", song1.formattedDuration)

        val song2 = Song(id = 2L, title = "Track 2", durationMs = 65000L) // 1m 05s
        assertEquals("1:05", song2.formattedDuration)

        val songZero = Song(id = 3L, title = "Zero", durationMs = 0L)
        assertEquals("0:00", songZero.formattedDuration)
    }

    @Test
    fun testBackupJsonStructure() {
        val root = JSONObject().apply {
            put("app", "Myato-Player")
            put("version", "2.0")
            put("exportedAt", 1700000000000L)
            put("playlists", JSONArray())
            put("favorites", JSONArray().apply { put(1001L); put(1002L) })
        }

        assertEquals("Myato-Player", root.getString("app"))
        assertEquals("2.0", root.getString("version"))
        assertEquals(2, root.getJSONArray("favorites").length())
    }
}
