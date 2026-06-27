package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * A beautiful custom 3D tilt Card that responds to touch gestures.
 * Rotates on X and Y axis when pressed or dragged.
 */
@Composable
fun ThreeDTiltCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glowColor: Color = Color(0xFF8B5CF6),
    accentColor: Color = Color(0xFF06B6D4),
    content: @Composable BoxScope.() -> Unit
) {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }

    val animatedRotationX by animateFloatAsState(
        targetValue = rotationX,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "rotX"
    )
    val animatedRotationY by animateFloatAsState(
        targetValue = rotationY,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "rotY"
    )
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 350f),
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationX = animatedRotationX
                this.rotationY = animatedRotationY
                this.scaleX = animatedScale
                this.scaleY = animatedScale
                this.cameraDistance = 16f
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        // Calculate tilt based on where the card was pressed
                        val width = size.width
                        val height = size.height
                        
                        val normalizedX = (offset.x / width - 0.5f) * 2f // -1 to 1
                        val normalizedY = (offset.y / height - 0.5f) * 2f // -1 to 1
                        
                        // Y press causes X rotation, X press causes Y rotation
                        rotationX = -normalizedY * 15f
                        rotationY = normalizedX * 15f
                        scale = 0.95f
                        
                        tryAwaitRelease()
                        
                        rotationX = 0f
                        rotationY = 0f
                        scale = 1f
                        if (onClick != null) {
                            onClick()
                        }
                    }
                )
            }
            .drawBehind {
                // Glow accent behind the card
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.35f), Color.Transparent),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.5f
                )
            }
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B).copy(alpha = 0.85f),
                        Color(0xFF0F172A).copy(alpha = 0.95f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = glowColor,
                spotColor = accentColor
            )
            .padding(1.dp) // border thickness
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.4f),
                        glowColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * A ultra-polished 3D Neumorphic/Elevated Button with a physically pressed state offset.
 */
@Composable
fun ThreeDButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "three_d_button",
    color: Color = Color(0xFF6D28D9), // Deep violet
    topColor: Color = Color(0xFF8B5CF6), // Bright violet
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val heightOffset by animateFloatAsState(
        targetValue = if (isPressed) 2f else 6f,
        animationSpec = spring(stiffness = 500f),
        label = "offset"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        }
                    )
                }
            }
            .padding(bottom = 6.dp) // space for 3D shadow bottom
    ) {
        // 3D Bottom Base Layer (Dark shadow part)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .offset(y = 6.dp)
                .background(
                    color = if (enabled) color.copy(alpha = 0.85f) else Color.Gray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        // Interactive top sliding layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .offset(y = (6.dp - heightOffset.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (enabled) {
                            listOf(topColor, color)
                        } else {
                            listOf(Color.LightGray, Color.Gray)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Checkout Credit Card 3D Component. Flippable around Y-axis when clicked.
 */
@Composable
fun ThreeDCheckoutCard(
    modifier: Modifier = Modifier,
    cardHolderName: String = "CLIENT ELIT",
    cardNumber: String = "•••• •••• •••• 2026",
    expiryDate: String = "12/30",
    cvv: String = "303",
    planPrice: String = "€4.99"
) {
    var flipped by remember { mutableStateOf(false) }
    val rotationY by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 120f),
        label = "flip"
    )

    Box(
        modifier = modifier
            .size(width = 320.dp, height = 200.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 14f
            }
            .clickable { flipped = !flipped }
            .shadow(24.dp, RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = if (flipped) {
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    } else {
                        listOf(Color(0xFF7C3AED), Color(0xFF0D9488)) // Violet to Teal neon gradient
                    }
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(1.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        if (rotationY <= 90f) {
            // FRONT OF CARD
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header (NFC & Logo)
                Box(modifier = Modifier.fillMaxWidth()) {
                    // NFC Chip
                    Box(
                        modifier = Modifier
                            .size(45.dp, 32.dp)
                            .background(Color(0xFFE2E8F0).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    )
                    
                    // Logo text
                    Text(
                        text = "AEROSTREAM 3D",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Card Number
                Text(
                    text = cardNumber,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(text = "MBALTËSI I KARTËS", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text(text = cardHolderName.uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                        Text(text = "SKADON", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text(text = expiryDate, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // BACK OF CARD (Rotated so it reads correctly when flipped)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.rotationY = 180f } // Counter-rotate content
                    .padding(vertical = 20.dp)
            ) {
                // Magnetic strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color.Black)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // CVV Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(35.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = cvv,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Signature description
                Text(
                    text = "Aktivizim i menjëhershëm i llogarisë Pro për $planPrice. Kliko për të kthyer.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}
