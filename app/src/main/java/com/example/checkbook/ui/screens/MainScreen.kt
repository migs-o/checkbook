package com.example.checkbook.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checkbook.data.entity.Transaction
import com.example.checkbook.data.entity.TransactionType
import com.example.checkbook.data.entity.PaymentMethod
import com.example.checkbook.ui.viewmodels.TransactionViewModel
import com.example.checkbook.ui.viewmodels.PaymentMethodViewModel
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    transactionViewModel: TransactionViewModel = hiltViewModel(),
    paymentMethodViewModel: PaymentMethodViewModel = hiltViewModel()
) {
    // Load payment methods when the screen is first displayed
    LaunchedEffect(Unit) {
        paymentMethodViewModel.loadPaymentMethods()
    }

    val transactions by transactionViewModel.transactions.collectAsState()
    val currentBalance by transactionViewModel.currentBalance.collectAsState()
    val paymentMethods by paymentMethodViewModel.paymentMethods.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Transaction?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Balance",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$${String.format("%.2f", currentBalance)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Transactions List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onDelete = { transactionViewModel.deleteTransaction(it) },
                    onEdit = { showEditDialog = it }
                )
            }
        }

        // Add Transaction Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            paymentMethods = paymentMethods,
            onDismiss = { showAddDialog = false },
            onAdd = { description, amount, type, paymentMethod, date ->
                transactionViewModel.addTransaction(
                    description = description,
                    amount = amount,
                    type = type,
                    paymentMethodId = paymentMethod.id,
                    date = date
                )
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { transaction ->
        AddTransactionDialog(
            paymentMethods = paymentMethods,
            onDismiss = { showEditDialog = null },
            onAdd = { description, amount, type, paymentMethod, date ->
                transactionViewModel.updateTransaction(
                    transaction.copy(
                        description = description,
                        amount = amount,
                        type = type,
                        paymentMethodId = paymentMethod.id,
                        date = date
                    )
                )
                showEditDialog = null
            },
            editingTransaction = transaction
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(transaction)
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onEdit(transaction) },
                onLongClick = { showDeleteConfirmation = true }
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = transaction.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.type == TransactionType.INCOME) 
                    Color(0xFF2E7D32) // Green for income
                else 
                    Color(0xFFD32F2F) // Red for expense
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    paymentMethods: List<com.example.checkbook.data.entity.PaymentMethod>,
    onDismiss: () -> Unit,
    onAdd: (String, Double, TransactionType, com.example.checkbook.data.entity.PaymentMethod, LocalDate) -> Unit,
    editingTransaction: Transaction? = null
) {
    var description by remember { mutableStateOf(editingTransaction?.description ?: "") }
    var amount by remember { mutableStateOf(editingTransaction?.amount?.toString() ?: "") }
    var selectedType by remember { mutableStateOf(editingTransaction?.type ?: TransactionType.EXPENSE) }
    var selectedPaymentMethod by remember { 
        mutableStateOf(
            paymentMethods.find { it.id == editingTransaction?.paymentMethodId } ?: paymentMethods.firstOrNull()
        ) 
    }
    var selectedDate by remember { mutableStateOf(editingTransaction?.date ?: LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingTransaction != null) "Edit Transaction" else "Add Transaction") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Amount field first
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                // Description field with auto-capitalization
                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        if (it.isEmpty()) {
                            description = it
                        } else {
                            description = it.replaceFirstChar { it.uppercase() }
                        }
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Date selection button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Date: ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}")
                }
                
                // Transaction type selection with custom colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (type) {
                                    TransactionType.INCOME -> Color(0xFF2E7D32) // Green
                                    TransactionType.EXPENSE -> Color(0xFFD32F2F) // Red
                                },
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                // Payment method dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMethod?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Payment Method") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isDropdownExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.name) },
                                onClick = { 
                                    selectedPaymentMethod = method
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: return@TextButton
                    selectedPaymentMethod?.let { method ->
                        onAdd(description, amountValue, selectedType, method, selectedDate)
                    }
                }
            ) {
                Text(if (editingTransaction != null) "Save" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = {
                    Text(
                        text = "Select Date",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    }
} 