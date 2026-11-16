package com.nb.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.barlow_condensed_medium
import nbapp.shared.generated.resources.inter
import org.jetbrains.compose.resources.Font

/** Barlow Condensed: scoreboard-like titles and scores. */
@Composable
internal fun displayFontFamily() = FontFamily(
    Font(Res.font.barlow_condensed_medium, FontWeight.Medium),
)

/** Inter (variable font): names, cities and labels. */
@Composable
internal fun textFontFamily() = FontFamily(
    Font(Res.font.inter, FontWeight.Normal),
    Font(Res.font.inter, FontWeight.Medium),
    Font(Res.font.inter, FontWeight.SemiBold),
)

@Composable
internal fun nbaTypography(): Typography {
    val display = displayFontFamily()
    val text = textFontFamily()
    val base = Typography()
    fun TextStyle.display() = copy(fontFamily = display, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
    fun TextStyle.text() = copy(fontFamily = text)
    return Typography(
        displayLarge = base.displayLarge.display(),
        displayMedium = base.displayMedium.display(),
        displaySmall = base.displaySmall.display(),
        headlineLarge = base.headlineLarge.display(),
        headlineMedium = base.headlineMedium.display().copy(fontSize = 34.sp),
        headlineSmall = base.headlineSmall.display(),
        titleLarge = base.titleLarge.display().copy(fontSize = 26.sp),
        titleMedium = base.titleMedium.text().copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.text(),
        bodyLarge = base.bodyLarge.text(),
        bodyMedium = base.bodyMedium.text(),
        bodySmall = base.bodySmall.text(),
        labelLarge = base.labelLarge.text(),
        labelMedium = base.labelMedium.text(),
        labelSmall = base.labelSmall.text(),
    )
}

/** Big scoreboard numbers with equal-width digits, so scores line up. */
@Composable
internal fun scoreTextStyle() = TextStyle(
    fontFamily = displayFontFamily(),
    fontWeight = FontWeight.Medium,
    fontSize = 32.sp,
    fontFeatureSettings = "tnum",
)
