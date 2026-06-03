package com.example.jazzdashboard.ui.screens
import com.example.jazzdashboard.media.MediaListenerService
import com.example.jazzdashboard.media.MediaState
import com.example.jazzdashboard.ui.components.OmegaWatchFace
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jazzdashboard.AppMode
import com.example.jazzdashboard.DashboardState
import com.example.jazzdashboard.ui.theme.GreenEco
import com.example.jazzdashboard.ui.theme.OrbitronFamily
import com.example.jazzdashboard.ui.theme.RajdhaniFamily

/**
 * Eco Dashboard matching HTML lines 94-183.
 * Layout: Sidebar | TopBar + Center(RPM | Speed+Gear | Watch/StopBtn) + BottomBar
 */
@Composable
fun EcoScreen(
    state: DashboardState,
    onModeChange: (AppMode) -> Unit,
    onToggleGps: () -> Unit,
    onStopEngine: () -> Unit
) {
    var showStopButton by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Tap background to dismiss stop button (HTML handleEcoBgClick)
                if (showStopButton) {
                    showStopButton = false
                }
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // ===== SIDEBAR (HTML lines 97-109) =====
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp)
                    .background(Color(0xFF09090B))
                    .border(width = 1.dp, color = Color(0xFF18181B))
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // H logo (HTML: font-black tracking-tighter italic text-zinc-500)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "H",
                        color = Color(0xFF71717A),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic // HTML has italic
                    )
                }

                // Map pin icon (zinc-500)
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF71717A), modifier = Modifier.size(22.dp))
                // Gauge icon (white - active)
                Icon(Icons.Default.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                // Divider
                Box(modifier = Modifier.width(28.dp).height(1.dp).background(Color(0xFF27272A)))
                // Settings icon
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF71717A), modifier = Modifier.size(22.dp))

                Spacer(modifier = Modifier.weight(1f))

                // Clock
                Text(
                    "12:00",
                    color = Color(0xFF52525B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = RajdhaniFamily
                )
            }

            // ===== MAIN CONTENT =====
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                // ===== TOP BAR (HTML lines 114-130) =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Location + Weather + GPS status
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF71717A),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                state.locationName.uppercase(),
                                color = Color(0xFFA1A1AA),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp,
                                fontSize = 11.sp,
                                fontFamily = RajdhaniFamily
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color(0xFFA1A1AA),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("--°", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(12.dp))
                            // Divider
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFF27272A)))
                            Spacer(modifier = Modifier.width(12.dp))
                            // GPS status dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        when {
                                            state.gpsLocked -> GreenEco
                                            state.gpsEnabled -> Color(0xFFEAB308) // yellow
                                            else -> Color(0xFF52525B) // zinc-600
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                when {
                                    state.gpsLocked -> "GPS LOCKED"
                                    state.gpsEnabled -> "SEARCHING..."
                                    else -> "NO GPS"
                                },
                                color = Color(0xFF71717A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = RajdhaniFamily
                            )
                        }
                    }

                    // Right: Drive/Sport toggle (HTML lines 126-129)
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        // Drive button (active)
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF064E3B).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "DRIVE",
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = RajdhaniFamily
                            )
                        }
                        // Sport button
                        Box(
                            modifier = Modifier
                                .clickable { onModeChange(AppMode.SPORT) }
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "SPORT",
                                color = Color(0xFF71717A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = RajdhaniFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ===== CENTER STAGE (HTML lines 132-180) =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- RPM Gauge (HTML lines 135-144) ---
                    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 10.dp.toPx()
                            // Background arc (stroke="#1f2937", 270° sweep)
                            drawArc(
                                color = Color(0xFF1F2937),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                            // Active arc (stroke="#10b981")
                            val rpmFraction = (state.rpm / 8000f).coerceIn(0f, 1f)
                            drawArc(
                                color = GreenEco,
                                startAngle = 135f,
                                sweepAngle = 270f * rpmFraction,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 20.dp)
                        ) {
                            Text(
                                String.format("%.1f", state.rpm / 1000f),
                                color = Color.White,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFamily
                            )
                            Text(
                                "x1000",
                                color = Color(0xFF52525B),
                                fontSize = 12.sp,
                                letterSpacing = 3.sp,
                                fontFamily = RajdhaniFamily
                            )
                        }
                    }

                    // --- Center Info: Speed + Gear + GPS button (HTML lines 147-164) ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(180.dp)
                    ) {
                        // Speed value (HTML: font-family: 'Orbitron', text-[14vmin])
                        Text(
                            text = "${state.speed}",
                            color = Color.White,
                            fontSize = 100.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = OrbitronFamily
                        )
                        Text(
                            "KM/H",
                            color = Color(0xFF71717A),
                            fontSize = 12.sp,
                            letterSpacing = 4.sp,
                            fontFamily = RajdhaniFamily
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gear indicator box
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFF18181B), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                state.gear,
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val mediaState by com.example.jazzdashboard.media.MediaListenerService.mediaState.collectAsState()
                        NowPlayingWidget(
                            state = mediaState,
                            onPlayPause = { com.example.jazzdashboard.media.MediaListenerService.playPause() },
                            onNext = { com.example.jazzdashboard.media.MediaListenerService.skipNext() },
                            onPrev = { com.example.jazzdashboard.media.MediaListenerService.skipPrevious() }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // GPS toggle button (HTML line 157-159)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleGps() }
                                .background(Color(0xFF18181B), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(4.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFA1A1AA),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (state.gpsEnabled) "STOP GPS" else "ENABLE GPS",
                                    color = Color(0xFFA1A1AA),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    fontSize = 10.sp,
                                    fontFamily = RajdhaniFamily
                                )
                            }
                        }

                        // Debug label (HTML line 161-163)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "MODE: AUTO",
                                color = Color(0xFF52525B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                fontFamily = RajdhaniFamily
                            )
                        }
                    }

                    // --- Watch / Stop Button (HTML lines 167-179) ---
                    Column(
                        modifier = Modifier.size(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Watch face (visible when stop button is hidden)
                        AnimatedVisibility(
                            visible = !showStopButton,
                            enter = fadeIn(tween(300)) + scaleIn(tween(300)),
                            exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.8f)
                        ) {
                            Box(
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    showStopButton = true
                                }
                            ) {
                                OmegaWatchFace(size = 200.dp, speedKmh = state.speed.toFloat())
                            }
                        }

                        // Stop button (visible when watch is hidden)
                        AnimatedVisibility(
                            visible = showStopButton,
                            enter = fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.5f),
                            exit = fadeOut(tween(300))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(Color(0xFFDC2626), Color(0xFF7F1D1D))
                                            )
                                        )
                                        .border(5.dp, Color(0xFF27272A), CircleShape)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            onStopEngine()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.fillMaxSize().border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape))
                                    Box(modifier = Modifier.fillMaxSize().padding(2.dp).border(1.dp, Color.Black.copy(alpha = 0.3f), CircleShape))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("ENGINE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 3.sp, fontFamily = RajdhaniFamily)
                                        Text("STOP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp, fontFamily = RajdhaniFamily)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(40.dp)
                                                .height(5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF87171))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "TAP BACKGROUND TO CANCEL",
                                    color = Color(0xFF52525B),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp,
                                    fontSize = 9.sp,
                                    fontFamily = RajdhaniFamily
                                )
                            }
                        }
                    }
                }

                // ===== BOTTOM GREEN BAR (HTML line 181) =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(GreenEco)
                )
            }
        }
    }
}

