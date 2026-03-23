package com.example.treasurehuntapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppColors {
    val PurplePrimary = Color(0xFF8E63FF)
    val PurpleAccent = Color(0xFFB44BF3)
    val PurpleDeep = Color(0xFF4D257B)

    val BgTop = Color(0xFF13071E)
    val BgMid = Color(0xFF180A26)
    val BgBottom = Color(0xFF100519)

    val Surface = Color(0xFF1B1125)
    val SurfaceAlt = Color(0xFF20162A)
    val SurfaceMuted = Color(0xFF221530)
    val SurfaceChip = Color(0xFF2B1B3C)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0A8C6)
    val TextMuted = Color(0xFF8C84A8)

    val Error = Color(0xFFFF6E8A)
    val Danger = Color(0xFFB4233D)
    val DangerBorder = Color(0xFF612338)

    val Success = Color(0xFFB8F7C3)
    val Border = Color(0xFF3A2B4C)
}

object AppGradients {
    val ScreenBackground: Brush
        get() = Brush.verticalGradient(
            listOf(AppColors.BgTop, AppColors.BgMid, AppColors.BgBottom)
        )

    val HeroPurple: Brush
        get() = Brush.verticalGradient(
            listOf(Color(0xFF8A19FF), Color(0xFF6A16C8), Color(0xFF4E119A))
        )

    val GlowPurpleTop: Brush
        get() = Brush.verticalGradient(
            listOf(AppColors.PurplePrimary.copy(alpha = 0.28f), Color.Transparent)
        )
}
