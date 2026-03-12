package com.upitracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.upitracker.app.data.model.Transaction
import com.upitracker.app.data.model.TransactionType
import com.upitracker.app.ui.theme.CreditGreen
import com.upitracker.app.ui.theme.DebitRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (transaction.type == TransactionType.CREDIT) CreditGreen.copy(0.15f) else DebitRed.copy(0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (transaction.type == TransactionType.CREDIT) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        null,
                        tint = if (transaction.type == TransactionType.CREDIT) CreditGreen else DebitRed
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(transaction.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(transaction.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                Text(dateFormatter.format(Date(transaction.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (transaction.type == TransactionType.CREDIT) "+" else "-"}${formatter.format(transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.CREDIT) CreditGreen else DebitRed,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (transaction.bankName.isNotEmpty()) {
                    Text(transaction.bankName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}
