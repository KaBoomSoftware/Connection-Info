package cz.kaboom.connectioninfo.feature.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.kaboom.connectioninfo.feature.main.MainLayoutSpec
import cz.kaboom.connectioninfo.feature.main.UiText
import cz.kaboom.connectioninfo.feature.main.formatPlain
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoColors

@Composable
internal fun AppFooter(
    versionName: String,
    layoutSpec: MainLayoutSpec
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = layoutSpec.footerVerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = UiText.version.formatPlain(versionName),
            color = ConnectionInfoColors.TextMuted,
            fontSize = layoutSpec.versionTextSize,
            textAlign = TextAlign.Center
        )
        Text(
            text = UiText.copyright,
            color = ConnectionInfoColors.TextMuted,
            fontSize = layoutSpec.copyrightTextSize,
            textAlign = TextAlign.Center
        )
    }
}
