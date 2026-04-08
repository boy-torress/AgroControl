package com.agrocontrol.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.agrocontrol.R

// ─── Google Font Provider ─────────────────────────────────────────────────────
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

private val PlusJakartaSans = GoogleFont("Plus Jakarta Sans")

val PlusJakartaSansFamily = FontFamily(
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.ExtraBold),
)

// ─── Tipografía premium ───────────────────────────────────────────────────────
val AppTypography = Typography(
    displayLarge  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.ExtraBold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold,      fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold,      fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold,      fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium   = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,    fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

// ─── Paleta AgroControl Premium ────────────────────────────────────────────────
val Verde80      = Color(0xFF0F291E)
val Verde60      = Color(0xFF1B4332)
val Verde40      = Color(0xFF2D6A4F)
val Verde20      = Color(0xFF40916C)
val VerdeLight   = Color(0xFFD8F3DC)
val VerdeAccent  = Color(0xFF52B788)
val VerdeNeon    = Color(0xFF4ADE80)

val GrisOscuro   = Color(0xFF111827)
val GrisMedio    = Color(0xFF6B7280)
val GrisClaro    = Color(0xFFF9FAFB)
val GrisSuave    = Color(0xFFF3F4F6)
val Blanco       = Color(0xFFFFFFFF)

val AmarilloAlert = Color(0xFFF59E0B)
val NaranjaAmanecer = Color(0xFFF97316)
val RojoAlert     = Color(0xFFEF4444)
val AzulInfo      = Color(0xFF3B82F6)

// Dark mode premium
private val DarkColorScheme = darkColorScheme(
    primary            = VerdeNeon,
    onPrimary          = Color(0xFF003822),
    primaryContainer   = Verde60,
    onPrimaryContainer = VerdeLight,
    secondary          = VerdeAccent,
    onSecondary        = GrisOscuro,
    secondaryContainer = Verde40,
    onSecondaryContainer = Blanco,
    tertiary           = AzulInfo,
    background         = Color(0xFF0D1117), // Deep space black-gray
    surface            = Color(0xFF161B22), // Slightly lighter for cards
    surfaceVariant     = Color(0xFF1F2937),
    onBackground       = Color(0xFFE6EDF3),
    onSurface          = Color(0xFFE6EDF3),
    onSurfaceVariant   = Color(0xFF9CA3AF),
    outline            = Color(0xFF374151),
    error              = RojoAlert,
    errorContainer     = Color(0xFF3B0000),
)

private val LightColorScheme = lightColorScheme(
    primary            = Verde60,
    onPrimary          = Blanco,
    primaryContainer   = VerdeLight,
    onPrimaryContainer = Verde80,
    secondary          = Verde40,
    onSecondary        = Blanco,
    secondaryContainer = VerdeLight,
    onSecondaryContainer = Verde80,
    tertiary           = AzulInfo,
    background         = GrisClaro,
    surface            = Blanco,
    surfaceVariant     = GrisSuave,
    onBackground       = GrisOscuro,
    onSurface          = GrisOscuro,
    onSurfaceVariant   = GrisMedio,
    outline            = Color(0xFFD1D5DB),
    error              = RojoAlert,
    errorContainer     = Color(0xFFFEE2E2),
)

@Composable
fun AgroControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
