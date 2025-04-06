package com.example.checkbook.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.checkbook.data.Transaction
import com.example.checkbook.data.TransactionType
import com.example.checkbook.data.PaymentMethod
import com.example.checkbook.ui.TransactionViewModel
import com.example.checkbook.ui.PaymentMethodViewModel
import com.example.checkbook.ui.components.TransactionItem
import com.example.checkbook.ui.viewmodels.UiState
import java.util.*
import java.text.NumberFormat
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TransactionViewModel,
    paymentMethodViewModel: PaymentMethodViewModel,
    onNavigateToSettings: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedPaymentMethodId by remember { mutableStateOf<Long?>(null) }
    var isAmountError by remember { mutableStateOf(false) }
    var shouldShake by remember { mutableStateOf(false) }
    var showDescriptionDropdown by remember { mutableStateOf(false) }
    
    val uniqueDescriptions by viewModel.uniqueDescriptions.collectAsState(initial = emptyList())
    val currentBalance by viewModel.currentBalance.collectAsState(initial = 0.0)
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val paymentMethods by paymentMethodViewModel.allActivePaymentMethods.collectAsState(initial = emptyList())

    val amountFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val filteredDescriptions = remember(description, uniqueDescriptions) {
        if (description.isNotEmpty()) {
            uniqueDescriptions.filter { it.contains(description, ignoreCase = true) }
        } else {
            emptyList()
        }
    }

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

    LaunchedEffect(showAddDialog) {
        if (showAddDialog) {
            delay(100) // Short delay to ensure the dialog is ready
            amountFocusRequester.requestFocus()
        }
    }

    fun addTransaction() {
        val amountValue = amount.toDoubleOrNull()
        if (amountValue != null && description.isNotBlank()) {
            viewModel.addTransaction(
                amount = amountValue,
                description = description,
                type = selectedType,
                paymentMethodId = selectedPaymentMethodId ?: 0L
            )
            amount = ""
            description = ""
            selectedType = TransactionType.EXPENSE
            selectedPaymentMethodId = null
            showAddDialog = false
        } else {
            isAmountError = true
            shouldShake = true
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
                        text = NumberFormat.getCurrencyInstance().format(currentBalance),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            // Transactions List
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onEdit = { editingTransaction = it },
                        onDelete = { viewModel.deleteTransaction(it) },
                        paymentMethods = paymentMethods
                    )
                }
            }
        }
    }

    if (showAddDialog || editingTransaction != null) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingTransaction = null
                amount = ""
                description = ""
                selectedType = TransactionType.EXPENSE
                selectedPaymentMethodId = null
            },
            title = { Text(if (editingTransaction != null) "Edit Transaction" else "Add Transaction") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                descriptionFocusRequester.requestFocus()
                            }
                        ),
                        isError = isAmountError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(amountFocusRequester)
                    )
                    
                    Box {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { 
                                description = it
                                showDescriptionDropdown = it.isNotEmpty()
                            },
                            label = { Text("Description") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(descriptionFocusRequester)
                        )
                        
                        if (showDescriptionDropdown && filteredDescriptions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .zIndex(1f),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    filteredDescriptions.forEach { desc ->
                                        Text(
                                            text = desc,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    description = desc
                                                    showDescriptionDropdown = false
                                                }
                                                .padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Transaction Type Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == TransactionType.EXPENSE,
                            onClick = { selectedType = TransactionType.EXPENSE },
                            label = { Text("Expense") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFEBEE),
                                selectedLabelColor = Color(0xFFC62828)
                            )
                        )
                        FilterChip(
                            selected = selectedType == TransactionType.INCOME,
                            onClick = { selectedType = TransactionType.INCOME },
                            label = { Text("Income") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8F5E9),
                                selectedLabelColor = Color(0xFF2E7D32)
                            )
                        )
                    }

                    // Payment Method Selection
                    if (paymentMethods.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = paymentMethods.find { pm -> pm.id.toLong() == selectedPaymentMethodId }?.name ?: "Select Payment Method",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Payment Method") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                paymentMethods.forEach { paymentMethod ->
                                    DropdownMenuItem(
                                        text = { Text(paymentMethod.name) },
                                        onClick = { 
                                            selectedPaymentMethodId = paymentMethod.id.toLong()
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editingTransaction != null) {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue != null && description.isNotBlank()) {
                                viewModel.updateTransaction(
                                    editingTransaction!!.copy(
                                        amount = amountValue,
                                        description = description,
                                        type = selectedType,
                                        paymentMethodId = selectedPaymentMethodId ?: 0L
                                    )
                                )
                                editingTransaction = null
                                amount = ""
                                description = ""
                                selectedType = TransactionType.EXPENSE
                                selectedPaymentMethodId = null
                            }
                        } else {
                            addTransaction()
                        }
                    }
                ) {
                    Text(if (editingTransaction != null) "Save" else "Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        editingTransaction = null
                        amount = ""
                        description = ""
                        selectedType = TransactionType.EXPENSE
                        selectedPaymentMethodId = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 