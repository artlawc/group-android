package com.groupa.presentation.download

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CarModelDownloadStatus {
    Idle,
    Downloading,
    Completed,
    Failed,
}

data class CarModelDownloadState(
    val status: CarModelDownloadStatus = CarModelDownloadStatus.Idle,
    val modelFile: File? = null,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val error: String? = null,
)

object CarModelDownloadManager {
    private const val ModelFileName = "CarConcept.glb"
    private const val TemporaryFileName = "CarConcept.glb.tmp"
    private const val NotificationChannelId = "car_3d_model_download"
    private const val NotificationId = 4021
    private const val ProgressUpdateBytes = 512L * 1024
    private const val ConnectTimeoutMillis = 15_000
    private const val ReadTimeoutMillis = 120_000

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableState = MutableStateFlow(CarModelDownloadState())
    private var downloadJob: Job? = null
    private var isScreenVisible = false
    private var isAppInForeground = false
    private val mutableCompletionEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val state: StateFlow<CarModelDownloadState> = mutableState.asStateFlow()
    val completionEvent: SharedFlow<Unit> = mutableCompletionEvent.asSharedFlow()

    fun start(context: Context, modelUrl: String) {
        val applicationContext = context.applicationContext
        isScreenVisible = true

        val targetFile = File(applicationContext.cacheDir, ModelFileName)
        if (!targetFile.isFile || !isValidGlbFile(targetFile)) {
            mutableState.update { CarModelDownloadState() }
        }
        if (targetFile.isFile && isValidGlbFile(targetFile)) {
            mutableState.update {
                CarModelDownloadState(
                    status = CarModelDownloadStatus.Completed,
                    modelFile = targetFile,
                    downloadedBytes = targetFile.length(),
                    totalBytes = targetFile.length(),
                )
            }
            return
        }

        if (mutableState.value.status == CarModelDownloadStatus.Downloading) {
            return
        }

        val temporaryFile = File(applicationContext.cacheDir, TemporaryFileName)
        mutableState.update {
            CarModelDownloadState(
                status = CarModelDownloadStatus.Downloading,
                downloadedBytes = if (temporaryFile.isFile) temporaryFile.length() else 0,
                totalBytes = readGlbTotalLength(temporaryFile) ?: 0,
            )
        }

        downloadJob?.cancel()
        downloadJob = scope.launch {
            try {
                val completedFile = download(applicationContext, modelUrl)
                mutableState.update {
                    CarModelDownloadState(
                        status = CarModelDownloadStatus.Completed,
                        modelFile = completedFile,
                        downloadedBytes = completedFile.length(),
                        totalBytes = completedFile.length(),
                    )
                }
                if (!isScreenVisible) {
                    if (isAppInForeground) {
                        mutableCompletionEvent.tryEmit(Unit)
                    } else {
                        showCompletionNotification(applicationContext)
                    }
                }
            } catch (error: IOException) {
                mutableState.update {
                    CarModelDownloadState(
                        status = CarModelDownloadStatus.Failed,
                        error = error.message ?: "the download could not be completed",
                    )
                }
            }
        }
    }

    fun retry(context: Context, modelUrl: String) {
        mutableState.update { CarModelDownloadState(status = CarModelDownloadStatus.Idle) }
        start(context, modelUrl)
    }

    fun markScreenExited() {
        isScreenVisible = false
    }

    fun markAppInForeground() {
        isAppInForeground = true
    }

    fun markAppInBackground() {
        isAppInForeground = false
    }

