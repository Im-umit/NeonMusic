package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.LruCache
import android.util.Size
import com.example.data.db.AppMusicDatabase
import com.example.data.db.entity.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

object AlbumArtManager {
    private const val TAG = "AlbumArtManager"

    // In-memory cache for artwork bytes (up to 8 MB)
    private val memoryCache = object : LruCache<String, ByteArray>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int {
            return value.size
        }
    }

    // Negative cache to quickly skip tracks that have no artwork
    private val negativeCache = object : LruCache<String, Boolean>(1000) {}

    fun clearCache() {
        memoryCache.evictAll()
        negativeCache.evictAll()
    }

    fun getArtworkBytes(context: Context, songId: Long): ByteArray? {
        val cacheKey = "art_$songId"
        if (negativeCache.get(cacheKey) == true) {
            return null
        }

        val cachedBytes = memoryCache.get(cacheKey)
        if (cachedBytes != null) {
            return cachedBytes
        }

        val db = AppMusicDatabase.getInstance(context)
        val song: SongEntity? = try {
            runBlocking(Dispatchers.IO) {
                db.songDao().getSongById(songId) ?: db.songDao().getSongByAlbumId(songId)
            }
        } catch (e: Exception) {
            null
        }

        val bytes = loadArtworkBytes(context, song, songId)
        return if (bytes != null && bytes.isNotEmpty()) {
            memoryCache.put(cacheKey, bytes)
            bytes
        } else {
            negativeCache.put(cacheKey, true)
            null
        }
    }

    fun getArtStream(context: Context, uri: Uri): Pair<InputStream, String>? {
        val lastSegment = uri.lastPathSegment ?: return null
        val id = lastSegment.toLongOrNull() ?: return null

        val bytes = getArtworkBytes(context, id)
        return if (bytes != null && bytes.isNotEmpty()) {
            Pair(ByteArrayInputStream(bytes), "image/jpeg")
        } else {
            null
        }
    }

    private fun loadArtworkBytes(context: Context, song: SongEntity?, id: Long): ByteArray? {
        if (song != null) {
            // 1. Extract embedded picture using MediaMetadataRetriever
            val retriever = MediaMetadataRetriever()
            try {
                if (song.path.isNotBlank() && File(song.path).exists()) {
                    retriever.setDataSource(song.path)
                } else if (song.uri.isNotBlank()) {
                    retriever.setDataSource(context, Uri.parse(song.uri))
                }
                val pic = retriever.embeddedPicture
                if (pic != null && pic.isNotEmpty()) {
                    return pic
                }
            } catch (e: Exception) {
                // Ignore retrieval errors
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    // Ignore release errors
                }
            }

            // 2. Try ContentResolver.loadThumbnail for Android Q+ (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && song.uri.isNotBlank()) {
                try {
                    val bitmap = context.contentResolver.loadThumbnail(
                        Uri.parse(song.uri),
                        Size(512, 512),
                        null
                    )
                    val bos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos)
                    val thumbnailBytes = bos.toByteArray()
                    if (thumbnailBytes.isNotEmpty()) {
                        return thumbnailBytes
                    }
                } catch (e: Exception) {
                    // Ignore thumbnail errors
                }
            }
        }

        // 3. Try legacy content resolver albumart uri only on pre-Q
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                val legacyUri = Uri.parse("content://media/external/audio/albumart/$id")
                context.contentResolver.openInputStream(legacyUri)?.use { input ->
                    val bos = ByteArrayOutputStream()
                    val buffer = ByteArray(4096)
                    var len: Int
                    while (input.read(buffer).also { len = it } != -1) {
                        bos.write(buffer, 0, len)
                    }
                    val data = bos.toByteArray()
                    if (data.isNotEmpty()) return data
                }
            } catch (e: Exception) {
                // Not found in legacy provider
            }
        }

        return null
    }
}
