package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NaturalPrimaryGreen,
    secondary = NaturalMossGreen,
    tertiary = NaturalContainerGreen,
    background = NaturalDarkBG,
    surface = NaturalCardBG,
    onPrimary = Color.White,
    onSecondary = NaturalAccentLabel,
    onBackground = NaturalTextDark,
    onSurface = NaturalTextDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalPrimaryGreen,
    secondary = NaturalMossGreen,
    tertiary = NaturalContainerGreen,
    background = NaturalDarkBG,
    surface = NaturalCardBG,
    onPrimary = Color.White,
    onSecondary = NaturalAccentLabel,
    onBackground = NaturalTextDark,
    onSurface = NaturalTextDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
