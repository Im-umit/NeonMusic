package com.neonmusic.player.domain.model

data class Album(
    val name: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: String?,
    val songs: List<Song>
)
