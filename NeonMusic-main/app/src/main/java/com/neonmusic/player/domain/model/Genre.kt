package com.neonmusic.player.domain.model

data class Genre(
    val name: String,
    val songCount: Int,
    val songs: List<Song>
)
