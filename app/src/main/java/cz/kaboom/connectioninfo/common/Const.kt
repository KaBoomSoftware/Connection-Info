package cz.kaboom.connectioninfo.common

/**
 * Central place for network endpoints and speed-test tuning values.
 *
 * Keeping these constants together makes the measurement behavior auditable and keeps repository
 * implementations focused on orchestration rather than magic numbers.
 */
object Const {
    /** Lightweight Cloudflare endpoint used for latency samples. */
    const val SPEED_TEST_PING_URL = "https://speed.cloudflare.com/__down?bytes=1"

    /** Large remote file streamed for the timed download measurement. */
    const val SPEED_TEST_DOWNLOAD_URL = "https://proof.ovh.net/files/100Mb.dat"

    /** Cloudflare upload endpoint accepting the generated binary test payload. */
    const val SPEED_TEST_UPLOAD_URL = "https://speed.cloudflare.com/__up"

    /** Number of latency probes collected before throughput phases begin. */
    const val PING_SAMPLE_COUNT = 5

    /** Duration of the download phase in milliseconds. */
    const val DOWNLOAD_TEST_DURATION_MS = 6000L

    /** Duration of the upload phase in milliseconds. */
    const val UPLOAD_TEST_DURATION_MS = 5000L

    /** External IP lookup endpoint returning a plain-text address. */
    const val IPAPI_BASE_URL = "https://api.ipify.org"

    /** IP geolocation lookup endpoint; the target IP is appended to this base URL. */
    const val LOOKUP_BASE_URL = "http://ip-api.com/json/"

    /** Number of bytes streamed into the upload request body. */
    const val UPLOAD_FILE_SIZE = 50 * 1000000
}
