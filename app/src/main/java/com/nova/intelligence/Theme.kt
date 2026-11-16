package com.nova.intelligence

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Paleta de NOVA Intelligence, tomada de tu concepto. */
object NovaColors {
    val Background = Color(0xFF0B0D14)
    val Surface = Color(0xFF141824)
    val Tile = Color(0xFF1F2438)
    val Outline = Color(0xFF2A3047)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF9AA3B8)
    val Purple = Color(0xFF9B8AF7)
    val PurpleDark = Color(0xFF241B4B)
    val Cyan = Color(0xFF38C8E8)
    val Green = Color(0xFF34D399)
    val GreenDark = Color(0xFF0B3B2A)
}

private val NovaColorScheme = darkColorScheme(
    primary = NovaColors.Purple,
    onPrimary = NovaColors.Background,
    background = NovaColors.Background,
    onBackground = NovaColors.TextPrimary,
    surface = NovaColors.Surface,
    onSurface = NovaColors.TextPrimary,
    outline = NovaColors.Outline,
)

@Composable
fun NovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NovaColorScheme, content = content)
}
