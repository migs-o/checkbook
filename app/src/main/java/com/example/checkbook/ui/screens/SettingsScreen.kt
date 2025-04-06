package com.example.checkbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.checkbook.data.PaymentMethod
import com.example.checkbook.ui.PaymentMethodViewModel
import com.example.checkbook.ui.viewmodels.SettingsViewModel
import com.example.checkbook.ui.viewmodels.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    paymentMethodViewModel: PaymentMethodViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )
    val themePreference by viewModel.themePreference.collectAsState(initial = "system")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = themePreference == "light",
                        onClick = { viewModel.setThemePreference("light") },
                        label = { Text("Light") }
                    )
                    FilterChip(
                        selected = themePreference == "dark",
                        onClick = { viewModel.setThemePreference("dark") },
                        label = { Text("Dark") }
                    )
                    FilterChip(
                        selected = themePreference == "system",
                        onClick = { viewModel.setThemePreference("system") },
                        label = { Text("System") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodItem(
    paymentMethod: PaymentMethod,
    onEdit: (PaymentMethod) -> Unit,
    onToggleActive: (PaymentMethod) -> Unit,
    onDelete: (PaymentMethod) -> Unit
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
            Text(paymentMethod.name)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = paymentMethod.isActive,
                    onCheckedChange = { onToggleActive(paymentMethod) }
                )
                IconButton(onClick = { onEdit(paymentMethod) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(paymentMethod) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPaymentMethodDialog(
    paymentMethod: PaymentMethod?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(paymentMethod?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (paymentMethod == null) "Add Payment Method" else "Edit Payment Method") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text(if (paymentMethod == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 