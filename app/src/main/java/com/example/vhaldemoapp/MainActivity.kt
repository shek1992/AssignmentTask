package com.example.vhaldemoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vhaldemoapp.model.VehicleUiState
import com.example.vhaldemoapp.service.CarPropertyMonitoringService
import com.example.vhaldemoapp.ui.VhalViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, CarPropertyMonitoringService::class.java))
        enableEdgeToEdge()
        setContent {
            val darkColorScheme = darkColorScheme(
                primary = Color(0xFF00FF66),
                secondary = Color(0xFF00E5FF),
                background = Color(0xFF020305),
                surface = Color(0xFF10141C),
                surfaceVariant = Color(0xFF1B202C),
            )
            MaterialTheme(colorScheme = darkColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF020305)
                ) {
                    VhalDemoScreen()
                }
            }
        }
    }
}

@Composable
private fun VhalDemoScreen(viewModel: VhalViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020305))
    ) {
        // 1. TOP STATUS BAR
        TopStatusBar(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        // 2. MAIN CENTER CONTENT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 34.dp, bottom = 62.dp)
        ) {
            // LEFT PARKING TAB
            LeftParkingTab(
                isParkingActive = state.isParkingActive,
                onToggleParking = viewModel::toggleParking,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // TOP DECK PANEL (SONIC & RANGE CARD)
            TopDeckPanel(
                driveMode = state.driveMode,
                rangeKm = state.rangeKm,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            )

            // CENTER SPEEDOMETER & HULL DECK GRAPHIC & ODOMETER
            CenterSpeedAndHullDeck(
                speedKmh = state.speedKmh ?: 88f,
                odoKm = state.odoKm,
                modifier = Modifier.align(Alignment.Center)
            )

            // RIGHT SIDE STATUS STACK
            RightStatusStack(
                state = state,
                onToggleRapidBrake = viewModel::toggleRapidBrake,
                onToggleSuperHold = viewModel::toggleSuperHold,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            )
        }

        // 3. BOTTOM CONTROL DOCK
        BottomControlDock(
            state = state,
            onToggleLeftTurn = viewModel::toggleLeftTurnSignal,
            onToggleRightTurn = viewModel::toggleRightTurnSignal,
            onToggleHighBeam = viewModel::toggleHighBeam,
            onTogglePower = viewModel::toggleHvacPower,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        )

        // ERROR OVERLAY BANNER (if any)
        state.errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC7F1D1D))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⚠️ $error",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ==========================================
// 1. TOP STATUS BAR
// ==========================================
@Composable
private fun TopStatusBar(
    state: VehicleUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF090D14), Color(0x00020305))
                )
            )
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // LEFT STATUS ITEMS
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bluetooth Icon + Device Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ᛒ",
                    color = Color(0xFF2997FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = state.carInfo?.let { "${it.make} ${it.model}" } ?: "Pixel 9 Pro... (18%)",
                    color = Color(0xFFD0D4DC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Mask / Incognito Icon
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2430)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🕶️", fontSize = 9.sp)
            }

            // DND / Bell Icon
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2430)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⛔", fontSize = 9.sp)
            }
        }

        // CENTER STATUS PILLS & ORANGE LOGO
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Message Pill [ ✉ 4 ]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF161C26))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "✉", color = Color.White, fontSize = 9.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "4", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // Notification Pill [ 👤 2 ]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF161C26))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "👤", color = Color.White, fontSize = 9.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "2", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // Call Timer Red Pill [ 01:48 📞 ]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF991B1B))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "01:48", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "📞", color = Color.White, fontSize = 9.sp)
            }

            // Hazard Warning Badge
            Text(text = "⚠️", fontSize = 11.sp)

            // Signal Badge (Blue Circle)
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF007AFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "((•))", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }

            // Navigation Badge A (Orange Circle)
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5500)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "A", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // CENTER ORANGE HEXAGON LOGO
            Canvas(modifier = Modifier.size(22.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension / 2f

                val path = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r * 0.866f, cy - r * 0.5f)
                    lineTo(cx + r * 0.866f, cy + r * 0.5f)
                    lineTo(cx, cy + r)
                    lineTo(cx - r * 0.866f, cy + r * 0.5f)
                    lineTo(cx - r * 0.866f, cy - r * 0.5f)
                    close()
                }
                drawPath(path = path, color = Color(0xFFFF5500))

                // Bolt / S symbol in white inside
                val sPath = Path().apply {
                    moveTo(cx + r * 0.25f, cy - r * 0.45f)
                    lineTo(cx - r * 0.25f, cy - r * 0.1f)
                    lineTo(cx + r * 0.15f, cy)
                    lineTo(cx - r * 0.25f, cy + r * 0.45f)
                    lineTo(cx + r * 0.25f, cy + r * 0.1f)
                    lineTo(cx - r * 0.15f, cy)
                    close()
                }
                drawPath(path = sPath, color = Color.White)
            }
        }

        // RIGHT STATUS ITEMS
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Fri 24 Oct  10:22 PM",
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            // LTE Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2430))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "LTE",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Signal & Battery Bars
            Text(text = "📶", fontSize = 10.sp)
        }
    }
}

