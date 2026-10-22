package com.groupa.coreui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GroupBlue,
    onPrimary = White,
    secondary = CheckoutBlue,
    background = AppBackground,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = LightGray,
    onSurfaceVariant = Gray500,
    outline = DividerGray,
)

@Composable
fun GroupATheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = GroupATypography,
        content = content,
    )
}
