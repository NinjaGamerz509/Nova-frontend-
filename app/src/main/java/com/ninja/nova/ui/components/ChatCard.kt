package com.ninja.nova.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninja.nova.network.Source
import com.ninja.nova.ui.theme.*
import com.ninja.nova.viewmodel.ChatMessage
import kotlinx.coroutines.launch

@Composable
fun ChatCard(messages: List<ChatMessage>, isLoading: Boolean, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) scope.launch { listState.animateScrollToItem(messages.size - 1) }
    }
    Box(modifier = modifier.fillMaxWidth().background(NovaSurface, RoundedCornerShape(20.dp)).padding(16.dp)) {
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("nova is ready. Say something.", color = NovaTextDim, fontSize = 14.sp)
            }
        } else {
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(messages) { msg -> MessageBubble(msg) }
                if (isLoading) { item { TypingIndicator() } }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier.widthIn(max = 260.dp)
                    .background(if (isUser) NovaAqua.copy(alpha = 0.2f) else NovaSurface2,
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp))
                    .padding(12.dp)
            ) { Text(msg.content, color = NovaText, fontSize = 14.sp, lineHeight = 20.sp) }
            if (msg.sources.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                msg.sources.take(3).forEach { source ->
                    Text("- ${source.title.take(40)}", color = NovaAqua.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "alpha"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { Box(modifier = Modifier.size(8.dp).background(NovaAqua.copy(alpha = alpha), RoundedCornerShape(50))) }
    }
}
