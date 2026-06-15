package cz.kaboom.connectioninfo.presentation.main

import android.content.Context
import cz.kaboom.connectioninfo.data.connectivity.AndroidConnectivityObserver
import cz.kaboom.connectioninfo.data.network.DefaultNetworkInfoRepository
import cz.kaboom.connectioninfo.data.network.remote.NetworkLookupClient
import cz.kaboom.connectioninfo.data.speedtest.DefaultSpeedTestRepository
import cz.kaboom.connectioninfo.data.network.networkJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Creates the shared presenter with Android-backed connectivity and network details. */
fun createMainPresenter(context: Context): MainPresenter {
    val client = HttpClient(Android) {
        engine {
            connectTimeout = 10_000
            socketTimeout = 30_000
        }
        install(ContentNegotiation) {
            json(networkJson)
        }
    }
    val lookupClient = NetworkLookupClient(client)

    return MainPresenter(
        connectivityObserver = AndroidConnectivityObserver(context),
        networkInfoRepository = DefaultNetworkInfoRepository(
            context = context,
            networkLookupClient = lookupClient,
            ioDispatcher = Dispatchers.IO
        ),
        speedTestRepository = DefaultSpeedTestRepository(
            client = client,
            ioDispatcher = Dispatchers.IO
        ),
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    )
}

