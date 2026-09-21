package com.neonmusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.neonmusic.player.domain.model.Song
import com.neonmusic.player.ui.theme.NeonPrimary
import com.neonmusic.player.ui.theme.NeonSurface

@Composable
fun PlayerBar(
    song: Song?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExpand: () -> Unit
) {
    if (song == null) return
    Surface(
        modifier = Modifier.fillMaxWidth().padding(10.dp).clickable(onClick = onExpand),
        shape = RoundedCornerShape(16.dp),
        color = NeonSurface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(NeonPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                if (!song.albumArtUri.isNullOrBlank()) {
                    AsyncImage(model = song.albumArtUri, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = NeonPrimary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.SkipPrevious, "Önceki", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onPlayPause, modifier = Modifier.size(44.dp).clip(CircleShape).background(NeonPrimary)) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (isPlaying) "Duraklat" else "Çal", tint = androidx.compose.ui.graphics.Color.Black)
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.SkipNext, "Sonraki", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
