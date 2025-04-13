package com.example.checkbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.checkbook.data.entity.PaymentMethod
import com.example.checkbook.ui.viewmodels.PaymentMethodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    navController: NavController,
    paymentMethodViewModel: PaymentMethodViewModel
) {
    val paymentMethods by paymentMethodViewModel.paymentMethods.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newPaymentMethodName by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var paymentMethodToDelete by remember { mutableStateOf<PaymentMethod?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (paymentMethods.isEmpty()) {
                Text(
                    text = "No payment methods added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                paymentMethods.forEach { paymentMethod ->
                    PaymentMethodItem(
                        paymentMethod = paymentMethod,
                        onDelete = {
                            paymentMethodToDelete = paymentMethod
                            showDeleteConfirmation = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Payment Method")
            }
        }

        // Add Payment Method Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Payment Method") },
                text = {
                    OutlinedTextField(
                        value = newPaymentMethodName,
                        onValueChange = { newPaymentMethodName = it },
                        label = { Text("Payment Method Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newPaymentMethodName.isNotBlank()) {
                                paymentMethodViewModel.addPaymentMethod(
                                    name = newPaymentMethodName,
                                    iconName = "credit_card",
                                    iconColor = 0xFF2196F3.toInt()
                                )
                                newPaymentMethodName = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Payment Method") },
                text = { Text("Are you sure you want to delete this payment method?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            paymentMethodToDelete?.let { paymentMethodViewModel.deletePaymentMethod(it) }
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
    }
}

@Composable
private fun PaymentMethodItem(
    paymentMethod: PaymentMethod,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(paymentMethod.name) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Payment Method")
            }
        },
        modifier = Modifier.padding(vertical = 4.dp)
    )
} 