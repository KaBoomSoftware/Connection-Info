package cz.kaboom.connectioninfo.data.speedtest

import cz.kaboom.connectioninfo.common.Const
import cz.kaboom.connectioninfo.domain.model.SpeedTestPhase
import cz.kaboom.connectioninfo.domain.model.SpeedTestUpdate
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min

class DefaultSpeedTestRepository(
    private val client: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher
) : SpeedTestRepository {

    override fun runSpeedTest(): Flow<SpeedTestUpdate> = channelFlow {
        val activeCall = AtomicReference<Call?>()
        val runner = launch(ioDispatcher) {
            try {
                send(SpeedTestUpdate.Started)
                runDownloadTest(activeCall) { trySend(it) }
                runUploadTest(activeCall) { trySendBlocking(it) }
                send(SpeedTestUpdate.Finished)
            } catch (e: CancellationException) {
                send(SpeedTestUpdate.Finished)
                throw e
            } catch (e: Exception) {
                send(SpeedTestUpdate.Failed(e.localizedMessage ?: "Speed test failed", e))
            }
        }

        awaitClose {
            runner.cancel()
            activeCall.getAndSet(null)?.cancel()
        }
    }

    private suspend fun runDownloadTest(
        activeCall: AtomicReference<Call?>,
        onProgress: (SpeedTestUpdate.Progress) -> Unit
    ) {
        val startedAt = System.nanoTime()
        val durationNanos = TimeUnit.MILLISECONDS.toNanos(Const.DOWNLOAD_TEST_DURATION_MS)
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var downloadedBytes = 0L
        var lastReportAt = 0L

        val request = Request.Builder()
            .url(Const.SPEED_TEST_DOWNLOAD_URL)
            .build()

        execute(request, activeCall).use { response ->
            check(response.isSuccessful) { "Download failed: HTTP ${response.code}" }
            val input = response.body.byteStream()

            while (System.nanoTime() - startedAt < durationNanos) {
                currentCoroutineContext().ensureActive()
                val read = input.read(buffer)
                if (read == -1) break

                downloadedBytes += read
                val now = System.nanoTime()
                if (now - lastReportAt >= REPORT_INTERVAL_NANOS) {
                    lastReportAt = now
                    onProgress(progress(SpeedTestPhase.DOWNLOAD, downloadedBytes, startedAt, now, durationNanos))
                }
            }
        }

        waitUntilDeadline(SpeedTestPhase.DOWNLOAD, downloadedBytes, startedAt, durationNanos, onProgress)
    }

    private suspend fun runUploadTest(
        activeCall: AtomicReference<Call?>,
        onProgress: (SpeedTestUpdate.Progress) -> Unit
    ) {
        val startedAt = System.nanoTime()
        val durationNanos = TimeUnit.MILLISECONDS.toNanos(Const.UPLOAD_TEST_DURATION_MS)
        val deadlineNanos = startedAt + durationNanos
        var uploadedBytes = 0L

        val body = CountingRequestBody(Const.UPLOAD_FILE_SIZE, deadlineNanos) { bytes ->
            uploadedBytes = bytes
            onProgress(progress(SpeedTestPhase.UPLOAD, uploadedBytes, startedAt, System.nanoTime(), durationNanos))
        }

        val request = Request.Builder()
            .url(Const.SPEED_TEST_UPLOAD_URL)
            .post(body)
            .build()

        execute(request, activeCall).use { response ->
            check(response.isSuccessful) { "Upload failed: HTTP ${response.code}" }
        }

        waitUntilDeadline(SpeedTestPhase.UPLOAD, uploadedBytes, startedAt, durationNanos, onProgress)
    }

    private suspend fun waitUntilDeadline(
        phase: SpeedTestPhase,
        transferredBytes: Long,
        startedAt: Long,
        durationNanos: Long,
        onProgress: (SpeedTestUpdate.Progress) -> Unit
    ) {
        while (System.nanoTime() - startedAt < durationNanos) {
            currentCoroutineContext().ensureActive()
            kotlinx.coroutines.delay(REPORT_INTERVAL_NANOS / 1_000_000)
            onProgress(progress(phase, transferredBytes, startedAt, System.nanoTime(), durationNanos))
        }
    }

    private fun execute(
        request: Request,
        activeCall: AtomicReference<Call?>
    ): okhttp3.Response {
        val call = client.newCall(request)
        activeCall.set(call)
        return call.execute().also { activeCall.compareAndSet(call, null) }
    }

    private fun progress(
        phase: SpeedTestPhase,
        transferredBytes: Long,
        startedAt: Long,
        now: Long,
        durationNanos: Long
    ): SpeedTestUpdate.Progress {
        val elapsedSeconds = max((now - startedAt) / 1_000_000_000.0, 0.001)
        return SpeedTestUpdate.Progress(
            phase = phase,
            percent = min(100f, ((now - startedAt).toFloat() / durationNanos.toFloat()) * 100f),
            bitsPerSecond = (transferredBytes * 8.0) / elapsedSeconds
        )
    }

    private class CountingRequestBody(
        private val size: Int,
        private val deadlineNanos: Long,
        private val onProgress: (Long) -> Unit
    ) : RequestBody() {
        override fun contentType(): MediaType? = null
        override fun contentLength(): Long = -1

        override fun writeTo(sink: BufferedSink) {
            val chunk = ByteArray(DEFAULT_BUFFER_SIZE)
            var written = 0
            var lastReportAt = 0L

            while (written < size && System.nanoTime() < deadlineNanos) {
                val byteCount = min(chunk.size, size - written)
                sink.write(chunk, 0, byteCount)
                written += byteCount

                val now = System.nanoTime()
                if (now - lastReportAt >= REPORT_INTERVAL_NANOS || written >= size) {
                    lastReportAt = now
                    onProgress(written.toLong())
                }
            }
        }
    }

    private companion object {
        const val DEFAULT_BUFFER_SIZE = 64 * 1024
        const val REPORT_INTERVAL_NANOS = 250_000_000L
    }
}
