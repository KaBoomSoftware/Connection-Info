package cz.kaboom.connectioninfo.data.network

import kotlinx.serialization.json.Json

internal val networkJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
