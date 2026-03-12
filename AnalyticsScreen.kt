package com.upitracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.upitracker.app.ui.theme.CreditGreen
import com.upitracker.app.ui.theme.DebitRed
import com.upitracker.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

val categoryColors = listOf(
    Color(0xFF4FC3F7), Color(0xFFAED581), Color(0xFFFFD54F),
    Color(0xFFFF8A65), Color(0xFFBA68C8), Color(0xFF4DB6AC),
    Color(0xFFF48FB1), Color(0xFF90A4AE)
)

@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val state by viewModel.dashboardState.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val total = state.categoryBreakdown.values.sum().takeIf { it > 0 } ?: 1.0

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("This Month", color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
        }

        // Overview cards
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Income",
                    value = formatter.format(state.totalCredit),
                    color = CreditGreen
                )
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Spent",
                    value = formatter.format(state.totalDebit),
                    color = DebitRed
                )
            }
        }

        // Spending breakdown
        if (state.categoryBreakdown.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Spending by Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        val sorted = state.categoryBreakdown.entries.sortedByDescending { it.value }
                        sorted.forEachIndexed { index, (category, amount) ->
                            val pct = amount / total
                            val color = categoryColors[index % categoryColors.size]
                            CategoryBar(category = category, amount = formatter.format(amount), percentage = pct, color = color)
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        } else {
            item {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No spending data yet", color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                }
            }
        }
    }
}

@Composable
fun OverviewCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun CategoryBar(category: String, amount: String, percentage: Double, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category, style = MaterialTheme.typography.bodyMedium)
            Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(
                Modifier
                    .fillMaxWidth(percentage.toFloat().coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Text("${(percentage * 100).toInt()}% of total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
    }
}
