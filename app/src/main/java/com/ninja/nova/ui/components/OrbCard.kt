package com.ninja.nova.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninja.nova.ui.theme.*

@Composable
fun OrbCard(
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    agentStatus: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    val scale by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.9f else 0.97f,
        targetValue = if (isListening) 1.1f else 1.03f,
        animationSpec = infiniteRepeatable(
            tween(if (isListening) 600 else if (isSpeaking) 400 else 2000),
            RepeatMode.Reverse
        ),
        label = "orbScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isListening || isSpeaking) 0.6f else 0.3f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "glow"
    )

    val orbColor = when {
        isListening -> listOf(NovaGreen, NovaAqua, NovaDark2)
        isSpeaking -> listOf(NovaBlueLight, NovaAqua, NovaDark2)
        isThinking -> listOf(NovaYellow, NovaAqua, NovaDark2)
        else -> listOf(NovaAqua, NovaBlue, NovaDark2)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NovaSurface, RoundedCornerShape(20.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Glow behind orb
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale * 1.3f)
                        .blur(30.dp)
                        .background(
                            Brush.radialGradient(listOf(NovaAqua.copy(alpha = glowAlpha), Color.Transparent)),
                            CircleShape
                        )
                )
                // Main orb
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(orbColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isListening -> "..."
                            isSpeaking -> ")"
                            isThinking -> "~"
                            else -> "N"
                        },
                        color = NovaDark,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (agentStatus.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(agentStatus, color = NovaTextDim, fontSize = 12.sp)
            }
        }
    }
}
