package com.ninja.nova.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninja.nova.network.Note
import com.ninja.nova.ui.theme.*

@Composable
fun NotesWindow(
    notes: List<Note>,
    onAdd: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit,
    onMinimize: () -> Unit
) {
    var showWindow by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(true) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }

    if (!showWindow) return

    FloatingWindow(
        title = "Notes",
        onClose = { showWindow = false; onClose() },
        onMinimize = onMinimize,
        isExpanded = isExpanded,
        onExpandToggle = { isExpanded = !isExpanded }
    ) {
        Column {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                placeholder = { Text("Title...", color = NovaTextDim, fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NovaAqua, unfocusedBorderColor = NovaSurface2, focusedTextColor = NovaText, unfocusedTextColor = NovaText, cursorColor = NovaAqua),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = newContent,
                onValueChange = { newContent = it },
                placeholder = { Text("Note content...", color = NovaTextDim, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NovaAqua, unfocusedBorderColor = NovaSurface2, focusedTextColor = NovaText, unfocusedTextColor = NovaText, cursorColor = NovaAqua),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
            )
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { if (newContent.isNotBlank()) { onAdd(newTitle, newContent); newTitle = ""; newContent = "" } },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NovaAqua, contentColor = NovaDark)
            ) { Text("Add Note", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = NovaSurface2)
            Spacer(Modifier.height(6.dp))

            if (notes.isEmpty()) {
                Text("No notes yet", color = NovaTextDim, fontSize = 12.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.heightIn(max = 200.dp)) {
                    items(notes) { note ->
                        Box(modifier = Modifier.fillMaxWidth().background(NovaSurface2, RoundedCornerShape(8.dp)).padding(8.dp)) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(note.title, color = NovaAqua, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { onDelete(note.id) }, contentPadding = PaddingValues(0.dp)) {
                                        Text("x", color = NovaRed, fontSize = 14.sp)
                                    }
                                }
                                Text(note.content.take(100), color = NovaText, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
