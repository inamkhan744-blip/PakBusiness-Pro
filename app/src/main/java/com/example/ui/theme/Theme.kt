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
    primary = PakEmeraldLight,
    onPrimary = Color.Black,
    primaryContainer = PakEmeraldDark,
    onPrimaryContainer = PakEmeraldContainer,
    secondary = Color(0xFFFFBA59),
    onSecondary = Color(0xFF452B00),
    secondaryContainer = PakGoldSecondary,
    onSecondaryContainer = PakGoldContainer,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    outline = OutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PakEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = PakEmeraldContainer,
    onPrimaryContainer = PakOnEmeraldContainer,
    secondary = PakGoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = PakGoldContainer,
    onSecondaryContainer = PakOnGoldContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    outline = OutlineLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun PakBusinessTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

