package cz.kaboom.connectioninfo.feature.main.network

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.kaboom.connectioninfo.domain.model.network.NetworkDetails
import cz.kaboom.connectioninfo.feature.main.MainLayoutSpec
import cz.kaboom.connectioninfo.feature.main.UiText
import cz.kaboom.connectioninfo.ui.theme.ConnectionInfoColors

/** Network information page showing local and public connection metadata. */
@Composable
internal fun NetworkInfoScreen(
    info: NetworkDetails?,
    internetAvailable: Boolean,
    errorMessage: String?,
    layoutSpec: MainLayoutSpec
) {
    val statusMessage = when {
        !internetAvailable -> UiText.networkUnavailable
        info == null && errorMessage != null -> errorMessage
        else -> null
    }
    val isPending = internetAvailable && info == null && errorMessage == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = layoutSpec.horizontalPadding,
                vertical = layoutSpec.networkVerticalPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = ConnectionInfoColors.Surface,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ConnectionInfoColors.SurfaceLine),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = layoutSpec.contentMaxWidth)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = layoutSpec.networkCardHorizontalPadding,
                    vertical = layoutSpec.networkCardVerticalPadding
                )
            ) {
                statusMessage?.let {
                    NetworkInfoRow(UiText.status, it, layoutSpec, isError = true)
                }
                NetworkInfoRow(UiText.network, info?.transport?.displayName.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.internalIp, info?.internalIp.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.externalIp, info?.externalIp.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.isp, info?.lookup?.isp.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.org, info?.lookup?.organization.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.city, info?.lookup?.city.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.region, info?.lookup?.region.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.regionName, info?.lookup?.regionName.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.country, info?.lookup?.country.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.countryCode, info?.lookup?.countryCode.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.latitude, info?.lookup?.latitude.valueOrPlaceholder(isPending), layoutSpec)
                NetworkInfoRow(UiText.longitude, info?.lookup?.longitude.valueOrPlaceholder(isPending), layoutSpec)
            }
        }
    }
}

/** Keeps Network Info readable while the first lookup is loading or partial data is unavailable. */
private fun String?.valueOrPlaceholder(isPending: Boolean): String {
    return when {
        isPending -> UiText.loading
        isNullOrBlank() -> UiText.notAvailable
        else -> this
    }
}

/** Label/value row used in the Network Info card. */
@Composable
private fun NetworkInfoRow(
    label: String,
    value: String,
    layoutSpec: MainLayoutSpec,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = layoutSpec.networkRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = ConnectionInfoColors.TextSecondary,
            fontSize = layoutSpec.networkTextSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(layoutSpec.networkLabelWidth)
        )
        Text(
            text = value,
            color = if (isError) ConnectionInfoColors.Error else ConnectionInfoColors.SpeedValue,
            fontSize = layoutSpec.networkTextSize,
            lineHeight = layoutSpec.networkValueLineHeight,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
