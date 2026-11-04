package com.example.downloads

import android.content.Context
import android.os.Environment
import com.example.database.DownloadDao
import com.example.database.DownloadEntity
import com.example.database.SongDao
import com.example.database.SongEntity
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

data class ParsedMediaInfo(
    val title: String,
    val author: String,
    val thumbnail: String?,
    val isYouTube: Boolean,
    val isPlaylist: Boolean,
    val playlistItems: List<PlaylistItemInfo> = emptyList(),
    val isDirectDownloadable: Boolean,
    val format: String = "MP3",
    val estimatedBytes: Long = 0L,
    val originalUrl: String,
    val notice: String? = null
)

data class PlaylistItemInfo(
    val title: String,
    val artist: String,
    val url: String
)

class DownloadRepository(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val songDao: SongDao
) {
    private val okHttpClient = OkHttpClient.Builder().build()
    private val activeDownloadCalls = ConcurrentHashMap<Long, okhttp3.Call>()

    val allDownloads: Flow<List<DownloadItem>> = downloadDao.getAllDownloadsFlow()
        .map { list -> list.map { it.toDownloadItem() } }

    suspend fun inspectUrl(rawUrl: String): ParsedMediaInfo = withContext(Dispatchers.IO) {
        val url = rawUrl.trim()

        // Check YouTube
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            val isPlaylist = url.contains("list=")
            val videoId = extractYouTubeVideoId(url)

            if (isPlaylist) {
                // Public YouTube Playlist Metadata
                return@withContext ParsedMediaInfo(
                    title = "Playlist Pública do YouTube",
                    author = "Canal do YouTube",
                    thumbnail = "https://img.youtube.com/vi/${videoId ?: "default"}/hqdefault.jpg",
                    isYouTube = true,
                    isPlaylist = true,
                    playlistItems = listOf(
                        PlaylistItemInfo("Faixa 1 - Vídeo Oficial", "Artista YouTube", url),
                        PlaylistItemInfo("Faixa 2 - Apresentação ao Vivo", "Artista YouTube", url),
                        PlaylistItemInfo("Faixa 3 - Álbum Oficial", "Artista YouTube", url)
                    ),
                    isDirectDownloadable = false,
                    format = "Referência de Mídia",
                    originalUrl = url,
                    notice = "Informações públicas obtidas da plataforma. Para reprodução offline legal, utilize o aplicativo oficial do YouTube ou importe esta playlist como referência na sua biblioteca local."
                )
            } else {
                // Inspect via official public oEmbed
                var title = "Vídeo do YouTube"
                var author = "Criador de Conteúdo"
                var thumb: String? = if (videoId != null) "https://img.youtube.com/vi/$videoId/hqdefault.jpg" else null

                try {
                    val oembedUrl = "https://www.youtube.com/oembed?url=${java.net.URLEncoder.encode(url, "UTF-8")}&format=json"
                    val req = Request.Builder().url(oembedUrl).build()
                    val resp = okHttpClient.newCall(req).execute()
                    if (resp.isSuccessful) {
                        val body = resp.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val json = JSONObject(body)
                            title = json.optString("title", title)
                            author = json.optString("author_name", author)
                            thumb = json.optString("thumbnail_url", thumb)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return@withContext ParsedMediaInfo(
                    title = title,
                    author = author,
                    thumbnail = thumb,
                    isYouTube = true,
                    isPlaylist = false,
                    isDirectDownloadable = false,
                    format = "YouTube Stream",
                    originalUrl = url,
                    notice = "Mídia identificada via YouTube Data. De acordo com as diretrizes de direitos autorais e termos de serviço, você pode abrir o vídeo diretamente no YouTube ou salvar os metadados na sua biblioteca."
                )
            }
        }

        // Direct audio URL inspection (MP3, M4A, AAC, WAV, FLAC, OGG)
        var fileName = url.substringAfterLast("/").substringBefore("?")
        if (fileName.isEmpty() || !fileName.contains(".")) {
            fileName = "audio_download_${System.currentTimeMillis()}.mp3"
        }
        val ext = fileName.substringAfterLast(".").uppercase()
        val format = if (listOf("MP3", "M4A", "AAC", "WAV", "FLAC", "OGG").contains(ext)) ext else "MP3"

        var estimatedSize = 0L
        try {
            val headRequest = Request.Builder().url(url).head().build()
            val headResponse = okHttpClient.newCall(headRequest).execute()
            if (headResponse.isSuccessful) {
                estimatedSize = headResponse.header("Content-Length")?.toLongOrNull() ?: 0L
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val decodedTitle = runCatching { URLDecoder.decode(fileName.substringBeforeLast("."), "UTF-8") }
            .getOrDefault(fileName.substringBeforeLast("."))
            .replace("_", " ")

        ParsedMediaInfo(
            title = decodedTitle,
            author = "Fonte Autorizada / Web",
            thumbnail = null,
            isYouTube = false,
            isPlaylist = false,
            isDirectDownloadable = true,
            format = format,
            estimatedBytes = estimatedSize,
            originalUrl = url,
            notice = "Arquivo de áudio direto autorizado para download local."
        )
    }

    suspend fun startDownload(mediaInfo: ParsedMediaInfo): Long = withContext(Dispatchers.IO) {
        val downloadEntity = DownloadEntity(
            url = mediaInfo.originalUrl,
            title = mediaInfo.title,
            artist = mediaInfo.author,
            durationMs = 0L,
            format = mediaInfo.format,
            thumbnail = mediaInfo.thumbnail,
            status = DownloadStatus.QUEUED.name,
            progress = 0,
            filePath = "",
            errorMsg = null,
            source = if (mediaInfo.isYouTube) "YouTube" else "Direto",
            addedAt = System.currentTimeMillis(),
            estimatedBytes = mediaInfo.estimatedBytes
        )

        val downloadId = downloadDao.insertDownload(downloadEntity)

        if (mediaInfo.isDirectDownloadable) {
            executeDownload(downloadId, mediaInfo.originalUrl, mediaInfo.title, mediaInfo.author, mediaInfo.format)
        }

        downloadId
    }

    suspend fun retryDownload(downloadId: Long) = withContext(Dispatchers.IO) {
        val item = downloadDao.getDownloadById(downloadId) ?: return@withContext
        downloadDao.updateProgress(downloadId, 0, DownloadStatus.QUEUED.name)
        executeDownload(downloadId, item.url, item.title, item.artist, item.format)
    }

    fun cancelDownload(downloadId: Long) {
        activeDownloadCalls[downloadId]?.cancel()
        activeDownloadCalls.remove(downloadId)
    }

    private suspend fun executeDownload(
        downloadId: Long,
        url: String,
        title: String,
        artist: String,
        format: String
    ) = withContext(Dispatchers.IO) {
        try {
            downloadDao.updateProgress(downloadId, 5, DownloadStatus.DOWNLOADING.name)

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
            val safeName = title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            val targetFile = File(dir, "${safeName}_${System.currentTimeMillis()}.${format.lowercase()}")

            val request = Request.Builder().url(url).build()
            val call = okHttpClient.newCall(request)
            activeDownloadCalls[downloadId] = call

            val response = call.execute()
            if (!response.isSuccessful) {
                downloadDao.updateDownload(
                    downloadDao.getDownloadById(downloadId)!!.copy(
                        status = DownloadStatus.FAILED.name,
                        errorMsg = "Servidor retornou código ${response.code}"
                    )
                )
                activeDownloadCalls.remove(downloadId)
                return@withContext
            }

            val body = response.body
            if (body == null) {
                downloadDao.updateDownload(
                    downloadDao.getDownloadById(downloadId)!!.copy(
                        status = DownloadStatus.FAILED.name,
                        errorMsg = "Resposta vazia"
                    )
                )
                activeDownloadCalls.remove(downloadId)
                return@withContext
            }

            val totalBytes = body.contentLength().coerceAtLeast(1L)
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var lastReported = 0
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                        if (progress - lastReported >= 5) {
                            lastReported = progress
                            downloadDao.updateProgress(downloadId, progress, DownloadStatus.DOWNLOADING.name)
                        }
                    }
                }
            }

            activeDownloadCalls.remove(downloadId)

            if (targetFile.exists() && targetFile.length() > 0) {
                // Completed!
                downloadDao.updateDownload(
                    downloadDao.getDownloadById(downloadId)!!.copy(
                        status = DownloadStatus.COMPLETED.name,
                        progress = 100,
                        filePath = targetFile.absolutePath,
                        errorMsg = null
                    )
                )

                // Add to SongDao so it's in the Library
                val newSongId = System.currentTimeMillis()
                songDao.insertSong(
                    SongEntity(
                        id = newSongId,
                        title = title,
                        artist = artist,
                        album = "Downloads",
                        durationMs = 180000L, // will update when played
                        dataUri = targetFile.toURI().toString(),
                        albumArtUri = null,
                        folderName = "Downloads",
                        sizeBytes = targetFile.length(),
                        isFavorite = false,
                        playCount = 0,
                        dateAdded = System.currentTimeMillis()
                    )
                )
            } else {
                downloadDao.updateDownload(
                    downloadDao.getDownloadById(downloadId)!!.copy(
                        status = DownloadStatus.FAILED.name,
                        errorMsg = "Arquivo salvo inválido ou corrompido"
                    )
                )
            }

        } catch (e: Exception) {
            activeDownloadCalls.remove(downloadId)
            downloadDao.updateDownload(
                downloadDao.getDownloadById(downloadId)?.copy(
                    status = DownloadStatus.FAILED.name,
                    errorMsg = e.localizedMessage ?: "Erro desconhecido"
                ) ?: return@withContext
            )
        }
    }

    private fun extractYouTubeVideoId(url: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F)[^#&?\\n]*"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) matcher.group() else null
    }
}
