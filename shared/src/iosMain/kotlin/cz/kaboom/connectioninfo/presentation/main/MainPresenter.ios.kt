package cz.kaboom.connectioninfo.presentation.main

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import cz.kaboom.connectioninfo.data.connectivity.IosConnectivityObserver
import cz.kaboom.connectioninfo.data.network.IosNetworkInfoRepository
import cz.kaboom.connectioninfo.data.network.NetworkLookupClient
import cz.kaboom.connectioninfo.data.speedtest.DefaultSpeedTestRepository
import cz.kaboom.connectioninfo.feature.main.ConnectionInfoApp
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoTheme
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController

/** Creates the shared presenter with iOS-backed network lookups. */
fun createMainPresenter(): MainPresenter {
    val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(networkJson)
        }
    }
    val lookupClient = NetworkLookupClient(client)

    return MainPresenter(
        connectivityObserver = IosConnectivityObserver(),
        networkInfoRepository = IosNetworkInfoRepository(lookupClient),
        speedTestRepository = DefaultSpeedTestRepository(client),
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    )
}

/** UIKit entry point consumed by the SwiftUI host app. */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val presenter = remember { createMainPresenter() }
    val state by presenter.uiState.collectAsState()

    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }

    ConnectionInfoTheme {
        ConnectionInfoApp(
            state = state,
            versionName = iosVersionName(),
            onAction = presenter::onAction
        )
    }
}

private fun iosVersionName(): String {
    return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
}

private val networkJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
