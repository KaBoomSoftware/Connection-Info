package cz.kaboom.connectioninfo.presentation.main

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import cz.kaboom.connectioninfo.di.createIosAppGraph
import cz.kaboom.connectioninfo.feature.main.ConnectionInfoApp
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoTheme
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController

/** UIKit entry point consumed by the SwiftUI host app. */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val graph = remember { createIosAppGraph() }
    val presenter = graph.mainPresenter
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

private fun iosVersionName(): String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
