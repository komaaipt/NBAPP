package com.nb.app.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nb.app.core.ui.theme.SplashBackground
import kotlin.math.min
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

private val BallColor = Color(0xFFE8762B)
private val BallSeamColor = Color(0xFF2B1607)

private val BallRadius = 56.dp
/** The ball rests this much above the screen center; the name sits below it. */
private val BallLift = 40.dp
private val NameOffset = 64.dp

/**
 * Splash: a basketball is thrown from the bottom-left corner, lands in the middle with a small
 * bounce and the app name fades in below it. Calls [onFinished] when the animation ends.
 */
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFrame(frame)
        }
        Text(
            text = stringResource(Res.string.app_name),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = NameOffset)
                .graphicsLayer {
                    alpha = frame.nameAlpha
                    translationY = (1f - frame.nameAlpha) * 12.dp.toPx()
                },
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 10.sp,
        )
    }
}

private fun DrawScope.drawFrame(frame: SplashFrame) {
    val restRadius = BallRadius.toPx()
    val restCenter = Offset(size.width / 2f, size.height / 2f - BallLift.toPx())
    val floorY = restCenter.y + restRadius

    // Soft glow behind the landing spot, so the ball doesn't float on a flat background.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.10f * frame.flight), Color.Transparent),
            center = restCenter,
            radius = size.width * 0.6f,
        ),
        radius = size.width * 0.6f,
        center = restCenter,
    )

    // Shadow on the landing spot: shrinks while the ball is up in the air.
    val shadowWidth = restRadius * 1.6f * (1f - 0.25f * frame.bounceLift)
    drawOval(
        color = Color.Black.copy(alpha = frame.shadowAlpha),
        topLeft = Offset(restCenter.x - shadowWidth / 2f, floorY - restRadius * 0.08f),
        size = Size(shadowWidth, restRadius * 0.22f),
    )

    // Thrown from just outside the bottom-left corner, in an arc, to the landing spot.
    val radius = restRadius * frame.ballScale
    val start = Offset(-radius, size.height + radius)
    val path = Offset(
        x = lerp(start.x, restCenter.x, frame.flight),
        y = lerp(start.y, restCenter.y, frame.flight) - frame.arc * size.height * 0.3f,
    )
    val width = radius * 2f * (1f + 0.15f * frame.ballSquash)
    val height = radius * 2f * (1f - 0.2f * frame.ballSquash)
    // When squashed it stays on the floor instead of shrinking around its center.
    val center = Offset(
        x = path.x,
        y = path.y + (radius - height / 2f) - frame.bounceLift * restRadius,
    )
    drawBall(center, width, height, frame.ballRotationDegrees)
}

/** A basketball: orange with the classic seams, rotated by [rotationDegrees]. */
private fun DrawScope.drawBall(
    center: Offset,
    width: Float,
    height: Float,
    rotationDegrees: Float,
) {
    val bounds = Rect(center.x - width / 2f, center.y - height / 2f, center.x + width / 2f, center.y + height / 2f)
    val r = min(width, height) / 2f
    val seam = Stroke(width = r * 0.07f)

    drawOval(BallColor, topLeft = bounds.topLeft, size = bounds.size)
    clipPath(Path().apply { addOval(bounds) }) {
        rotate(rotationDegrees, pivot = center) {
            drawLine(BallSeamColor, Offset(center.x, center.y - r * 1.2f), Offset(center.x, center.y + r * 1.2f), seam.width)
            drawLine(BallSeamColor, Offset(center.x - r * 1.2f, center.y), Offset(center.x + r * 1.2f, center.y), seam.width)
            drawArc(
                color = BallSeamColor,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - r * 1.75f, center.y - r * 1.1f),
                size = Size(r * 1.4f, r * 2.2f),
                style = seam,
            )
            drawArc(
                color = BallSeamColor,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x + r * 0.35f, center.y - r * 1.1f),
                size = Size(r * 1.4f, r * 2.2f),
                style = seam,
            )
        }
        // Light from the top-left gives the ball some volume.
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent, Color.Black.copy(alpha = 0.25f)),
                center = Offset(center.x - r * 0.4f, center.y - r * 0.4f),
                radius = r * 1.8f,
            ),
            topLeft = bounds.topLeft,
            size = bounds.size,
        )
    }
}

@Preview
@Composable
private fun SplashSceneFlightPreview() {
    SplashScene(frame = splashFrame(FLIGHT_END_MILLIS / 2f))
}

@Preview
@Composable
private fun SplashSceneEndPreview() {
    SplashScene(frame = splashFrame(SPLASH_DURATION_MILLIS))
}
