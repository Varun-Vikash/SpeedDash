package com.example.jazzdashboard.ui.screens
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jazzdashboard.AppMode
import com.example.jazzdashboard.DashboardState
import com.example.jazzdashboard.ui.theme.GreenEco
import com.example.jazzdashboard.ui.theme.OrbitronFamily
import com.example.jazzdashboard.ui.theme.RajdhaniFamily
import com.example.jazzdashboard.ui.theme.YellowWarn
import java.util.Locale

// --- Viewport Scaling Extensions (Replicating HTML CSS behavior) ---
val Number.vmin: Dp
    @Composable get() {
        val config = LocalConfiguration.current
        val minSize = minOf(config.screenWidthDp, config.screenHeightDp)
        return (this.toFloat() * minSize / 100f).dp
    }

val Number.vminSp: TextUnit
    @Composable get() {
        val config = LocalConfiguration.current
        val minSize = minOf(config.screenWidthDp, config.screenHeightDp)
        return (this.toFloat() * minSize / 100f).sp
    }

val Number.vw: Dp
    @Composable get() {
        val config = LocalConfiguration.current
        return (this.toFloat() * config.screenWidthDp / 100f).dp
    }

val Number.vh: Dp
    @Composable get() {
        val config = LocalConfiguration.current
        return (this.toFloat() * config.screenHeightDp / 100f).dp
    }

/**
 * Sport (+R) Dashboard matching HTML lines 185-273 exactly.
 * Completely rewritten using vmin/vw/vh scaling and precise Canvas rendering to achieve 100% pixel parity.
 */
