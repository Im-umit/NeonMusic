package com.neonmusic.player.data.repository

import com.neonmusic.player.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getLocalSongs(): Flow<List<Song>>
}
