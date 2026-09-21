package com.neonmusic.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPrimary,
    secondary = NeonSecondary,
    background = NeonBackground,
    surface = NeonSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = NeonOnSurface,
    onSurface = NeonOnSurface
)

@Composable
fun NeonMusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