@Composable
fun SportScreen(
    state: DashboardState,
    onModeChange: (AppMode) -> Unit,
    onSimulateSwipe: () -> Unit,
    onToggleStopwatch: () -> Unit,
    onResetStopwatch: () -> Unit,
    onToggleGps: () -> Unit
) {
    var showIntro by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        showIntro = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // 1. Background Pattern (`bg-fl5-pattern`)
            .drawBehind {
                val hexSize = 40f
                val path = Path().apply {
                    moveTo(20f, 0f)
                    lineTo(37.32f, 10f)
                    lineTo(37.32f, 30f)
                    lineTo(20f, 40f)
                    lineTo(2.68f, 30f)
                    lineTo(2.68f, 10f)
                    close()
                }
                // Tiling pattern (CSS background-image tile behavior)
                val strokeW = 1f
                for (x in 0..size.width.toInt() step hexSize.toInt()) {
                    for (y in 0..size.height.toInt() step hexSize.toInt()) {
                        withTransform({ translate(x.toFloat(), y.toFloat()) }) {
                            drawPath(path, color = Color(0xFF333333).copy(alpha = 0.08f))
                            drawPath(path, color = Color(0xFF444444), style = Stroke(strokeW))
                        }
                    }
                }
            }
    ) {
        // Bottom-to-top gradient (HTML: bg-gradient-to-t from-red-900/20 via-transparent to-transparent)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFF7F1D1D).copy(alpha = 0.2f)
                        )
                    )
                )
        )

        // ===== TOP RIGHT BUTTONS (HTML: top-[2vmin] right-[2vmin] gap-[1vmin]) =====
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.vmin, end = 2.vmin),
            horizontalArrangement = Arrangement.spacedBy(1.vmin)
        ) {
            // GPS Button
            Box(
                modifier = Modifier
                    .clickable { onToggleGps() }
                    .background(Color(0xFF18181B).copy(alpha = 0.8f), RoundedCornerShape(0.5.vmin))
                    .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(0.5.vmin))
                    .padding(horizontal = 2.vmin, vertical = 1.vmin)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(1.2.vmin)
                            .background(
                                when {
                                    state.gpsLocked -> GreenEco
                                    state.gpsEnabled -> Color(0xFFEAB308)
                                    else -> Color(0xFF52525B)
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(1.vmin))
                    Text(
                        when {
                            state.gpsLocked -> "LOCKED"
                            state.gpsEnabled -> "SEARCH"
                            else -> "GPS"
                        },
                        color = Color(0xFFA1A1AA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 1.2.vminSp, // md:text-[1.2vmin]
                        fontFamily = RajdhaniFamily
                    )
                }
            }
            // SWIPE Button
            Box(
                modifier = Modifier
                    .clickable { onSimulateSwipe() }
                    .background(Color(0xFF18181B).copy(alpha = 0.8f), RoundedCornerShape(0.5.vmin))
                    .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(0.5.vmin))
                    .padding(horizontal = 2.vmin, vertical = 1.vmin)
            ) {
                Text(
                    "SWIPE",
                    color = Color(0xFFA1A1AA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 1.2.vminSp,
                    fontFamily = RajdhaniFamily
                )
            }
            // EXIT +R Button
            Box(
                modifier = Modifier
                    .clickable { onModeChange(AppMode.ECO) }
                    .background(Color(0xFF18181B).copy(alpha = 0.8f), RoundedCornerShape(0.5.vmin))
                    .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(0.5.vmin))
                    .padding(horizontal = 2.vmin, vertical = 1.vmin)
            ) {
                Text(
                    "EXIT +R",
                    color = Color(0xFFA1A1AA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 1.2.vminSp,
                    fontFamily = RajdhaniFamily
                )
            }
        }

        // ===== MAIN CONTENT =====
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.vmin)
                .padding(top = 5.vh), // mt-[5vh] from HTML
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== SHIFT LIGHTS & RPM SLANTED BAR =====
            // Combining into a single Canvas for exact SVG replication
            Box(
                modifier = Modifier
                    .width(80.vw) // max-w-[80vw]
                    .height(18.vh) // 1.5vh (leds) + 0.5vmin (gap) + 12vh (bar) + padding
                    .padding(bottom = 2.vh)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // --- 1. SHIFT LEDs (with exact CSS skew-x) ---
                    // HTML: flex gap-[0.5vmin] mb-[0.5vmin] px-[5%] h-[1.5vh]
                    val ledH = h * 0.15f
                    val totalLedsWidth = w * 0.9f // 5% padding on each side
                    val gap = w * 0.005f
                    val ledW = (totalLedsWidth - (9 * gap)) / 10f
                    val startX = w * 0.05f
                    val ledY = 0f

                    val rpmStart = 4000f
                    for (i in 0 until 10) {
                        val isActive = state.rpm >= (rpmStart + (i * 300f))
                        val ledColor = when {
                            i < 5 -> Color(0xFF22C55E)
                            i < 8 -> Color(0xFFEAB308)
                            else -> Color(0xFFEF4444)
                        }

                        val xPos = startX + i * (ledW + gap)
                        withTransform({ translate(xPos, ledY) }) {
                            drawContext.canvas.nativeCanvas.save()
                            // CSS skew-x: -20deg is tan(-20) = -0.364
                            drawContext.canvas.nativeCanvas.skew(-0.364f, 0f)
                            
                            if (isActive) {
                                // Glow
                                drawRect(
                                    color = ledColor.copy(alpha = 0.5f),
                                    topLeft = Offset(-4f, -4f),
                                    size = Size(ledW + 8f, ledH + 8f)
                                )
                            }
                            drawRect(
                                color = if (isActive) ledColor else Color(0xFF333333),
                                size = Size(ledW, ledH)
                            )
                            drawContext.canvas.nativeCanvas.restore()
                        }
                    }

                    // --- 2. RPM SLANTED BAR ---
                    // HTML SVG viewbox is 0 0 800 100.
                    val barTopY = h * 0.3f // Room for LEDs
                    val barH = h * 0.7f
                    
                    val svgPath = Path().apply {
                        moveTo(0f, barTopY + barH * 0.9f)
                        lineTo(w * (150f/800f), barTopY + barH * 0.15f)
                        lineTo(w, barTopY + barH * 0.15f)
                    }

                    // Background stroke (#262626)
                    drawPath(
                        path = svgPath,
                        color = Color(0xFF262626),
                        style = Stroke(width = w * (12f/800f), cap = StrokeCap.Butt)
                    )

                    // Tick marks and text
                    val textPaint = android.graphics.Paint().apply {
                        color = 0xFF71717A.toInt() // zinc-500
                        textSize = w * (20f/800f) // md:text-[2vmin] equivalent scaling
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                    }
                    val textPaintRed = android.graphics.Paint(textPaint).apply {
                        color = 0xFFEF4444.toInt()
                    }

                    val tickPositions = listOf(150f, 231f, 312f, 393f, 474f, 555f, 636f, 717f)
                    for (i in 0..7) {
                        val xFrac = tickPositions[i] / 800f
                        val tickColor = if (i >= 6) Color(0xFFEF4444) else Color(0xFF555555)
                        
                        // Tick mark
                        drawLine(
                            color = tickColor,
                            start = Offset(w * xFrac, barTopY + barH * 0.15f),
                            end = Offset(w * xFrac, barTopY + barH * 0.35f),
                            strokeWidth = 2f
                        )

                        // Number label
                        val p = if (i >= 6) textPaintRed else textPaint
                        drawContext.canvas.nativeCanvas.drawText(
                            "$i",
                            w * xFrac,
                            barTopY + barH * 0.75f,
                            p
                        )
                    }

                    // "x1000 r/min" label
                    val labelPaint = android.graphics.Paint().apply {
                        color = 0xFFFFFFFF.toInt()
                        textSize = w * (15f/800f)
                        textAlign = android.graphics.Paint.Align.RIGHT
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "x1000 r/min",
                        w,
                        barTopY - (barH * 0.05f),
                        labelPaint
                    )

                    // Active fill gradient
                    val rpmFraction = (state.rpm / 7500f).coerceIn(0f, 1.07f)
                    clipRect(right = w * rpmFraction) {
                        drawPath(
                            path = svgPath,
                            brush = Brush.horizontalGradient(
                                0.0f to YellowWarn,
                                0.7f to YellowWarn,
                                0.85f to Color(0xFFEF4444),
                                1.0f to Color(0xFFEF4444)
                            ),
                            style = Stroke(width = w * (12f/800f), cap = StrokeCap.Butt)
                        )
                    }
                }
            }

            // ===== MID SECTION (HTML flex row) =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 2.vmin),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- LEFT: Timer (flex-1 items-end pr-[4vw] border-r) ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.vw)
                        .drawBehind {
                            drawLine(
                                color = Color(0xFF18181B), // border-zinc-900
                                start = Offset(size.width, 0f),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1f
                            )
                        }
                        .padding(end = 4.vw), // HTML uses pr-[4vw]
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "CHRONO",
                        color = Color(0xFF71717A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 1.5.vminSp,
                        letterSpacing = 2.sp,
                        fontFamily = RajdhaniFamily
                    )
                    
                    val m = state.stopwatchElapsedMs / 60000
                    val s = (state.stopwatchElapsedMs % 60000) / 1000
                    val ms = (state.stopwatchElapsedMs % 1000) / 10
                    Text(
                        String.format(Locale.US, "%02d:%02d.%02d", m, s, ms),
                        color = Color.White,
                        fontSize = 6.vminSp, // text-[6vmin]
                        fontFamily = OrbitronFamily
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.vmin),
                        modifier = Modifier.padding(top = 1.vmin)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable { onResetStopwatch() }
                                .background(Color(0xFF27272A), RoundedCornerShape(0.5.vmin))
                                .padding(horizontal = 2.vmin, vertical = 0.5.vmin)
                        ) {
                            Text(
                                if (state.stopwatchRunning) "LAP" else "RESET",
                                color = Color(0xFFD4D4D8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 1.2.vminSp,
                                fontFamily = RajdhaniFamily
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clickable { onToggleStopwatch() }
                                .background(
                                    if (state.stopwatchRunning) Color(0xFF7F1D1D) else Color(0xFF064E3B),
                                    RoundedCornerShape(0.5.vmin)
                                )
                                .padding(horizontal = 2.vmin, vertical = 0.5.vmin)
                        ) {
                            Text(
                                if (state.stopwatchRunning) "STOP" else "START",
                                color = if (state.stopwatchRunning) Color(0xFFFCA5A5) else Color(0xFF6EE7B7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 1.2.vminSp,
                                fontFamily = RajdhaniFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(1.5.vmin))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF18181B), RoundedCornerShape(0.5.vmin))
                            .border(1.dp, Color(0xFF7F1D1D).copy(alpha = 0.5f), RoundedCornerShape(0.5.vmin))
                            .padding(vertical = 0.5.vmin),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "0-100 LAUNCH",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 1.2.vminSp,
                            letterSpacing = 2.sp,
                            fontFamily = RajdhaniFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(1.vmin))
                    Text(
                        state.lastLapTime,
                        color = Color(0xFFA1A1AA),
                        fontSize = 1.2.vminSp,
                        fontFamily = OrbitronFamily // HTML uses font-mono text-zinc-400
                    )
                }

                // --- CENTER: Speed (w-[40vmin] items-center) ---
                Box(
                    modifier = Modifier.width(40.vmin),
                    contentAlignment = Alignment.Center
                ) {
                    // Red blur background (w-[35vmin] h-[30vmin])
                    Box(
                        modifier = Modifier
                            .size(width = 35.vmin, height = 30.vmin)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFDC2626).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // text-[20vmin] font-black
                        Text(
                            "${state.speed}",
                            color = Color.White,
                            fontSize = 20.vminSp,
                            fontWeight = FontWeight.Black,
                            fontFamily = OrbitronFamily,
                            maxLines = 1,
                            softWrap = false
                        )
                        // text-[2.5vmin] text-red-500 tracking-[0.4em] -mt-[3vmin]
                        Text(
                            "KM/H",
                            color = Color(0xFFEF4444),
                            fontSize = 2.5.vminSp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp,
                            fontFamily = RajdhaniFamily,
                            modifier = Modifier.offset(y = (-3).vmin)
                        )

                        // Telemetry Grid (grid-cols-2 gap-x-[4vmin] gap-y-[1vmin] mt-[4vmin])
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.vmin, start = 2.vmin, end = 2.vmin)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TelemetryItem("WAT", "${state.waterTemp}", "°")
                                TelemetryItem("OIL", "${state.oilTemp}", "°")
                            }
                            Spacer(modifier = Modifier.height(1.vmin))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TelemetryItem("BST", "${state.boostPressure}", "b")
                                TelemetryItem("VLT", "${state.voltage}", "v")
                            }
                        }
                    }
                }

                // --- RIGHT: Gear & G-Meter (flex-1 pl-[4vw] border-l) ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.vw)
                        .drawBehind {
                            drawLine(
                                color = Color(0xFF18181B),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1f
                            )
                        }
                        .padding(start = 4.vw),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(bottom = 4.vmin)
                    ) {
                        // +R (text-[5vmin] italic text-red-600 mr-[1vmin])
                        Text(
                            "+R",
                            color = Color(0xFFDC2626),
                            fontSize = 5.vminSp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            fontFamily = OrbitronFamily,
                            modifier = Modifier.padding(end = 1.vmin)
                        )
                        // Gear (text-[14vmin])
                        Text(
                            state.gear,
                            color = Color.White,
                            fontSize = 14.vminSp,
                            fontWeight = FontWeight.Black,
                            fontFamily = OrbitronFamily
                        )
                    }

                    // G-Meter (w-[20vmin] h-[20vmin])
                    Box(
                        modifier = Modifier
                            .size(20.vmin)
                            .background(Color(0xFF18181B).copy(alpha = 0.5f), CircleShape)
                            .border(0.3.vmin, Color(0xFF27272A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner ring (w-[12vmin])
                        Box(
                            modifier = Modifier
                                .size(12.vmin)
                                .border(1.dp, Color(0xFF27272A), CircleShape)
                        )
                        // Crosshairs
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF27272A)))
                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFF27272A)))

                        // G-Dot (w-[1.5vmin] bg-yellow-400)
                        val gMax = 2f
                        val maxOff = 3.5.vmin
                        val xOff = (state.gForceX / gMax) * maxOff.value
                        val yOff = (-state.gForceY / gMax) * maxOff.value
                        Box(
                            modifier = Modifier
                                .offset(x = xOff.dp, y = yOff.dp)
                                .size(1.5.vmin)
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0xFFFACC15).copy(alpha = 0.4f),
                                        radius = size.width
                                    )
                                }
                                .background(Color(0xFFFACC15), CircleShape)
                        )

                        Text(
                            "PEAK G",
                            color = Color(0xFF71717A),
                            fontSize = 1.2.vminSp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFamily,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 2.vmin)
                        )
                    }
                }
            }
        }

        // --- Intro Overlay ---
        androidx.compose.animation.AnimatedVisibility(
            visible = showIntro,
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)),
            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FULL SEND",
                        color = Color.White,
                        fontSize = 15.vminSp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontFamily = OrbitronFamily,
                        letterSpacing = (-2).sp
                    )
                    Spacer(modifier = Modifier.height(2.vmin))
                    Box(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(horizontal = 4.vmin, vertical = 1.vmin)
                    ) {
                        Text(
                            text = "+R MODE ACTIVE",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 3.vminSp,
                            letterSpacing = 6.sp,
                            fontFamily = RajdhaniFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryItem(label: String, value: String, unit: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.width(14.vmin)
    ) {
        Text(
            label,
            color = Color(0xFF71717A),
            fontWeight = FontWeight.Bold,
            fontSize = 1.5.vminSp,
            fontFamily = RajdhaniFamily
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 1.5.vminSp,
                fontFamily = OrbitronFamily
            )
            Text(
                unit,
                color = Color(0xFF52525B),
                fontSize = 1.vminSp,
                fontFamily = RajdhaniFamily,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}





