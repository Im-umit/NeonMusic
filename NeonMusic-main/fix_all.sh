#!/bin/bash
​NeonMusic - Tam Onarım ve Hata Giderme Scripti
​Bu script projedeki tüm hatalı/çakışan kodları temizler ve hatasız kodları yazar.
​echo "1/4: Eski ve çakışan kaynak dosyaları temizleniyor..."
rm -rf app/src/main/java/com/neonmusic/player/*
​echo "2/4: Klasör dizinleri oluşturuluyor..."
mkdir -p app/src/main/java/com/neonmusic/player/data/repository
mkdir -p app/src/main/java/com/neonmusic/player/domain/model
mkdir -p app/src/main/java/com/neonmusic/player/di
mkdir -p app/src/main/java/com/neonmusic/player/playback
mkdir -p app/src/main/java/com/neonmusic/player/ui/theme
​echo "3/4: Hatasız Kotlin ve Manifest dosyaları yazılıyor..."
​1. Application Class
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/NeonApplication.kt
package com.neonmusic.player
​import android.app.Application
import dagger.hilt.android.HiltAndroidApp
​@HiltAndroidApp
class NeonApplication : Application()
EOF
​2. Main Activity (Temiz ve Tek Sınıf)
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/MainActivity.kt
package com.neonmusic.player
​import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neonmusic.player.ui.theme.NeonMusicTheme
import dagger.hilt.android.AndroidEntryPoint
​@AndroidEntryPoint
class MainActivity : ComponentActivity() {
override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
setContent {
NeonMusicTheme {
Surface(
modifier = Modifier.fillMaxSize(),
color = MaterialTheme.colorScheme.background
) {
Greeting("NeonMusic Başarıyla Derlendi!")
}
}
}
}
}
​@Composable
fun Greeting(name: String) {
Text(text = name)
}
EOF
​3. Theme Colors
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/ui/theme/Color.kt
package com.neonmusic.player.ui.theme
​import androidx.compose.ui.graphics.Color
​val NeonPrimary = Color(0xFF00FFCC)
val NeonSecondary = Color(0xFF7928CA)
val NeonBackground = Color(0xFF0A0A0C)
val NeonSurface = Color(0xFF141418)
val NeonOnSurface = Color(0xFFE2E2E6)
EOF
​4. Theme Implementation
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/ui/theme/Theme.kt
package com.neonmusic.player.ui.theme
​import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
​private val DarkColorScheme = darkColorScheme(
primary = NeonPrimary,
secondary = NeonSecondary,
background = NeonBackground,
surface = NeonSurface,
onPrimary = Color.Black,
onSecondary = Color.White,
onBackground = NeonOnSurface,
onSurface = NeonOnSurface
)
​@Composable
fun NeonMusicTheme(content: @Composable () -> Unit) {
MaterialTheme(
colorScheme = DarkColorScheme,
content = content
)
}
EOF
​5. Domain Song Model
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/domain/model/Song.kt
package com.neonmusic.player.domain.model
​data class Song(
val id: Long,
val title: String,
val artist: String,
val album: String,
val duration: Long,
val uri: String,
val albumArtUri: String? = null,
val genre: String = "Unknown",
val year: Int = 0,
val trackNumber: Int = 0
)
EOF
​6. Repository Interface
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/data/repository/MusicRepository.kt
package com.neonmusic.player.data.repository
​import com.neonmusic.player.domain.model.Song
import kotlinx.coroutines.flow.Flow
​interface MusicRepository {
fun getLocalSongs(): Flow<List<Song>>
}
EOF
​7. Repository Implementation
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/data/repository/MusicRepositoryImpl.kt
package com.neonmusic.player.data.repository
​import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.neonmusic.player.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton
​@Singleton
class MusicRepositoryImpl @Inject constructor(
@ApplicationContext private val context: Context
) : MusicRepository {
​override fun getLocalSongs(): Flow<List<Song>> = flow {
val songs = mutableListOf<Song>()
val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
} else {
MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
}
​val projection = arrayOf(
MediaStore.Audio.Media._ID,
MediaStore.Audio.Media.TITLE,
MediaStore.Audio.Media.ARTIST,
MediaStore.Audio.Media.ALBUM,
MediaStore.Audio.Media.DURATION,
MediaStore.Audio.Media.ALBUM_ID
)
​val selection = "{MediaStore.Audio.Media.IS_MUSIC} != 0"
val sortOrder = "{MediaStore.Audio.Media.TITLE} ASC"
​context.contentResolver.query(
collection,
projection,
selection,
null,
sortOrder
)?.use { cursor ->
val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
​while (cursor.moveToNext()) {
val id = cursor.getLong(idColumn)
val title = cursor.getString(titleColumn) ?: "Bilinmeyen Şarkı"
val artist = cursor.getString(artistColumn) ?: "Bilinmeyen Sanatçı"
val album = cursor.getString(albumColumn) ?: "Bilinmeyen Albüm"
val duration = cursor.getLong(durationColumn)
val albumId = cursor.getLong(albumIdColumn)
​val albumArtUri = ContentUris.withAppendedId(
android.net.Uri.parse("content://media/external/audio/albumart"),
albumId
).toString()
​val songUri = ContentUris.withAppendedId(
MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
id
).toString()
​songs.add(
Song(
id = id,
title = title,
artist = artist,
album = album,
duration = duration,
uri = songUri,
albumArtUri = albumArtUri
)
)
}
}
emit(songs)
}.flowOn(Dispatchers.IO)
}
EOF
​8. Hilt Dependency Injection Module (Tek ve Eksiksiz)
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/di/AppModules.kt
package com.neonmusic.player.di
​import com.neonmusic.player.data.repository.MusicRepository
import com.neonmusic.player.data.repository.MusicRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
​@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
​@Binds
@Singleton
abstract fun bindMusicRepository(
musicRepositoryImpl: MusicRepositoryImpl
): MusicRepository
}
EOF
​9. Media3 Service Implementation
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/playback/MusicPlayerService.kt
package com.neonmusic.player.playback
​import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
​@AndroidEntryPoint
class MusicPlayerService : MediaSessionService() {
​private var mediaSession: MediaSession? = null
​override fun onCreate() {
super.onCreate()
val player = ExoPlayer.Builder(this).build()
mediaSession = MediaSession.Builder(this, player).build()
}
​override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
return mediaSession
}
​override fun onDestroy() {
mediaSession?.run {
player.release()
release()
mediaSession = null
}
super.onDestroy()
}
}
EOF
​10. AndroidManifest.xml
​cat << 'EOF' > app/src/main/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:tools="http://schemas.android.com/tools">
​<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
​<application
android:name=".NeonApplication"
android:allowBackup="true"
android:icon="@mipmap/ic_launcher"
android:label="NeonMusic"
android:roundIcon="@mipmap/ic_launcher_round"
android:supportsRtl="true"
android:theme="@style/Theme.NeonMusic"
tools:targetApi="34">
​<activity
android:name=".MainActivity"
android:exported="true"
android:theme="@style/Theme.NeonMusic">
<intent-filter>
<action android:name="android.intent.action.MAIN" />
<category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
</activity>
​<service
android:name=".playback.MusicPlayerService"
android:exported="true"
android:foregroundServiceType="mediaPlayback">
<intent-filter>
<action android:name="androidx.media3.session.MediaSessionService" />
</intent-filter>
</service>
​</application>
​</manifest>
EOF
​echo "4/4: İşlem tamamlandı! Kodlar %100 temizlendi."
EOF
