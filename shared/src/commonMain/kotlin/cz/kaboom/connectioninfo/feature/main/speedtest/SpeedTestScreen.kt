package cz.kaboom.connectioninfo.feature.main.speedtest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kaboom.connectioninfo.domain.model.speedtest.SpeedTestPhase
import cz.kaboom.connectioninfo.feature.main.MainLayoutSpec
import cz.kaboom.connectioninfo.feature.main.UiText
import cz.kaboom.connectioninfo.feature.main.formatDecimal
import cz.kaboom.connectioninfo.presentation.main.SpeedTestUiState
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoColors

/** Speed test page containing the gauge, metric panel, progress copy, and action button. */
@Composable
internal fun SpeedTestScreen(
    state: SpeedTestUiState,
    internetAvailable: Boolean,
    onToggleTest: () -> Unit,
    layoutSpec: MainLayoutSpec
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = layoutSpec.horizontalPadding,
                vertical = layoutSpec.speedVerticalPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = layoutSpec.contentMaxWidth)
        ) {
            SpeedGauge(
                value = state.gaugeValue,
                maxValue = 500f,
                layoutSpec = layoutSpec,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(layoutSpec.gaugeHeight)
            )

            Text(
                text = UiText.speed,
                color = ConnectionInfoColors.TextPrimary,
                fontSize = layoutSpec.sectionTitleTextSize,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 10.dp)
            )

            SpeedStatsPanel(
                ping = state.ping,
                download = state.download,
                upload = state.upload,
                layoutSpec = layoutSpec
            )

            SpeedProgressBar(
                progress = state.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = layoutSpec.progressTopPadding)
                    .height(6.dp)
            )

            Text(
                text = state.progressText(),
                color = ConnectionInfoColors.TextSecondary,
                fontSize = layoutSpec.actionTextSize,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            if (internetAvailable) {
                Button(
                    onClick = onToggleTest,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConnectionInfoColors.MaterialPurple,
                        contentColor = ConnectionInfoColors.ActionText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = layoutSpec.actionTopPadding)
                        .height(layoutSpec.buttonHeight)
                ) {
                    Text(
                        text = (if (state.running) UiText.stopTest else UiText.startTest).uppercase(),
                        fontSize = layoutSpec.actionTextSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = layoutSpec.actionTopPadding)
                        .height(layoutSpec.buttonHeight)
                ) {
                    Text(
                        text = UiText.networkUnavailable.uppercase(),
                        color = ConnectionInfoColors.Error,
                        fontSize = layoutSpec.actionTextSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun SpeedTestUiState.progressText(): String {
    return when (phase) {
        SpeedTestPhase.PING -> "Ping: ${formatDecimal(progress * 100f, 0)}%"
        SpeedTestPhase.DOWNLOAD -> "DL: ${formatDecimal(progress * 100f, 0)}%"
        SpeedTestPhase.UPLOAD -> "UL: ${formatDecimal(progress * 100f, 0)}%"
        SpeedTestPhase.IDLE -> ""
    }
}
