package com.example.checkbook.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkbook.data.Transaction
import com.example.checkbook.data.TransactionType
import com.example.checkbook.data.PaymentMethod
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    paymentMethods: List<PaymentMethod>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val threshold = with(density) { 150.dp.toPx() }
    
    key(transaction.id) {
        var isVisible by remember { mutableStateOf(true) }
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                if (dismissValue != SwipeToDismissBoxValue.Settled) {
                    isVisible = false
                    true
                } else {
                    false
                }
            },
            positionalThreshold = { threshold }
        )

        AnimatedVisibility(
            visible = isVisible,
            exit = fadeOut(
                animationSpec = tween(durationMillis = 300)
            ) + shrinkHorizontally(
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.error)
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    },
                    content = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clickable { onEdit(transaction) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 0.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = transaction.description,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                                .format(transaction.date),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        transaction.paymentMethodId?.let { id ->
                                            paymentMethods.find { it.id.toLong() == id }?.let { paymentMethod ->
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = paymentMethod.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = NumberFormat.getCurrencyInstance().format(transaction.amount),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = when (transaction.type) {
                                            TransactionType.INCOME -> Color.Green
                                            TransactionType.EXPENSE -> Color.Red
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        LaunchedEffect(isVisible) {
            if (!isVisible) {
                kotlinx.coroutines.delay(300)
                onDelete(transaction)
            }
        }
    }
} 