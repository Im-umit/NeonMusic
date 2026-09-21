package com.neonmusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.neonmusic.player.domain.model.Album
import com.neonmusic.player.ui.theme.NeonPrimary
import com.neonmusic.player.ui.theme.NeonSurface

@Composable
fun AlbumItem(album: Album, onClick: () -> Unit) {
    Column(Modifier.width(150.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(6.dp)) {
        Box(Modifier.size(138.dp).clip(RoundedCornerShape(12.dp)).background(NeonSurface), contentAlignment = Alignment.Center) {
            if (!album.albumArtUri.isNullOrBlank()) {
                AsyncImage(model = album.albumArtUri, contentDescription = album.name,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Album, null, tint = NeonPrimary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(album.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${album.artist} • ${album.songCount}", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
