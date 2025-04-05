package com.example.checkbook.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.checkbook.data.Transaction
import com.example.checkbook.data.TransactionType
import com.example.checkbook.data.PaymentMethod
import com.example.checkbook.ui.viewmodels.TransactionViewModel
import com.example.checkbook.ui.viewmodels.UiState
import com.example.checkbook.ui.components.TransactionItem
import java.util.*
import java.text.NumberFormat
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TransactionViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    var selectedPaymentMethodId by remember { mutableStateOf<Int?>(null) }
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

    LaunchedEffect(editingTransaction) {
        if (editingTransaction != null) {
            amount = editingTransaction!!.amount.toString()
            description = editingTransaction!!.description
            selectedType = editingTransaction!!.type
            selectedPaymentMethodId = editingTransaction!!.paymentMethodId
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkbook") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Balance Card
            when (val balanceState = viewModel.balanceState.collectAsState().value) {
                is UiState.Loading -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is UiState.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Current Balance",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance().format(balanceState.data),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Error loading balance: ${balanceState.message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Transactions List
            when (val transactionsState = viewModel.transactionsState.collectAsState().value) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    LazyColumn {
                        items(transactionsState.data) { transaction ->
                            val paymentMethods = when (val paymentMethodsState = viewModel.paymentMethodsState.collectAsState().value) {
                                is UiState.Success -> paymentMethodsState.data
                                else -> emptyList()
                            }
                            TransactionItem(
                                transaction = transaction,
                                onEdit = { editingTransaction = it },
                                onDelete = { viewModel.delete(it) },
                                paymentMethods = paymentMethods
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading transactions: ${transactionsState.message}",
                            color = MaterialTheme.colorScheme.error
                        )
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
                selectedPaymentMethodId = null
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
                            translationX = if (shouldShake) offsetX * 20 else 0f
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionType.values().forEach { type ->
                            FilledTonalButton(
                                onClick = { selectedType = type },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (selectedType == type) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            ) {
                                Text(
                                    text = type.name,
                                    color = if (selectedType == type) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                    
                    // Payment Method Selection
                    when (val paymentMethodsState = viewModel.paymentMethodsState.collectAsState().value) {
                        is UiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        is UiState.Success -> {
                            if (paymentMethodsState.data.isNotEmpty()) {
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = paymentMethodsState.data.find { it.id == selectedPaymentMethodId }?.name ?: "",
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Payment Method") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        paymentMethodsState.data.forEach { paymentMethod ->
                                            DropdownMenuItem(
                                                text = { Text(paymentMethod.name) },
                                                onClick = {
                                                    selectedPaymentMethodId = paymentMethod.id
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Text(
                                text = "Error loading payment methods",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue != null) {
                            viewModel.insert(Transaction(
                                amount = amountValue,
                                description = description,
                                type = selectedType,
                                date = Date(),
                                paymentMethodId = selectedPaymentMethodId
                            ))
                            showAddDialog = false
                            amount = ""
                            description = ""
                            selectedType = TransactionType.INCOME
                            selectedPaymentMethodId = null
                        } else {
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
                        selectedType = TransactionType.INCOME
                        selectedPaymentMethodId = null
                        isAmountError = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    editingTransaction?.let { transaction ->
        AlertDialog(
            onDismissRequest = {
                editingTransaction = null
                isAmountError = false
                selectedPaymentMethodId = null
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
                            translationX = if (shouldShake) offsetX * 20 else 0f
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionType.values().forEach { type ->
                            FilledTonalButton(
                                onClick = { selectedType = type },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (selectedType == type) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            ) {
                                Text(
                                    text = type.name,
                                    color = if (selectedType == type) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                    
                    // Payment Method Selection
                    when (val paymentMethodsState = viewModel.paymentMethodsState.collectAsState().value) {
                        is UiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        is UiState.Success -> {
                            if (paymentMethodsState.data.isNotEmpty()) {
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = paymentMethodsState.data.find { it.id == selectedPaymentMethodId }?.name ?: "",
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Payment Method") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        paymentMethodsState.data.forEach { paymentMethod ->
                                            DropdownMenuItem(
                                                text = { Text(paymentMethod.name) },
                                                onClick = {
                                                    selectedPaymentMethodId = paymentMethod.id
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Text(
                                text = "Error loading payment methods",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue != null) {
                            viewModel.update(transaction.copy(
                                amount = amountValue,
                                description = description,
                                type = selectedType,
                                paymentMethodId = selectedPaymentMethodId
                            ))
                            editingTransaction = null
                            amount = ""
                            description = ""
                            selectedType = TransactionType.INCOME
                            selectedPaymentMethodId = null
                        } else {
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
                        editingTransaction = null
                        amount = ""
                        description = ""
                        selectedType = TransactionType.INCOME
                        selectedPaymentMethodId = null
                        isAmountError = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 