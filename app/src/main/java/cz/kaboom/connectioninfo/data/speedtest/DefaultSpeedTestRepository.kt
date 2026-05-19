package cz.kaboom.connectioninfo.data.speedtest

import cz.kaboom.connectioninfo.common.Const
import cz.kaboom.connectioninfo.di.modules.IoDispatcher
import cz.kaboom.connectioninfo.domain.model.SpeedTestPhase
import cz.kaboom.connectioninfo.domain.model.SpeedTestUpdate
import cz.kaboom.connectioninfo.domain.repository.SpeedTestRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 * Performs the network speed test and streams progress updates as a cold [Flow].
 *
 * The implementation runs ping, download, and upload phases sequentially on the IO dispatcher. Ktor
 * is used for all network calls, and the upload body is streamed to avoid allocating the full
 * payload in memory.
 */
class DefaultSpeedTestRepository @Inject constructor(
    private val client: HttpClient,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SpeedTestRepository {

    /** Starts a full speed test when collected and cancels network work when the collector stops. */
    override fun runSpeedTest(): Flow<SpeedTestUpdate> = channelFlow {
        val runner = launch(ioDispatcher) {
            try {
                send(SpeedTestUpdate.Started)
                runPingTest { trySend(it) }
                runDownloadTest { trySend(it) }
                runUploadTest { trySendBlocking(it) }
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
        }
    }

    /** Measures request latency using several tiny cache-busted Cloudflare downloads. */
    private suspend fun runPingTest(
        onProgress: (SpeedTestUpdate.Latency) -> Unit
    ) {
        repeat(Const.PING_SAMPLE_COUNT) { index ->
            currentCoroutineContext().ensureActive()
            val startedAt = System.nanoTime()
            val response = client.get(Const.SPEED_TEST_PING_URL) {
                parameter(CACHE_BUSTER_PARAMETER, System.nanoTime())
            }
            check(response.status.isSuccess()) { "Ping failed: HTTP ${response.status.value}" }
            response.bodyAsText()

            val elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000.0
            onProgress(
                SpeedTestUpdate.Latency(
                    percent = ((index + 1).toFloat() / Const.PING_SAMPLE_COUNT.toFloat()) * 100f,
                    milliseconds = elapsedMillis
                )
            )
            kotlinx.coroutines.delay(PING_SAMPLE_DELAY_MS)
        }
    }

    /** Streams a large remote file for a fixed duration and reports calculated throughput. */
    private suspend fun runDownloadTest(
        onProgress: (SpeedTestUpdate.Progress) -> Unit
    ) {
        val startedAt = System.nanoTime()
        val durationNanos = TimeUnit.MILLISECONDS.toNanos(Const.DOWNLOAD_TEST_DURATION_MS)
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var downloadedBytes = 0L
        var lastReportAt = 0L

        client.prepareGet(Const.SPEED_TEST_DOWNLOAD_URL).execute { response ->
            check(response.status.isSuccess()) { "Download failed: HTTP ${response.status.value}" }
            val channel = response.bodyAsChannel()

            while (System.nanoTime() - startedAt < durationNanos) {
                currentCoroutineContext().ensureActive()
                val read = channel.readAvailable(buffer, 0, buffer.size)
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

    /** Streams a generated binary payload and reports upload throughput through Ktor callbacks. */
    private suspend fun runUploadTest(
        onProgress: (SpeedTestUpdate.Progress) -> Unit
    ) {
        val startedAt = System.nanoTime()
        val durationNanos = TimeUnit.MILLISECONDS.toNanos(Const.UPLOAD_TEST_DURATION_MS)
        var uploadedBytes = 0L

        client.preparePost(Const.SPEED_TEST_UPLOAD_URL) {
            setBody(SpeedTestUploadContent())
            onUpload { bytesSentTotal, _ ->
                uploadedBytes = bytesSentTotal
                onProgress(progress(SpeedTestPhase.UPLOAD, uploadedBytes, startedAt, System.nanoTime(), durationNanos))
            }
        }.execute { response ->
            check(response.status.isSuccess()) { "Upload failed: HTTP ${response.status.value}" }
        }

        waitUntilDeadline(SpeedTestPhase.UPLOAD, uploadedBytes, startedAt, durationNanos, onProgress)
    }

    /** Keeps the current phase visually alive until its configured duration has elapsed. */
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

    /** Converts transferred bytes and elapsed time into a normalized progress event. */
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

    /**
     * Streaming request body used by the upload phase.
     *
     * Ktor asks this content to write chunks into the request channel, keeping memory usage stable
     * regardless of [Const.UPLOAD_FILE_SIZE].
     */
    private class SpeedTestUploadContent : OutgoingContent.WriteChannelContent() {
        /** Exact payload size sent to the upload endpoint. */
        override val contentLength: Long = Const.UPLOAD_FILE_SIZE.toLong()

        /** Binary payload marker understood by the Cloudflare upload endpoint. */
        override val contentType: ContentType = ContentType.Application.OctetStream

        /** Writes zero-filled chunks until the configured upload size is reached. */
        override suspend fun writeTo(channel: ByteWriteChannel) {
            val buffer = ByteArray(UPLOAD_BUFFER_SIZE)
            var remainingBytes = contentLength

            while (remainingBytes > 0) {
                currentCoroutineContext().ensureActive()
                val byteCount = min(buffer.size.toLong(), remainingBytes).toInt()
                channel.writeFully(buffer, 0, byteCount)
                remainingBytes -= byteCount
            }
        }
    }

    /** Tunable implementation details that should not leak into the domain contract. */
    private companion object {
        const val DEFAULT_BUFFER_SIZE = 64 * 1024
        const val UPLOAD_BUFFER_SIZE = 64 * 1024
        const val REPORT_INTERVAL_NANOS = 250_000_000L
        const val PING_SAMPLE_DELAY_MS = 120L
        const val CACHE_BUSTER_PARAMETER = "_"
    }
}
