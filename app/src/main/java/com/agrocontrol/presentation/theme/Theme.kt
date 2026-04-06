package com.agrocontrol.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Paleta AgroControl ───────────────────────────────────────────────────────
val Verde80      = Color(0xFF2D6A4F)
val Verde60      = Color(0xFF40916C)
val Verde40      = Color(0xFF52B788)
val Verde20      = Color(0xFFB7E4C7)
val VerdeLight   = Color(0xFFD8F3DC)

val TierraOscuro = Color(0xFF6B4226)
val TierraMedio  = Color(0xFF9C6644)
val TierraClaro  = Color(0xFFE8C9A0)

val AmarilloAlert = Color(0xFFF4A261)
val RojoAlert     = Color(0xFFE63946)
val AzulInfo      = Color(0xFF457B9D)

val GrisOscuro   = Color(0xFF1B1B1B)
val GrisMedio    = Color(0xFF6B6B6B)
val GrisClaro    = Color(0xFFF5F5F5)
val Blanco       = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary         = Verde40,
    onPrimary       = GrisOscuro,
    primaryContainer= Verde80,
    secondary       = TierraMedio,
    background      = Color(0xFF121212),
    surface         = Color(0xFF1E1E1E),
    onBackground    = Blanco,
    onSurface       = Blanco,
    error           = RojoAlert
)

private val LightColorScheme = lightColorScheme(
    primary         = Verde60,
    onPrimary       = Blanco,
    primaryContainer= VerdeLight,
    onPrimaryContainer = Verde80,
    secondary       = TierraMedio,
    onSecondary     = Blanco,
    secondaryContainer = TierraClaro,
    background      = GrisClaro,
    surface         = Blanco,
    onBackground    = GrisOscuro,
    onSurface       = GrisOscuro,
    surfaceVariant  = Color(0xFFEEF2EE),
    error           = RojoAlert
)

@Composable
fun AgroControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography(),
        content     = content
    )
}
