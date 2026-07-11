package com.example.glucoserecordbook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Colors = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF00695C), onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF3EC), onPrimaryContainer = Color(0xFF003731),
    secondary = Color(0xFF00897B), onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDECE4), onSecondaryContainer = Color(0xFF003D36),
    background = Color(0xFFFAF8F2), onBackground = Color(0xFF18201E),
    surface = Color(0xFFFFFDFC), onSurface = Color(0xFF18201E),
    surfaceVariant = Color(0xFFEDF4F1), onSurfaceVariant = Color(0xFF33413D),
    outline = Color(0xFF60716C), outlineVariant = Color(0xFFBFCAC6),
    error = Color(0xFFBA1A1A), onError = Color.White
)

private val GentleShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val AccessibleTypography = Typography(
    displayLarge = TextStyle(fontSize = 46.sp, lineHeight = 54.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 19.sp, lineHeight = 27.sp),
    labelLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
)

@Composable fun GlucoseRecordBookTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = AccessibleTypography, shapes = GentleShapes, content = content)
}
