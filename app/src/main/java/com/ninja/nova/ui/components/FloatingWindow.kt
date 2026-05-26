package com.ninja.nova.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninja.nova.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun FloatingWindow(
    title: String,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    isExpanded: Boolean = true,
    onExpandToggle: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var offset by remember { mutableStateOf(Offset(40f, 200f)) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .width(320.dp)
            .background(NovaSurface, RoundedCornerShape(14.dp))
            .border(1.dp, NovaAqua.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
    ) {
        Column {
            // Title bar - draggable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NovaSurface2, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Traffic lights
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(12.dp).background(NovaRed, CircleShape).clickable { onClose() })
                    Box(Modifier.size(12.dp).background(NovaYellow, CircleShape).clickable { onMinimize() })
                    Box(Modifier.size(12.dp).background(NovaGreen, CircleShape).clickable { onExpandToggle() })
                }

                Text(title, color = NovaText, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                // Expand button
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(NovaSurface, RoundedCornerShape(4.dp))
                        .clickable { onExpandToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isExpanded) "-" else "+", color = NovaTextDim, fontSize = 12.sp)
                }
            }

            // Drag handle line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(NovaAqua.copy(alpha = 0.3f))
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)
                        }
                    }
            )

            // Content
            if (isExpanded) {
                Box(modifier = Modifier.padding(12.dp)) {
                    content()
                }
            }
        }
    }
}