    private suspend fun download(context: Context, modelUrl: String): File {
        val targetFile = File(context.cacheDir, ModelFileName)
        if (targetFile.isFile && isValidGlbFile(targetFile)) {
            return targetFile
        }

        val temporaryFile = File(context.cacheDir, TemporaryFileName)
        if (temporaryFile.isFile && isCompleteGlbFile(temporaryFile)) {
            promoteTemporaryFile(temporaryFile, targetFile)
            return targetFile
        }

        var resumeOffset = if (temporaryFile.isFile) temporaryFile.length() else 0
        if (resumeOffset > 0 && !isResumablePartialGlbFile(temporaryFile)) {
            temporaryFile.delete()
            resumeOffset = 0
        }

        val connection = URL(modelUrl).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = ConnectTimeoutMillis
            connection.readTimeout = ReadTimeoutMillis
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Accept", "application/octet-stream")
            connection.setRequestProperty("User-Agent", "GroupA/1.0")
            if (resumeOffset > 0) {
                connection.setRequestProperty("Range", "bytes=$resumeOffset-")
            }

            val responseCode = connection.responseCode
            when {
                responseCode == HttpURLConnection.HTTP_PARTIAL -> Unit
                responseCode == 416 && isCompleteGlbFile(temporaryFile) -> {
                    promoteTemporaryFile(temporaryFile, targetFile)
                    return targetFile
                }
                responseCode == 416 -> {
                    temporaryFile.delete()
                    resumeOffset = 0
                    mutableState.update { it.copy(downloadedBytes = 0, totalBytes = 0) }
                    return download(context, modelUrl)
                }
                responseCode in 200..299 -> {
                    if (resumeOffset > 0) {
                        temporaryFile.delete()
                        resumeOffset = 0
                        mutableState.update { it.copy(downloadedBytes = 0, totalBytes = 0) }
                    }
                }
                else -> throw IOException("the server returned HTTP $responseCode")
            }

            val contentLength = connection.contentLengthLong
            val appendToFile = responseCode == HttpURLConnection.HTTP_PARTIAL && resumeOffset > 0
            val expectedLength = if (resumeOffset > 0) {
                readGlbTotalLength(temporaryFile)
            } else {
                null
            }
            var totalBytes = expectedLength ?: when {
                contentLength >= 0 && appendToFile -> resumeOffset + contentLength
                contentLength >= 0 -> contentLength
                else -> 0
            }

            mutableState.update { state ->
                state.copy(
                    downloadedBytes = resumeOffset,
                    totalBytes = totalBytes,
                )
            }

            FileOutputStream(temporaryFile, appendToFile).use { output ->
                connection.inputStream.use { input ->
                    val buffer = ByteArray(64 * 1024)
                    var downloadedBytes = resumeOffset
                    var lastUpdateBytes = resumeOffset
                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) {
                            break
                        }
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        readGlbTotalLength(temporaryFile)?.let { expectedTotalBytes ->
                            totalBytes = expectedTotalBytes
                        }
                        if (downloadedBytes - lastUpdateBytes >= ProgressUpdateBytes) {
                            lastUpdateBytes = downloadedBytes
                            mutableState.update { state ->
                                state.copy(
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes,
                                )
                            }
                        }
                    }
                    output.flush()

                    val finalExpectedLength = readGlbTotalLength(temporaryFile)
                    if (finalExpectedLength != null && downloadedBytes != finalExpectedLength) {
                        throw IOException("the download stopped before the model was complete")
                    }
                    mutableState.update { state ->
                        state.copy(
                            downloadedBytes = downloadedBytes,
                            totalBytes = finalExpectedLength ?: totalBytes,
                        )
                    }
                }
            }

            if (!isValidGlbFile(temporaryFile)) {
                throw IOException("the download did not return a complete GLB file")
            }
            promoteTemporaryFile(temporaryFile, targetFile)
            return targetFile
        } finally {
            connection.disconnect()
        }
    }

    private fun promoteTemporaryFile(temporaryFile: File, targetFile: File) {
        if (targetFile.exists() && !targetFile.delete()) {
            throw IOException("the cached model could not be replaced")
        }
        if (!temporaryFile.renameTo(targetFile)) {
            throw IOException("the downloaded model could not be saved")
        }
    }

    private fun showCompletionNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                NotificationChannelId,
                "3D model downloads",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, NotificationChannelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("3D model ready")
            .setContentText("The car model has finished downloading.")
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)?.notify(NotificationId, notification)
    }

    private fun File.hasGlbMagic(): Boolean = runCatching {
        RandomAccessFile(this, "r").use { input ->
            val magic = ByteArray(GlbMagic.size)
            input.readFully(magic)
            magic.contentEquals(GlbMagic)
        }
    }.getOrDefault(false)

    internal fun readGlbTotalLength(file: File): Long? = runCatching {
        if (file.length() < GlbHeaderLength) {
            return@runCatching null
        }
        RandomAccessFile(file, "r").use { input ->
            val lengthBytes = ByteArray(Int.SIZE_BYTES)
            input.seek(GlbTotalLengthOffset)
            input.readFully(lengthBytes)
            ByteBuffer.wrap(lengthBytes).order(ByteOrder.LITTLE_ENDIAN).int.toLong()
        }
    }.getOrNull()

    internal fun isCompleteGlbFile(file: File): Boolean = isValidGlbFile(file) &&
        readGlbTotalLength(file) == file.length()

    private fun isResumablePartialGlbFile(file: File): Boolean {
        if (!file.hasGlbMagic()) {
            return false
        }
        val declaredLength = readGlbTotalLength(file) ?: return file.length() >= GlbHeaderLength
        return file.length() in GlbHeaderLength until declaredLength
    }

    private fun isValidGlbFile(file: File): Boolean = file.isFile &&
        file.length() > GlbHeaderLength &&
        file.hasGlbMagic()

    private const val GlbHeaderLength = 12L
    private const val GlbTotalLengthOffset = 8L
    private val GlbMagic = byteArrayOf(0x67, 0x6C, 0x54, 0x46)
}
