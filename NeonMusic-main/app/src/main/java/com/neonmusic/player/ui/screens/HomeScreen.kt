package com.neonmusic.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neonmusic.player.domain.model.*
import com.neonmusic.player.ui.components.*
import com.neonmusic.player.ui.theme.NeonBackground
import com.neonmusic.player.ui.theme.NeonPrimary
import com.neonmusic.player.ui.theme.NeonSurface
import com.neonmusic.player.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

private enum class LibraryTab(val title: String) {
    Songs("Şarkılar"),
    Playlists("Listeler"),
    Folders("Klasörler"),
    Albums("Albümler"),
    Artists("Sanatçılar"),
    Genres("Türler")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(LibraryTab.Songs) }
    var showFullPlayer by remember { mutableStateOf(false) }
    var detailSongs by remember { mutableStateOf<List<Song>?>(null) }
    var detailTitle by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (showFullPlayer && state.currentSong != null) {
        FullPlayerScreen(
            song = state.currentSong!!,
            isPlaying = state.isPlaying,
            position = state.currentPosition,
            duration = state.duration,
            shuffle = state.shuffle,
            repeatMode = state.repeatMode,
            onDismiss = { showFullPlayer = false },
            onPlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.playNext() },
            onPrevious = { viewModel.playPrevious() },
            onSeek = { viewModel.seekTo(it) },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onCycleRepeat = { viewModel.cycleRepeat() }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = NeonBackground) {
                Spacer(Modifier.height(24.dp))
                Text("NeonMusic", fontWeight = FontWeight.Bold, fontSize = 22.sp,
                    color = NeonPrimary, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(20.dp))
                DrawerItem(Icons.Default.LibraryMusic, "Kütüphane") { scope.launch { drawerState.close() } }
                DrawerItem(Icons.Default.Equalizer, "Ekolayzer") { }
                DrawerItem(Icons.Default.Timer, "Uyku Zamanlayıcı") { }
                DrawerItem(Icons.Default.Palette, "Tema") { }
                DrawerItem(Icons.Default.Settings, "Ayarlar") { }
                DrawerItem(Icons.Default.Info, "Hakkında") { }
            }
        }
    ) {
        Scaffold(
            containerColor = NeonBackground,
            topBar = {
                if (detailSongs == null) {
                    Column {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LibraryMusic, null, tint = NeonPrimary, modifier = Modifier.size(26.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("NeonMusic", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NeonPrimary)
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, null, tint = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            actions = {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonBackground)
                        )
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab.ordinal,
                            containerColor = NeonBackground,
                            contentColor = NeonPrimary,
                            edgePadding = 8.dp
                        ) {
                            LibraryTab.entries.forEach { tab ->
                                Tab(
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    text = {
                                        Text(tab.title, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    TopAppBar(
                        title = { Text(detailTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        navigationIcon = {
                            IconButton(onClick = { detailSongs = null }) {
                                Icon(Icons.Default.ArrowBack, null, tint = NeonPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonBackground)
                    )
                }
            },
            bottomBar = {
                PlayerBar(
                    song = state.currentSong,
                    isPlaying = state.isPlaying,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNext() },
                    onPrevious = { viewModel.playPrevious() },
                    onExpand = { showFullPlayer = true }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).background(NeonBackground)) {
                when {
                    !hasPermission -> PermissionBox(onRequestPermission)
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = NeonPrimary)
                    state.error != null -> Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                    detailSongs != null -> {
                        SongList(
                            songs = detailSongs!!,
                            currentId = state.currentSong?.id,
                            isPlaying = state.isPlaying,
                            onClick = { viewModel.playSong(it, detailSongs) },
                            header = "${detailSongs!!.size} şarkı"
                        )
                    }
                    else -> when (selectedTab) {
                        LibraryTab.Songs -> SongsContent(state.songs, state.currentSong?.id, state.isPlaying,
                            onSong = { viewModel.playSong(it) },
                            onShuffle = { if (state.songs.isNotEmpty()) viewModel.playSong(state.songs.random(), state.songs) },
                            onPlayAll = { if (state.songs.isNotEmpty()) viewModel.playSongs(state.songs, 0) })
                        LibraryTab.Albums -> AlbumsContent(state.albums) { album ->
                            detailTitle = album.name
                            detailSongs = album.songs
                        }
                        LibraryTab.Artists -> ArtistsContent(state.artists) { artist ->
                            detailTitle = artist.name
                            detailSongs = artist.songs
                        }
                        LibraryTab.Folders -> FoldersContent(state.folders) { folder ->
                            detailTitle = folder.name
                            detailSongs = folder.songs
                        }
                        LibraryTab.Genres -> GenresContent(state.genres) { genre ->
                            detailTitle = genre.name
                            detailSongs = genre.songs
                        }
                        LibraryTab.Playlists -> PlaylistsPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = NeonPrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SongsContent(
    songs: List<Song>, currentId: Long?, isPlaying: Boolean,
    onSong: (Song) -> Unit, onShuffle: () -> Unit, onPlayAll: () -> Unit
) {
    if (songs.isEmpty()) { Empty("Şarkı yok"); return }
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onShuffle, colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPrimary)) {
                Icon(Icons.Default.Shuffle, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Karıştır")
            }
            Button(onClick = onPlayAll, colors = ButtonDefaults.buttonColors(containerColor = NeonPrimary)) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp), tint = androidx.compose.ui.graphics.Color.Black)
                Spacer(Modifier.width(6.dp)); Text("Oynat", color = androidx.compose.ui.graphics.Color.Black)
            }
            Spacer(Modifier.weight(1f))
            Text("${songs.size} şarkı", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterVertically))
        }
        SongList(songs, currentId, isPlaying, onSong, null)
    }
}

@Composable
private fun AlbumsContent(albums: List<Album>, onClick: (Album) -> Unit) {
    if (albums.isEmpty()) { Empty("Albüm yok"); return }
    Column {
        Text("${albums.size} albüm", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        LazyVerticalGrid(GridCells.Adaptive(150.dp), contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(albums, key = { "${it.name}_${it.artist}" }) { AlbumItem(it) { onClick(it) } }
        }
    }
}

@Composable
private fun ArtistsContent(artists: List<Artist>, onClick: (Artist) -> Unit) {
    if (artists.isEmpty()) { Empty("Sanatçı yok"); return }
    Column {
        Text("${artists.size} sanatçı", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(artists, key = { it.name }) { ArtistItem(it) { onClick(it) } }
        }
    }
}

@Composable
private fun FoldersContent(folders: List<Folder>, onClick: (Folder) -> Unit) {
    if (folders.isEmpty()) { Empty("Klasör yok"); return }
    Column {
        Text("${folders.size} klasör", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(folders, key = { it.name }) { folder ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(NeonSurface)
                        .clickable { onClick(folder) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, null, tint = NeonPrimary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(folder.name, style = MaterialTheme.typography.bodyLarge)
                        Text("${folder.songCount} şarkı", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GenresContent(genres: List<Genre>, onClick: (Genre) -> Unit) {
    if (genres.isEmpty()) { Empty("Tür yok"); return }
    Column {
        Text("${genres.size} tür", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(genres, key = { it.name }) { genre ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(NeonSurface)
                        .clickable { onClick(genre) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(NeonPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center) {
                        Text(genre.name.take(1).uppercase(), color = NeonPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(genre.name, style = MaterialTheme.typography.bodyLarge)
                        Text("${genre.songCount} şarkı", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistsPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.QueueMusic, null, tint = NeonPrimary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(12.dp))
            Text("Çalma listeleri yakında", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun SongList(songs: List<Song>, currentId: Long?, isPlaying: Boolean, onClick: (Song) -> Unit, header: String?) {
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (header != null) {
            item {
                Text(header, style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 6.dp))
            }
        }
        items(songs, key = { it.id }) { SongItem(it, currentId == it.id && isPlaying) { onClick(it) } }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable private fun Empty(msg: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(msg, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun PermissionBox(onRequest: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.LibraryMusic, null, tint = NeonPrimary, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(16.dp))
        Text("Müzik İzni Gerekli", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Şarkıları görebilmek için izin ver.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest, colors = ButtonDefaults.buttonColors(containerColor = NeonPrimary)) {
            Text("İzin Ver", color = androidx.compose.ui.graphics.Color.Black)
        }
    }
}
