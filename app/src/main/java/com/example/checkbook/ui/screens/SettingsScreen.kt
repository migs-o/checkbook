package com.example.checkbook.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.checkbook.data.entity.PaymentMethod
import com.example.checkbook.ui.viewmodels.PaymentMethodViewModel
import java.util.*

@Composable
fun SettingsScreen(
    viewModel: PaymentMethodViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var name by remember { mutableStateOf("") }
    var iconName by remember { mutableStateOf("") }
    var iconColor by remember { mutableStateOf(0) }

    val paymentMethods by viewModel.paymentMethods.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Payment Methods",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(paymentMethods) { paymentMethod ->
                PaymentMethodItem(
                    paymentMethod = paymentMethod,
                    onToggleActive = { viewModel.togglePaymentMethodActive(it) },
                    onDelete = { viewModel.deletePaymentMethod(it) },
                    onEdit = { editingPaymentMethod = it }
                )
            }
        }

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

    if (showAddDialog || editingPaymentMethod != null) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingPaymentMethod = null
                name = ""
                iconName = ""
                iconColor = 0
            },
            title = { Text(if (editingPaymentMethod != null) "Edit Payment Method" else "Add Payment Method") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Add more fields for icon selection and color
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editingPaymentMethod != null) {
                            viewModel.updatePaymentMethod(editingPaymentMethod!!.copy(
                                name = name,
                                iconName = iconName,
                                iconColor = iconColor
                            ))
                        } else {
                            viewModel.addPaymentMethod(name, iconName, iconColor)
                        }
                        showAddDialog = false
                        editingPaymentMethod = null
                        name = ""
                        iconName = ""
                        iconColor = 0
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        editingPaymentMethod = null
                        name = ""
                        iconName = ""
                        iconColor = 0
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PaymentMethodItem(
    paymentMethod: PaymentMethod,
    onToggleActive: (PaymentMethod) -> Unit,
    onDelete: (PaymentMethod) -> Unit,
    onEdit: (PaymentMethod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = paymentMethod.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (paymentMethod.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row {
                IconButton(onClick = { onEdit(paymentMethod) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onToggleActive(paymentMethod) }) {
                    Icon(
                        if (paymentMethod.isActive) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = if (paymentMethod.isActive) "Deactivate" else "Activate"
                    )
                }
                IconButton(onClick = { onDelete(paymentMethod) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
} 