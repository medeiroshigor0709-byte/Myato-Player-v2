package com.example

import com.example.model.RepeatMode
import com.example.model.Song
import com.example.model.SortOrder
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMultiSelectionInversion() {
        val songs = listOf(
            Song(id = 1L, title = "A"),
            Song(id = 2L, title = "B"),
            Song(id = 3L, title = "C"),
            Song(id = 4L, title = "D")
        )
        val selectedIds = setOf(1L, 3L)
        val inverted = songs.map { it.id }.filterNot { selectedIds.contains(it) }.toSet()

        assertEquals(setOf(2L, 4L), inverted)
    }

    @Test
    fun testMultiSelectionRange() {
        val songs = listOf(
            Song(id = 10L, title = "A"),
            Song(id = 20L, title = "B"),
            Song(id = 30L, title = "C"),
            Song(id = 40L, title = "D")
        )
        val firstId = 10L
        val lastId = 30L

        val idx1 = songs.indexOfFirst { it.id == firstId }
        val idx2 = songs.indexOfFirst { it.id == lastId }
        val range = songs.subList(minOf(idx1, idx2), maxOf(idx1, idx2) + 1).map { it.id }.toSet()

        assertEquals(setOf(10L, 20L, 30L), range)
    }

    @Test
    fun testRepeatModeTransitions() {
        var mode = RepeatMode.OFF
        mode = when (mode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        assertEquals(RepeatMode.ALL, mode)

        mode = when (mode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        assertEquals(RepeatMode.ONE, mode)

        mode = when (mode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        assertEquals(RepeatMode.OFF, mode)
    }

    @Test
    fun testSongSorting() {
        val songs = listOf(
            Song(id = 1L, title = "Zebra", durationMs = 50000L),
            Song(id = 2L, title = "Alpha", durationMs = 120000L),
            Song(id = 3L, title = "Beta", durationMs = 90000L)
        )

        val byNameAsc = songs.sortedBy { it.title.lowercase() }
        assertEquals("Alpha", byNameAsc[0].title)
        assertEquals("Beta", byNameAsc[1].title)
        assertEquals("Zebra", byNameAsc[2].title)

        val byDurationDesc = songs.sortedByDescending { it.durationMs }
        assertEquals(120000L, byDurationDesc[0].durationMs)
        assertEquals(90000L, byDurationDesc[1].durationMs)
        assertEquals(50000L, byDurationDesc[2].durationMs)
    }
}
