package com.example.jazzdashboard.ui.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared Omega Speedmaster watch face composable.
 * Faithfully reproduces the SVG from getOmegaWatchSVG() in dashboard.html (lines 318-351).
 */
@Composable
fun OmegaWatchFace(
    size: Dp = 220.dp,
    speedKmh: Float = 0f
) {
    val calendar = remember { Calendar.getInstance() }
    calendar.timeInMillis = System.currentTimeMillis()

    val hours = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)
    val millis = calendar.get(Calendar.MILLISECOND)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF141414))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val r = cx // radius = half of canvas

            // Outer ring (stroke="#050505" stroke-width="20" on r=98)
            drawCircle(
                color = Color(0xFF050505),
                radius = r * 0.98f,
                center = Offset(cx, cy),
                style = Stroke(width = r * 0.20f)
            )

            // Outer bezel numbers (60, 65, 75, 90, 120, 180, 300, 500)
            val bezelPaint = android.graphics.Paint().apply {
                color = 0xFFCCCCCC.toInt()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = r * 0.06f
                isFakeBoldText = true
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.apply {
                drawText("60", cx, cy - r * 0.88f, bezelPaint)
                drawText("75", cx + r * 0.75f, cy - r * 0.45f, bezelPaint)
                drawText("90", cx + r * 0.88f, cy + r * 0.02f, bezelPaint)
                drawText("120", cx, cy + r * 0.92f, bezelPaint)
                drawText("180", cx - r * 0.75f, cy + r * 0.50f, bezelPaint)
                drawText("300", cx - r * 0.88f, cy + r * 0.02f, bezelPaint)
                drawText("500", cx - r * 0.75f, cy - r * 0.55f, bezelPaint)
            }

            // Inner dial face (r=78, fill="#18181b")
            drawCircle(color = Color(0xFF18181B), radius = r * 0.78f, center = Offset(cx, cy))

            // Inner ring (r=76, stroke="#555")
            drawCircle(
                color = Color(0xFF555555),
                radius = r * 0.76f,
                center = Offset(cx, cy),
                style = Stroke(width = r * 0.005f)
            )

            // Hour markers (12 tick marks)
            for (deg in 0 until 360 step 30) {
                rotate(deg.toFloat(), Offset(cx, cy)) {
                    // White tick
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(cx - r * 0.015f, cy - r * 0.72f),
                        size = androidx.compose.ui.geometry.Size(r * 0.03f, r * 0.12f)
                    )
                    // Green lume dot
                    drawCircle(
                        color = Color(0xFFBBF7D0),
                        radius = r * 0.01f,
                        center = Offset(cx, cy - r * 0.57f)
                    )
                }
            }

            // "Ω" symbol
            val omegaPaint = android.graphics.Paint().apply {
                color = 0xFFCCCCCC.toInt()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = r * 0.08f
                isFakeBoldText = true
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText("Ω", cx, cy - r * 0.40f, omegaPaint)

            // "OMEGA" text
            val omegaTextPaint = android.graphics.Paint().apply {
                color = 0xFFCCCCCC.toInt()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = r * 0.04f
                isFakeBoldText = true
                letterSpacing = 0.05f
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText("OMEGA", cx, cy - r * 0.34f, omegaTextPaint)

            // "Speedmaster" text
            val speedmasterPaint = android.graphics.Paint().apply {
                color = 0xFFDDDDDD.toInt()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = r * 0.04f
                isAntiAlias = true
                textSkewX = -0.15f // italic
            }
            drawContext.canvas.nativeCanvas.drawText("Speedmaster", cx, cy - r * 0.28f, speedmasterPaint)

            // "PROFESSIONAL" text
            val profPaint = android.graphics.Paint().apply {
                color = 0xFFDDDDDD.toInt()
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = r * 0.03f
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText("PROFESSIONAL", cx, cy - r * 0.24f, profPaint)

            // Sub-dials
            drawSubDial(cx - r * 0.38f, cy, r * 0.21f, seconds * 6f + millis * 0.006f) // seconds sub-dial (left)
            drawSubDial(cx + r * 0.38f, cy, r * 0.21f, (minutes % 30) * 12f) // minutes sub-dial (right)
            drawSubDial(cx, cy + r * 0.38f, r * 0.21f, (hours % 12) * 30f + minutes * 0.5f) // hours sub-dial (bottom)

            // Main hour hand
            val hourAngle = (hours % 12) * 30f + minutes * 0.5f
            drawMainHand(cx, cy, r, hourAngle, r * 0.62f, r * 0.025f, Color.White, Color(0xFFBBF7D0))

            // Main minute hand
            val minAngle = minutes * 6f + seconds * 0.1f
            drawMainHand(cx, cy, r, minAngle, r * 0.82f, r * 0.02f, Color.White, Color(0xFFBBF7D0))

            // Chrono hand (speed-linked in eco, static in intro)
            val chronoDeg = (speedKmh * 2.25f).coerceAtMost(360f + (speedKmh % 10))
            rotate(chronoDeg, Offset(cx, cy)) {
                // Tail
                drawLine(Color.White, Offset(cx, cy + r * 0.25f), Offset(cx, cy - r * 0.95f), strokeWidth = r * 0.006f)
                // Arrow tip
                val arrowPath = Path().apply {
                    moveTo(cx - r * 0.03f, cy - r * 0.70f)
                    lineTo(cx + r * 0.03f, cy - r * 0.70f)
                    lineTo(cx, cy - r * 0.85f)
                    close()
                }
                drawPath(arrowPath, Color.White)
                // Center dot
                drawCircle(Color.White, r * 0.025f, Offset(cx, cy))
            }
        }

        // Gradient glass overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

private fun DrawScope.drawSubDial(cx: Float, cy: Float, radius: Float, handAngle: Float) {
    // Sub-dial background
    drawCircle(Color(0xFF111111), radius, Offset(cx, cy))
    drawCircle(Color(0xFF333333), radius, Offset(cx, cy), style = Stroke(width = 0.5f))

    // Top tick mark
    drawLine(Color.White, Offset(cx, cy - radius + 3f), Offset(cx, cy - radius), strokeWidth = 2f)

    // Sub-dial hand
    rotate(handAngle, Offset(cx, cy)) {
        drawLine(Color.White, Offset(cx, cy + radius * 0.25f), Offset(cx, cy - radius * 0.85f), strokeWidth = 2f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawMainHand(cx: Float, cy: Float, r: Float, angle: Float, length: Float, width: Float, color: Color, lumeColor: Color) {
    rotate(angle, Offset(cx, cy)) {
        // Main hand shape
        val handPath = Path().apply {
            moveTo(cx - width, cy)
            lineTo(cx - width * 0.6f, cy - length * 0.88f)
            lineTo(cx, cy - length)
            lineTo(cx + width * 0.6f, cy - length * 0.88f)
            lineTo(cx + width, cy)
            close()
        }
        drawPath(handPath, color)

        // Lume strip
        drawRect(
            lumeColor,
            topLeft = Offset(cx - width * 0.3f, cy - length * 0.8f),
            size = androidx.compose.ui.geometry.Size(width * 0.6f, length * 0.65f)
        )
    }
}

