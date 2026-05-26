package com.ninja.nova.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninja.nova.network.Task
import com.ninja.nova.ui.theme.*

@Composable
fun TasksWindow(tasks: List<Task>, onAdd: (String, String, String) -> Unit, onComplete: (String) -> Unit, onDelete: (String) -> Unit, onClose: () -> Unit, onMinimize: () -> Unit) {
    var showWindow by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(true) }
    var newTitle by remember { mutableStateOf("") }
    var newPriority by remember { mutableStateOf("medium") }
    if (!showWindow) return
    FloatingWindow(title = "Tasks", onClose = { showWindow = false; onClose() }, onMinimize = onMinimize, isExpanded = isExpanded, onExpandToggle = { isExpanded = !isExpanded }) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = newTitle, onValueChange = { newTitle = it },
                    placeholder = { Text("Add task...", color = NovaTextDim, fontSize = 12.sp) },
                    singleLine = true, modifier = Modifier.weight(1f).height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NovaAqua, unfocusedBorderColor = NovaSurface2, focusedTextColor = NovaText, unfocusedTextColor = NovaText, cursorColor = NovaAqua),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp))
                Button(onClick = { if (newTitle.isNotBlank()) { onAdd(newTitle, "", newPriority); newTitle = "" } },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaAqua, contentColor = NovaDark),
                    contentPadding = PaddingValues(horizontal = 12.dp)) { Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                listOf("low", "medium", "high").forEach { p ->
                    val color = when(p) { "high" -> NovaRed; "medium" -> NovaYellow; else -> NovaGreen }
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(modifier = Modifier.background(if (newPriority == p) color.copy(alpha = 0.3f) else NovaSurface2, RoundedCornerShape(6.dp))
                        .clickable(interactionSource = interactionSource, indication = null) { newPriority = p }
                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(p, color = if (newPriority == p) color else NovaTextDim, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = NovaSurface2)
            Spacer(Modifier.height(6.dp))
            if (tasks.isEmpty()) {
                Text("No tasks yet", color = NovaTextDim, fontSize = 12.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.heightIn(max = 200.dp)) {
                    items(tasks) { task ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(checked = task.status == "done", onCheckedChange = { if (task.status != "done") onComplete(task.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = NovaAqua, uncheckedColor = NovaTextDim), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text(task.title, color = if (task.status == "done") NovaTextDim else NovaText, fontSize = 13.sp,
                                        textDecoration = if (task.status == "done") TextDecoration.LineThrough else TextDecoration.None)
                                    val pColor = when(task.priority) { "high" -> NovaRed; "medium" -> NovaYellow; else -> NovaGreen }
                                    Text(task.priority, color = pColor, fontSize = 10.sp)
                                }
                            }
                            TextButton(onClick = { onDelete(task.id) }, contentPadding = PaddingValues(0.dp)) {
                                Text("x", color = NovaRed, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
