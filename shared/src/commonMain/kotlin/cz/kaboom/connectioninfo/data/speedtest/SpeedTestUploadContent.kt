package cz.kaboom.connectioninfo.data.speedtest

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.math.min

/** Streaming request body used by the upload phase. */
internal class SpeedTestUploadContent : OutgoingContent.WriteChannelContent() {
    /** Exact payload size sent to the upload endpoint. */
    override val contentLength: Long = SpeedTestConfig.uploadFileSize.toLong()

    /** Binary payload marker understood by the Cloudflare upload endpoint. */
    override val contentType: ContentType = ContentType.Application.OctetStream

    /** Writes zero-filled chunks until the configured upload size is reached. */
    override suspend fun writeTo(channel: ByteWriteChannel) {
        val buffer = ByteArray(UploadBufferSize)
        var remainingBytes = contentLength

        while (remainingBytes > 0) {
            currentCoroutineContext().ensureActive()
            val byteCount = min(buffer.size.toLong(), remainingBytes).toInt()
            channel.writeFully(buffer, 0, byteCount)
            remainingBytes -= byteCount
        }
    }

    private companion object {
        const val UploadBufferSize = 64 * 1024
    }
}
