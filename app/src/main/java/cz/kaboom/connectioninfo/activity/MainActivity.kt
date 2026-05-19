package cz.kaboom.connectioninfo.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.kaboom.connectioninfo.R
import cz.kaboom.connectioninfo.feature.main.ConnectionInfoApp
import cz.kaboom.connectioninfo.presentation.main.MainViewModel
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: ""

        setContent {
            val state = viewModel.uiState.collectAsStateWithLifecycle()

            ConnectionInfoTheme {
                ConnectionInfoApp(
                    state = state.value,
                    versionName = versionName,
                    onAction = viewModel::onAction
                )
            }
        }
    }
}
