package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
  primary = PrimaryCorporate,
  onPrimary = OnPrimaryCorporate,
  primaryContainer = PrimaryCorporateContainer,
  onPrimaryContainer = OnPrimaryCorporateContainer,
  secondary = SecondaryCorporate,
  onSecondary = OnSecondaryCorporate,
  secondaryContainer = SecondaryCorporateContainer,
  onSecondaryContainer = OnSecondaryCorporateContainer,
  tertiary = TertiaryCorporate,
  onTertiary = OnTertiaryCorporate,
  tertiaryContainer = TertiaryCorporateContainer,
  onTertiaryContainer = OnTertiaryCorporateContainer,
  background = CorporateBackground,
  onBackground = CorporateOnBackground,
  surface = CorporateSurface,
  onSurface = CorporateOnSurface,
  surfaceVariant = CorporateSurfaceVariant,
  onSurfaceVariant = CorporateOnSurfaceVariant,
  outline = CorporateOutline
)

private val DarkColorScheme = darkColorScheme(
  primary = DarkPrimary,
  onPrimary = DarkOnPrimary,
  primaryContainer = PrimaryCorporate,
  onPrimaryContainer = SoftLightBlue,
  secondary = DarkSecondary,
  onSecondary = DarkOnPrimary,
  secondaryContainer = SecondaryCorporateContainer,
  onSecondaryContainer = OnSecondaryCorporateContainer,
  background = DarkBackground,
  onBackground = CorporateSurface,
  surface = DarkSurface,
  onSurface = CorporateSurface,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = SoftLightBlue,
  outline = DeepBlueBase
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
