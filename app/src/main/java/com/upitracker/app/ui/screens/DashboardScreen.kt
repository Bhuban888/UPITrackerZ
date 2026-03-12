package com.upitracker.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.upitracker.app.data.model.TransactionType
import com.upitracker.app.ui.Screen
import com.upitracker.app.ui.components.PermissionRequestCard
import com.upitracker.app.ui.components.TransactionItem
import com.upitracker.app.ui.theme.CreditGreen
import com.upitracker.app.ui.theme.DebitRed
import com.upitracker.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, navController: NavController) {
    val state by viewModel.dashboardState.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "UPI Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "This Month's Summary",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        item { PermissionRequestCard() }

        // Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Net Balance", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    val net = state.totalCredit - state.totalDebit
                    Text(
                        formatter.format(net),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (net >= 0) CreditGreen else DebitRed
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryChip(
                            label = "Income",
                            amount = formatter.format(state.totalCredit),
                            color = CreditGreen,
                            icon = Icons.Default.TrendingUp
                        )
                        SummaryChip(
                            label = "Spent",
                            amount = formatter.format(state.totalDebit),
                            color = DebitRed,
                            icon = Icons.Default.TrendingDown
                        )
                    }
                }
            }
        }

        // Recent Transactions
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { navController.navigate(Screen.Transactions.route) }) {
                    Text("See All")
                }
            }
        }

        if (state.transactions.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.onBackground.copy(0.3f), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No transactions yet", color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                    }
                }
            }
        } else {
            items(state.transactions) { tx ->
                TransactionItem(transaction = tx, onDelete = { viewModel.deleteTransaction(tx) })
            }
        }
    }
}

@Composable
fun SummaryChip(label: String, amount: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f))
            Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}
