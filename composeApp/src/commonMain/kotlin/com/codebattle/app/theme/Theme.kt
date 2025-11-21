package com.codebattle.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CyberBlack = Color(0xFF121212)
val CyberDarkGrey = Color(0xFF1E1E1E)
val NeonPurple = Color(0xFFD0BCFF) // Light Purple
val NeonPurpleDark = Color(0xFF6650a4)
val AcidGreen = Color(0xFFCCFF00) // Vivid Green
val AcidGreenDark = Color(0xFF668000)
val White = Color(0xFFFFFFFF)
val LightGrey = Color(0xFFCCCCCC)

val CodeBattleColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = CyberBlack,
    primaryContainer = NeonPurpleDark,
    onPrimaryContainer = NeonPurple,
    secondary = AcidGreen,
    onSecondary = CyberBlack,
    secondaryContainer = AcidGreenDark,
    onSecondaryContainer = AcidGreen,
    background = CyberBlack,
    onBackground = White,
    surface = CyberDarkGrey,
    onSurface = White
)

@Composable
fun CodeBattleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CodeBattleColorScheme,
        content = content
    )
}

