package com.example.checkbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import com.example.checkbook.data.Transaction
import com.example.checkbook.data.TransactionType
import com.example.checkbook.ui.TransactionUiState
import com.example.checkbook.ui.TransactionViewModel
import com.example.checkbook.ui.components.TransactionItem
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val currentBalance by viewModel.currentBalance.collectAsState(initial = 0.0)
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.DEPOSIT) }
    var isAmountError by remember { mutableStateOf(false) }
    var shouldShake by remember { mutableStateOf(false) }

    val offsetX by animateFloatAsState(
        targetValue = if (shouldShake) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { shouldShake = false }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkbook") },
                actions = {
                    Text(
                        text = "Balance: $%.2f".format(currentBalance),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = uiState) {
                is TransactionUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TransactionUiState.Error -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.clearError() },
                        title = { Text("Error") },
                        text = { Text(currentState.message) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("OK")
                            }
                        }
                    )
                }
                is TransactionUiState.Success -> {
                    LazyColumn {
                        items(transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onDelete = { viewModel.deleteTransaction(transaction) },
                                onEdit = {
                                    editingTransaction = transaction
                                    amount = transaction.amount.toString()
                                    description = transaction.description
                                    selectedType = transaction.type
                                    showEditDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                isAmountError = false
            },
            title = { Text("Add Transaction") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it
                            isAmountError = it.toDoubleOrNull() == null && it.isNotEmpty()
                        },
                        modifier = Modifier.graphicsLayer {
                            translationY = if (shouldShake) offsetX * 20 else 0f
                        },
                        label = { Text("Amount") },
                        singleLine = true,
                        isError = isAmountError,
                        supportingText = if (isAmountError) {
                            { Text("Please enter a valid number") }
                        } else null
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        singleLine = true
                    )
                    TransactionType.values().forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(type.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        amount.toDoubleOrNull()?.let { amountValue ->
                            viewModel.addTransaction(
                                amount = amountValue,
                                description = description,
                                type = selectedType
                            )
                            showAddDialog = false
                            amount = ""
                            description = ""
                            selectedType = TransactionType.DEPOSIT
                            isAmountError = false
                        } ?: run {
                            isAmountError = true
                            shouldShake = true
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddDialog = false
                        amount = ""
                        description = ""
                        selectedType = TransactionType.DEPOSIT
                        isAmountError = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog && editingTransaction != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                editingTransaction = null
                isAmountError = false
            },
            title = { Text("Edit Transaction") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it
                            isAmountError = it.toDoubleOrNull() == null && it.isNotEmpty()
                        },
                        modifier = Modifier.graphicsLayer {
                            translationY = if (shouldShake) offsetX * 20 else 0f
                        },
                        label = { Text("Amount") },
                        singleLine = true,
                        isError = isAmountError,
                        supportingText = if (isAmountError) {
                            { Text("Please enter a valid number") }
                        } else null
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        singleLine = true
                    )
                    TransactionType.values().forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(type.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        amount.toDoubleOrNull()?.let { amountValue ->
                            editingTransaction?.let { transaction ->
                                viewModel.updateTransaction(
                                    transaction.copy(
                                        amount = amountValue,
                                        description = description,
                                        type = selectedType
                                    )
                                )
                                showEditDialog = false
                                editingTransaction = null
                                amount = ""
                                description = ""
                                selectedType = TransactionType.DEPOSIT
                                isAmountError = false
                            }
                        } ?: run {
                            isAmountError = true
                            shouldShake = true
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        editingTransaction = null
                        amount = ""
                        description = ""
                        selectedType = TransactionType.DEPOSIT
                        isAmountError = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 