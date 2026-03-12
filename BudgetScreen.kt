package com.upitracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.upitracker.app.data.model.Budget
import com.upitracker.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

val CATEGORIES = listOf("Food & Dining", "Travel", "Shopping", "Entertainment", "Bills & Utilities", "Healthcare", "Education", "Uncategorized")

@Composable
fun BudgetScreen(viewModel: MainViewModel) {
    val state by viewModel.dashboardState.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    var showAddBudget by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddBudget = true }) {
                Icon(Icons.Default.Add, "Add Budget")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Budget", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("This Month", color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
            }

            if (state.budgets.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountBalance, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onBackground.copy(0.3f))
                            Spacer(Modifier.height(8.dp))
                            Text("No budgets set", color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                            Spacer(Modifier.height(4.dp))
                            Text("Tap + to add a budget", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(0.4f))
                        }
                    }
                }
            } else {
                items(state.budgets) { budget ->
                    val spent = state.categoryBreakdown[budget.category] ?: 0.0
                    val progress = (spent / budget.limit).toFloat().coerceIn(0f, 1f)
                    val overBudget = spent > budget.limit

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(budget.category, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                IconButton(onClick = { viewModel.deleteBudget(budget) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    "${formatter.format(spent)} spent",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "of ${formatter.format(budget.limit)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                )
                            }
                            if (overBudget) {
                                Spacer(Modifier.height(4.dp))
                                Text("⚠ Over budget by ${formatter.format(spent - budget.limit)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddBudget) {
        AddBudgetDialog(
            onDismiss = { showAddBudget = false },
            onAdd = { budget ->
                viewModel.upsertBudget(budget)
                showAddBudget = false
            }
        )
    }
}

@Composable
fun AddBudgetDialog(onDismiss: () -> Unit, onAdd: (Budget) -> Unit) {
    var selectedCategory by remember { mutableStateOf(CATEGORIES.first()) }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        CATEGORIES.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monthly Limit (₹)") },
                    leadingIcon = { Text("₹") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cal = Calendar.getInstance()
                onAdd(Budget(selectedCategory, amount.toDoubleOrNull() ?: 0.0, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)))
            }, enabled = amount.isNotEmpty()) {
                Text("Set Budget")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