// ==========================================
// 2. TOP DECK PANEL (SONIC MODE & RANGE CARD)
// ==========================================
@Composable
private fun TopDeckPanel(
    driveMode: String,
    rangeKm: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(320.dp)
            .height(58.dp)
    ) {
        // Trapezoidal Card Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val bgPath = Path().apply {
                moveTo(w * 0.08f, 0f)
                lineTo(w * 0.92f, 0f)
                lineTo(w * 0.98f, h)
                lineTo(w * 0.02f, h)
                close()
            }

            drawPath(
                path = bgPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF232934), Color(0xFF0F1218))
                )
            )

            // Glowing Neon Green Top Bar
            val topBarPath = Path().apply {
                moveTo(w * 0.08f, 0f)
                lineTo(w * 0.92f, 0f)
            }
            drawPath(
                path = topBarPath,
                color = Color(0xFF00FF66),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SONIC Mode Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFD9381E))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = driveMode,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "⇄",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Range Value Display
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = rangeKm.toString(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "km",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Range",
                        color = Color(0xFF9E9E9E),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. CENTER SPEEDOMETER & HULL DECK GRAPHIC
// ==========================================
@Composable
private fun CenterSpeedAndHullDeck(
    speedKmh: Float,
    odoKm: Int,
    modifier: Modifier = Modifier
) {
    var displaySpeed by remember { mutableFloatStateOf(0f) }
    val safeTargetSpeed = speedKmh.coerceAtLeast(0f)

    LaunchedEffect(safeTargetSpeed) {
        while (abs(safeTargetSpeed - displaySpeed) >= 0.5f) {
            val step = if (safeTargetSpeed > displaySpeed) 1f else -1f
            displaySpeed = (displaySpeed + step).coerceAtLeast(0f)
            delay(15.milliseconds)
        }
        displaySpeed = safeTargetSpeed
    }

    val speedDisplayInt = max(0, abs(displaySpeed).roundToInt())

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Digital Speed Number
        Text(
            text = "$speedDisplayInt",
            fontSize = 78.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-2).sp
        )

        // Speed Unit
        Text(
            text = "k m / h",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8E99A8),
            letterSpacing = 1.5.sp,
            modifier = Modifier.offset(y = (-6).dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3D FUTURISTIC VEHICLE HULL DECK GRAPHIC (21, 23, 24, 25)
        FuturisticHullDeckCanvas(
            modifier = Modifier
                .width(540.dp)
                .height(130.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ODOMETER DISPLAY (22)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "<",
                color = Color(0xFF6B7685),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ODO  $odoKm km",
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = ">",
                color = Color(0xFF6B7685),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FuturisticHullDeckCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // 1. OUTER METALLIC WINGS (25)
        val outerWingPath = Path().apply {
            moveTo(cx - w * 0.48f, h * 0.65f)
            cubicTo(
                cx - w * 0.35f, h * 0.30f,
                cx - w * 0.20f, h * 0.20f,
                cx, h * 0.18f
            )
            cubicTo(
                cx + w * 0.20f, h * 0.20f,
                cx + w * 0.35f, h * 0.30f,
                cx + w * 0.48f, h * 0.65f
            )
            lineTo(cx + w * 0.42f, h * 0.75f)
            cubicTo(
                cx + w * 0.30f, h * 0.45f,
                cx + w * 0.18f, h * 0.38f,
                cx, h * 0.36f
            )
            cubicTo(
                cx - w * 0.18f, h * 0.38f,
                cx - w * 0.30f, h * 0.45f,
                cx - w * 0.42f, h * 0.75f
            )
            close()
        }

        drawPath(
            path = outerWingPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF2D3545), Color(0xFF121620))
            )
        )

        // 2. INNER HULL BODY ARCH
        val innerHullPath = Path().apply {
            moveTo(cx - w * 0.40f, h * 0.70f)
            cubicTo(
                cx - w * 0.28f, h * 0.42f,
                cx - w * 0.14f, h * 0.32f,
                cx, h * 0.30f
            )
            cubicTo(
                cx + w * 0.14f, h * 0.32f,
                cx + w * 0.28f, h * 0.42f,
                cx + w * 0.40f, h * 0.70f
            )
            lineTo(cx + w * 0.34f, h * 0.78f)
            cubicTo(
                cx + w * 0.22f, h * 0.55f,
                cx + w * 0.12f, h * 0.46f,
                cx, h * 0.44f
            )
            cubicTo(
                cx - w * 0.12f, h * 0.46f,
                cx - w * 0.22f, h * 0.55f,
                cx - w * 0.34f, h * 0.78f
            )
            close()
        }

        drawPath(
            path = innerHullPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1E232E), Color(0xFF090B10))
            )
        )

        // 3. CENTER RED LED GLOW & GRILLE ARCH (23)
        val redGrillePath = Path().apply {
            moveTo(cx - w * 0.32f, h * 0.78f)
            cubicTo(
                cx - w * 0.20f, h * 0.56f,
                cx - w * 0.10f, h * 0.48f,
                cx, h * 0.47f
            )
            cubicTo(
                cx + w * 0.10f, h * 0.48f,
                cx + w * 0.20f, h * 0.56f,
                cx + w * 0.32f, h * 0.78f
            )
            lineTo(cx + w * 0.30f, h * 0.86f)
            cubicTo(
                cx + w * 0.18f, h * 0.65f,
                cx + w * 0.09f, h * 0.58f,
                cx, h * 0.57f
            )
            cubicTo(
                cx - w * 0.09f, h * 0.58f,
                cx - w * 0.18f, h * 0.65f,
                cx - w * 0.30f, h * 0.86f
            )
            close()
        }

        // Red Glow Base
        drawPath(
            path = redGrillePath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFF1E27), Color(0xFFB91C1C))
            )
        )

        // Vertical Red LED Fin Rays inside grille
        val finCount = 42
        for (i in 0..finCount) {
            val t = i.toFloat() / finCount.toFloat()
            val finX = cx - w * 0.30f + (w * 0.60f * t)
            val distFromCenter = abs(t - 0.5f) * 2f
            val finYTop = h * (0.48f + 0.28f * distFromCenter * distFromCenter)
            val finYBot = finYTop + h * 0.09f

            drawLine(
                color = Color(0xFFFF5555),
                start = Offset(finX, finYTop),
                end = Offset(finX, finYBot),
                strokeWidth = 2.dp.toPx()
            )
        }

        // 4. CORNER AMBER LIGHT BLADES (21)
        drawArc(
            color = Color(0xFFFF4500),
            startAngle = 200f,
            sweepAngle = 30f,
            useCenter = false,
            topLeft = Offset(cx - w * 0.42f, h * 0.28f),
            size = Size(w * 0.22f, h * 0.30f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        drawArc(
            color = Color(0xFFFF4500),
            startAngle = 310f,
            sweepAngle = 30f,
            useCenter = false,
            topLeft = Offset(cx + w * 0.20f, h * 0.28f),
            size = Size(w * 0.22f, h * 0.30f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // 5. SIDE HEADLIGHT GLOW BEAMS (24)
        val leftGlowPath = Path().apply {
            moveTo(cx - w * 0.34f, h * 0.45f)
            lineTo(cx - w * 0.44f, h * 0.52f)
            lineTo(cx - w * 0.38f, h * 0.60f)
            close()
        }
        drawPath(
            path = leftGlowPath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF4D0), Color(0x00FFF4D0)),
                center = Offset(cx - w * 0.39f, h * 0.52f),
                radius = 40.dp.toPx()
            )
        )

        val rightGlowPath = Path().apply {
            moveTo(cx + w * 0.34f, h * 0.45f)
            lineTo(cx + w * 0.44f, h * 0.52f)
            lineTo(cx + w * 0.38f, h * 0.60f)
            close()
        }
        drawPath(
            path = rightGlowPath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF4D0), Color(0x00FFF4D0)),
                center = Offset(cx + w * 0.39f, h * 0.52f),
                radius = 40.dp.toPx()
            )
        )
    }
}

