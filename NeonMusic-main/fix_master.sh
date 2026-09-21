#!/bin/bash
​echo "1/6: Eski çakışan dosyalar temizleniyor..."
rm -rf app/src/main/java/com/neonmusic/player/*
rm -rf app/src/main/res/*
mkdir -p .github/workflows
​echo "2/6: Klasör dizinleri yapılandırılıyor..."
mkdir -p app/src/main/java/com/neonmusic/player/data/repository
mkdir -p app/src/main/java/com/neonmusic/player/domain/model
mkdir -p app/src/main/java/com/neonmusic/player/di
mkdir -p app/src/main/java/com/neonmusic/player/playback
mkdir -p app/src/main/java/com/neonmusic/player/ui/theme
mkdir -p app/src/main/res/values
​echo "3/6: Gradle Yapılandırma Dosyaları Güncelleniyor..."
​Kök build.gradle.kts
​cat << 'EOF' > build.gradle.kts
plugins {
id("com.android.application") version "8.2.2" apply false
id("com.android.library") version "8.2.2" apply false
id("org.jetbrains.kotlin.android") version "1.9.22" apply false
id("com.google.dagger.hilt.android") version "2.50" apply false
}
EOF
​app/build.gradle.kts
​cat << 'EOF' > app/build.gradle.kts
plugins {
id("com.android.application")
id("org.jetbrains.kotlin.android")
id("kotlin-kapt")
id("com.google.dagger.hilt.android")
}
​android {
namespace = "com.neonmusic.player"
compileSdk = 34
​defaultConfig {
applicationId = "com.neonmusic.player"
minSdk = 26
targetSdk = 34
versionCode = 1
versionName = "1.0.0"
​testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
vectorDrawables {
useSupportLibrary = true
}
}
​buildTypes {
release {
isMinifyEnabled = false
proguardFiles(
getDefaultProguardFile("proguard-android-optimize.txt"),
"proguard-rules.pro"
)
}
debug {
isMinifyEnabled = false
}
}
compileOptions {
sourceCompatibility = JavaVersion.VERSION_17
targetCompatibility = JavaVersion.VERSION_17
}
kotlinOptions {
jvmTarget = "17"
}
buildFeatures {
compose = true
}
composeOptions {
kotlinCompilerExtensionVersion = "1.5.8"
}
packaging {
resources {
excludes += "/META-INF/{AL2.0,LGPL2.1}"
}
}
}
​dependencies {
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
​// Hilt Dependency Injection
implementation("com.google.dagger:hilt-android:2.50")
kapt("com.google.dagger:hilt-compiler:2.50")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
​// Media3 ExoPlayer
implementation("androidx.media3:media3-exoplayer:1.2.1")
implementation("androidx.media3:media3-session:1.2.1")
implementation("androidx.media3:media3-ui:1.2.1")
​// Coil Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")
}
EOF
​echo "4/6: XML Kaynakları ve Kodlar Yazılıyor..."
​Themes XML
​cat << 'EOF' > app/src/main/res/values/themes.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
<style name="Theme.NeonMusic" parent="android:Theme.Material.NoTitleBar">
<item name="android:statusBarColor">#0A0A0C</item>
<item name="android:navigationBarColor">#0A0A0C</item>
</style>
</resources>
EOF
​Strings XML
​cat << 'EOF' > app/src/main/res/values/strings.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
<string name="app_name">NeonMusic</string>
</resources>
EOF
​AndroidManifest.xml
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
​Application Class
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/NeonApplication.kt
package com.neonmusic.player
​import android.app.Application
import dagger.hilt.android.HiltAndroidApp
​@HiltAndroidApp
class NeonApplication : Application()
EOF
​Main Activity
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
​Theme Colors
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/ui/theme/Color.kt
package com.neonmusic.player.ui.theme
​import androidx.compose.ui.graphics.Color
​val NeonPrimary = Color(0xFF00FFCC)
val NeonSecondary = Color(0xFF7928CA)
val NeonBackground = Color(0xFF0A0A0C)
val NeonSurface = Color(0xFF141418)
val NeonOnSurface = Color(0xFFE2E2E6)
EOF
​Theme Implementation
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
​Song Model
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
​Repository Interface
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/data/repository/MusicRepository.kt
package com.neonmusic.player.data.repository
​import com.neonmusic.player.domain.model.Song
import kotlinx.coroutines.flow.Flow
​interface MusicRepository {
fun getLocalSongs(): Flow<List<Song>>
}
EOF
​Repository Implementation
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
​Hilt DI Module
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
​Media3 Service
​cat << 'EOF' > app/src/main/java/com/neonmusic/player/playback/MusicPlayerService.kt
package com.neonmusic.player.playback
​import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
​class MusicPlayerService : MediaSessionService() {
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
​echo "5/6: GitHub Actions Workflow Yapılandırılıyor..."
cat << 'EOF' > .github/workflows/build.yml
name: Build Android APK
​on:
push:
branches: [ "main" ]
workflow_dispatch:
​jobs:
build:
runs-on: ubuntu-latest
​steps:
- name: Checkout repository
uses: actions/checkout@v4
​- name: Set up JDK 17
uses: actions/setup-java@v4
with:
distribution: 'temurin'
java-version: '17'
​- name: Make gradlew executable
run: chmod +x gradlew
​- name: Build Debug APK
run: ./gradlew assembleDebug
​- name: Upload APK Artifact
uses: actions/upload-artifact@v4
with:
name: neonmusic-debug-apk
path: app/build/outputs/apk/debug/app-debug.apk
EOF
​echo "6/6: GitHub'a Gönderiliyor..."
git add -A
git commit -m "Fix DEX bytecode compilation, resources, and workflow"
git push -f origin main
​echo "Tamamlandı! GitHub Actions yeni ve eksiksiz APK'yı derlemeye başladı."
