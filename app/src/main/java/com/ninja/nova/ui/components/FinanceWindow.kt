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
import com.ninja.nova.network.Expense
import com.ninja.nova.ui.theme.*

@Composable
fun FinanceWindow(
    expenses: List<Expense>,
    onAdd: (String, Double, String) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit,
    onMinimize: () -> Unit
) {
    var showWindow by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(true) }
    var newTitle by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("other") }

    if (!showWindow) return

    FloatingWindow(
        title = "Finance",
        onClose = { showWindow = false; onClose() },
        onMinimize = onMinimize,
        isExpanded = isExpanded,
        onExpandToggle = { isExpanded = !isExpanded }
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    placeholder = { Text("Item...", color = NovaTextDim, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NovaAqua, unfocusedBorderColor = NovaSurface2, focusedTextColor = NovaText, unfocusedTextColor = NovaText, cursorColor = NovaAqua),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                )
                OutlinedTextField(
                    value = newAmount,
                    onValueChange = { newAmount = it },
                    placeholder = { Text("Amount", color = NovaTextDim, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.width(80.dp).height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NovaAqua, unfocusedBorderColor = NovaSurface2, focusedTextColor = NovaText, unfocusedTextColor = NovaText, cursorColor = NovaAqua),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("food", "travel", "shopping", "other").forEach { cat ->
                    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .background(if (newCategory == cat) NovaAqua.copy(alpha = 0.3f) else NovaSurface2, RoundedCornerShape(6.dp))
                            .clickable(interactionSource = interactionSource, indication = null) { newCategory = cat }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) { Text(cat, color = if (newCategory == cat) NovaAqua else NovaTextDim, fontSize = 10.sp) }
                }
            }
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = {
                    val amt = newAmount.toDoubleOrNull()
                    if (newTitle.isNotBlank() && amt != null) {
                        onAdd(newTitle, amt, newCategory)
                        newTitle = ""; newAmount = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NovaAqua, contentColor = NovaDark)
            ) { Text("Add Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = NovaSurface2)
            Spacer(Modifier.height(6.dp))

            val total = expenses.sumOf { it.amount }
            Text("Total: Rs. ${"%.0f".format(total)}", color = NovaYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))

            if (expenses.isEmpty()) {
                Text("No expenses yet", color = NovaTextDim, fontSize = 12.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.heightIn(max = 180.dp)) {
                    items(expenses) { expense ->
                        Row(modifier = Modifier.fillMaxWidth().background(NovaSurface2, RoundedCornerShape(8.dp)).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.title, color = NovaText, fontSize = 12.sp)
                                Text(expense.category, color = NovaTextDim, fontSize = 10.sp)
                            }
                            Text("Rs. ${"%.0f".format(expense.amount)}", color = NovaYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { onDelete(expense.id) }, contentPadding = PaddingValues(0.dp)) {
                                Text("x", color = NovaRed, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