// ==========================================
// 4. RIGHT SIDE STATUS STACK
// ==========================================
@Composable
private fun RightStatusStack(
    state: VehicleUiState,
    onToggleRapidBrake: () -> Unit,
    onToggleSuperHold: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(185.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ) {
        // 1. RAPID BRAKE BADGE
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF14171E))
                .clickable { onToggleRapidBrake() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Red Lightning Logo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "⚡", color = Color(0xFFFF2D55), fontSize = 14.sp)
                Text(text = "⚡", color = Color(0xFFFF2D55), fontSize = 14.sp, modifier = Modifier.offset(y = (-6).dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Rapid",
                    color = if (state.isRapidBrakeActive) Color.White else Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Brake",
                    color = if (state.isRapidBrakeActive) Color.White else Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 2. SUPER HOLD BADGE
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF14171E))
                .clickable { onToggleSuperHold() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cyan Waves surrounding pill
            Text(
                text = "((",
                color = if (state.isSuperHoldActive) Color(0xFF00F5D4) else Color.DarkGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF003833))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SUPER",
                        color = Color(0xFF00F5D4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hold",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "))",
                color = if (state.isSuperHoldActive) Color(0xFF00F5D4) else Color.DarkGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. SPEED LIMIT EXCEEDED BADGE
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF14171E))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Speed Sign 80 km/h
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(width = 3.dp, color = Color(0xFFE53935), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.speedLimitKmh}",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "km/h",
                        color = Color.DarkGray,
                        fontSize = 5.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-2).dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Speed",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Exceeded",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. CRUISE CONTROL ACTIVE WIDGET
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF14171E))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Green Speedometer Icon
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⏲️", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Active",
                    color = Color(0xFF00FF66),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.cruiseSpeedKmh} km/h",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 5. LEFT PARKING TAB
