package com.neonmusic.player.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val albumArtUri: String? = null,
    val genre: String = "Unknown",
    val year: Int = 0,
    val trackNumber: Int = 0,
    val folder: String = ""
)
