package com.nb.app.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nb.app.core.ui.theme.NbaTheme
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.nba_logo_badge
import nbapp.shared.generated.resources.nba_logo_wordmark
import org.jetbrains.compose.resources.painterResource

private val LogoHeight = 80.dp
private val BadgeWidth = LogoHeight * (113f / 258f)
private val WordmarkWidth = LogoHeight * (291f / 258f)
private val LogoGap = LogoHeight * (30f / 258f)
private val WordmarkSlideDistance = 24.dp

/** NBA logo animating in over the app background gradient. Calls [onFinished] when done. */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    val elapsed = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        elapsed.animateTo(
            targetValue = SPLASH_DURATION_MILLIS,
            animationSpec = tween(durationMillis = SPLASH_DURATION_MILLIS.toInt(), easing = LinearEasing),
        )
        currentOnFinished()
    }

    SplashScene(frame = splashFrame(elapsed.value), modifier = modifier)
}

@Composable
private fun SplashScene(
    frame: SplashFrame,
    modifier: Modifier = Modifier,
) {
    val background = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.background, NbaTheme.colors.backgroundGradientEnd),
    )
    Box(
        modifier = modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.nba_logo_badge),
                contentDescription = null,
                modifier = Modifier
                    .width(BadgeWidth)
                    .height(LogoHeight)
                    .graphicsLayer {
                        alpha = frame.badgeAlpha
                        scaleX = frame.badgeScale
                        scaleY = frame.badgeScale
                    },
            )
            Spacer(Modifier.width(LogoGap))
            Image(
                painter = painterResource(Res.drawable.nba_logo_wordmark),
                contentDescription = "NBA",
                modifier = Modifier
                    .width(WordmarkWidth)
                    .height(LogoHeight)
                    .graphicsLayer {
                        alpha = frame.wordmarkAlpha
                        translationX = -frame.wordmarkSlide * WordmarkSlideDistance.toPx()
                    },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            )
        }
    }
}

@Preview
@Composable
private fun SplashScenePreview() {
    NbaTheme {
        SplashScene(frame = splashFrame(SPLASH_DURATION_MILLIS))
    }
}