// ==========================================
@Composable
private fun LeftParkingTab(
    isParkingActive: Boolean,
    onToggleParking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
            .background(Color(0xFF161A22))
            .border(
                width = 1.dp,
                color = Color(0xFF2B3240),
                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
            )
            .clickable { onToggleParking() }
            .padding(horizontal = 8.dp, vertical = 14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            "PARKING".forEach { char ->
                Text(
                    text = char.toString(),
                    color = if (isParkingActive) Color(0xFF00FF66) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "»",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// 6. BOTTOM CONTROL DOCK BAR
// ==========================================
@Composable
private fun BottomControlDock(
    state: VehicleUiState,
    onToggleLeftTurn: () -> Unit,
    onToggleRightTurn: () -> Unit,
    onToggleHighBeam: () -> Unit,
    onTogglePower: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Blink transition for turn signals
    val infiniteTransition = rememberInfiniteTransition(label = "turnSignalBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaAnim"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF0F131A))
            .border(width = 1.dp, color = Color(0xFF222936), shape = RoundedCornerShape(26.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. App Launcher Button
            DockIconButton(
                bgColor = Color(0xFF1E2430),
                content = { Text(text = "⊞", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            )

            // 2. Navigation Button
            DockIconButton(
                bgColor = Color(0xFF1E2430),
                content = { Text(text = "▲", color = Color.White, fontSize = 14.sp) }
            )

            Spacer(modifier = Modifier.width(4.dp))

            // 3. High Beam Button (Blue Headlight)
            DockIconButton(
                bgColor = Color(0xFF1E2430),
                onClick = onToggleHighBeam,
                content = {
                    Text(
                        text = "💡",
                        fontSize = 14.sp,
                        color = if (state.isHighBeamOn) Color(0xFF007AFF) else Color.Gray
                    )
                }
            )

            // 4. Suspension / Level Button (Yellow)
            DockIconButton(
                bgColor = Color(0xFF1E2430),
                content = { Text(text = "⬐", color = Color(0xFFFFCC00), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            )

            // 5. Left Turn Arrow Button (Green)
            DockIconButton(
                bgColor = Color(0xFF1E2430),
                onClick = onToggleLeftTurn,
                content = {
                    Text(
                        text = "⬅",
                        color = if (state.leftTurnSignal) Color(0xFF00FF66).copy(alpha = alpha) else Color(0xFF00FF66),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            // 6. Service / Wrench Warning (Orange Wrench)
            DockIconButton(
                bgColor = Color(0xFF1E2430),
                content = { Text(text = "🔧", color = Color(0xFFFF9500), fontSize = 14.sp) }
            )

            // 7. Traction Control Badge (TC inside circle)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2430))
                    .border(width = 1.5.dp, color = Color(0xFF00FF66), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TC",
                    color = Color(0xFF00FF66),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // 8. Right Turn Arrow Button (Green)
            DockIconButton(
                bgColor = Color(0xFF1E2430),
                onClick = onToggleRightTurn,
                content = {
                    Text(
                        text = "➡",
                        color = if (state.rightTurnSignal) Color(0xFF00FF66).copy(alpha = alpha) else Color(0xFF00FF66),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            // 9. Tire Pressure / Wheel Icon
            DockIconButton(
                bgColor = Color(0xFF1E2430),
                content = { Text(text = "⚙️", fontSize = 14.sp) }
            )

            Spacer(modifier = Modifier.width(4.dp))

            // 10. Battery Level Pill [ 🔋 100% ]
            val fuelPercent = state.fuelLevelPercent ?: 100f
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF00C853))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔋", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${fuelPercent.roundToInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // 11. System Power ON Pill Button [ (⚙) ON ]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (state.isHvacPowerOn) Color(0xFF00C853) else Color(0xFF334155))
                    .clickable { onTogglePower() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙️",
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (state.isHvacPowerOn) "ON" else "OFF",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun DockIconButton(
    bgColor: Color,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
