package com.neonmusic.player.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.neonmusic.player.data.repository.MusicRepository
import com.neonmusic.player.domain.model.*
import com.neonmusic.player.playback.MusicPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = 0 // 0=off, 1=one, 2=all
)

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var positionJob: Job? = null
    private var queue: List<Song> = emptyList()
    private var queueIndex: Int = -1

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startPositionUpdates() else stopPositionUpdates()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val songId = mediaItem?.mediaId?.toLongOrNull()
            val song = queue.find { it.id == songId } ?: _uiState.value.songs.find { it.id == songId }
            _uiState.update {
                it.copy(
                    currentSong = song,
                    duration = mediaController?.duration?.coerceAtLeast(0) ?: (song?.duration ?: 0L),
                    currentPosition = 0L
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                val dur = mediaController?.duration ?: 0L
                if (dur > 0) _uiState.update { it.copy(duration = dur) }
            }
            if (playbackState == Player.STATE_ENDED) {
                when (_uiState.value.repeatMode) {
                    1 -> mediaController?.seekTo(0)?.also { mediaController?.play() }
                    else -> playNext()
                }
            }
        }
    }

    init {
        loadSongs()
        connectController()
    }

    fun loadSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.getLocalSongs().collect { songs ->
                    val albums = songs.groupBy { it.album to it.artist }.map { (k, list) ->
                        Album(k.first.ifBlank { "Bilinmeyen Albüm" }, k.second.ifBlank { "Bilinmeyen Sanatçı" },
                            list.size, list.firstOrNull()?.albumArtUri, list.sortedBy { it.trackNumber })
                    }.sortedBy { it.name.lowercase() }

                    val artists = songs.groupBy { it.artist.ifBlank { "Bilinmeyen Sanatçı" } }.map { (n, list) ->
                        Artist(n, list.size, list.map { it.album }.distinct().size, list.sortedBy { it.title.lowercase() })
                    }.sortedBy { it.name.lowercase() }

                    val folders = songs.groupBy { it.folder.ifBlank { "Music" } }.map { (n, list) ->
                        Folder(n, n, list.size, list.sortedBy { it.title.lowercase() })
                    }.sortedBy { it.name.lowercase() }

                    val genres = songs.groupBy { it.genre.ifBlank { "Unknown" } }.map { (n, list) ->
                        Genre(n, list.size, list.sortedBy { it.title.lowercase() })
                    }.sortedBy { it.name.lowercase() }

                    _uiState.update {
                        it.copy(songs = songs, albums = albums, artists = artists,
                            folders = folders, genres = genres, isLoading = false, error = null)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Yüklenemedi") }
            }
        }
    }

    private fun connectController() {
        val token = SessionToken(context, ComponentName(context, MusicPlayerService::class.java))
        controllerFuture = MediaController.Builder(context, token).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()?.also { it.addListener(playerListener) }
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "Oynatıcı bağlanamadı") }
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (isActive) {
                val pos = mediaController?.currentPosition ?: 0L
                val dur = mediaController?.duration?.coerceAtLeast(0) ?: _uiState.value.duration
                _uiState.update { it.copy(currentPosition = pos, duration = if (dur > 0) dur else it.duration) }
                delay(200) // 5 fps position – smooth enough, light on battery
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    fun playSong(song: Song, queueSongs: List<Song>? = null) {
        val list = queueSongs ?: _uiState.value.songs
        queue = list
        queueIndex = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playAtIndex(queueIndex)
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        queue = songs
        queueIndex = startIndex.coerceIn(0, songs.lastIndex)
        playAtIndex(queueIndex)
    }

    private fun playAtIndex(index: Int) {
        if (index !in queue.indices) return
        val song = queue[index]
        val controller = mediaController ?: return
        val item = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .build()
            ).build()
        controller.setMediaItem(item)
        controller.prepare()
        controller.play()
        _uiState.update { it.copy(currentSong = song, isPlaying = true, duration = song.duration, currentPosition = 0L) }
    }

    fun togglePlayPause() {
        val c = mediaController ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun playNext() {
        if (queue.isEmpty()) return
        val next = if (_uiState.value.shuffle) {
            queue.indices.filter { it != queueIndex }.randomOrNull() ?: return
        } else {
            (queueIndex + 1).takeIf { it < queue.size } ?: return
        }
        queueIndex = next
        playAtIndex(queueIndex)
    }

    fun playPrevious() {
        if (queue.isEmpty()) return
        val pos = mediaController?.currentPosition ?: 0L
        if (pos > 3000) {
            mediaController?.seekTo(0)
            return
        }
        val prev = (queueIndex - 1).coerceAtLeast(0)
        queueIndex = prev
        playAtIndex(queueIndex)
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _uiState.update { it.copy(currentPosition = position) }
    }

    fun toggleShuffle() {
        _uiState.update { it.copy(shuffle = !it.shuffle) }
    }

    fun cycleRepeat() {
        _uiState.update { it.copy(repeatMode = (it.repeatMode + 1) % 3) }
    }

    override fun onCleared() {
        stopPositionUpdates()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        super.onCleared()
    }
}