@Composable
fun NowPlayingWidget(
    state: com.example.jazzdashboard.media.MediaState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    var currentTimeMs by androidx.compose.runtime.remember { androidx.compose.runtime.mutableLongStateOf(0L) }

    androidx.compose.runtime.LaunchedEffect(state.positionMs, state.lastPositionUpdateTime, state.isPlaying, state.playbackSpeed) {
        if (state.isPlaying) {
            while (true) {
                val elapsed = android.os.SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
                currentTimeMs = state.positionMs + (elapsed * state.playbackSpeed).toLong()
                kotlinx.coroutines.delay(100)
            }
        } else {
            currentTimeMs = state.positionMs
        }
    }

    val activeLyric = state.lyrics.lastOrNull { it.timeMs <= currentTimeMs + 300 } // 300ms lookahead for smoothness
    val lyricText = activeLyric?.text

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art Placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF064E3B), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF047857), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    fontFamily = com.example.jazzdashboard.ui.theme.RajdhaniFamily
                )
                Text(
                    text = state.artist,
                    color = Color(0xFFA1A1AA),
                    fontSize = 10.sp,
                    maxLines = 1,
                    fontFamily = com.example.jazzdashboard.ui.theme.RajdhaniFamily
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color(0xFFA1A1AA),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onPrev() }
                )
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onPlayPause() }
                )
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color(0xFFA1A1AA),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onNext() }
                )
            }
        }

        // Live Lyrics
        androidx.compose.animation.AnimatedVisibility(visible = lyricText != null) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.Crossfade(
                        targetState = lyricText ?: "",
                        label = "LyricAnimation"
                    ) { text ->
                        Text(
                            text = text,
                            color = Color(0xFF10B981), // Emerald Green to match music icon
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1,
                            fontFamily = com.example.jazzdashboard.ui.theme.RajdhaniFamily
                        )
                    }
                }
            }
        }
    }
}





