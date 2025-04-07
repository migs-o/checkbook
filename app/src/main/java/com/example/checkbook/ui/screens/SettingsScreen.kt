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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.checkbook.data.PaymentMethod
import com.example.checkbook.ui.PaymentMethodViewModel
import com.example.checkbook.ui.viewmodels.SettingsViewModel
import com.example.checkbook.ui.viewmodels.SettingsViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    paymentMethodViewModel: PaymentMethodViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )
    val themePreference by viewModel.themePreference.collectAsState(initial = "system")
    val paymentMethods by paymentMethodViewModel.allPaymentMethods.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var isPaymentMethodsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            // Add an empty box with the same size as the back button to maintain centering
            Box(modifier = Modifier.size(48.dp))
        }

        // Theme Settings
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

        // Payment Methods Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPaymentMethodsExpanded = !isPaymentMethodsExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Methods",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add payment method")
                        }
                        Icon(
                            imageVector = if (isPaymentMethodsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isPaymentMethodsExpanded) "Collapse" else "Expand"
                        )
                    }
                }
                
                AnimatedVisibility(
                    visible = isPaymentMethodsExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(paymentMethods) { paymentMethod ->
                            PaymentMethodItem(
                                paymentMethod = paymentMethod,
                                onEdit = { editingPaymentMethod = it },
                                onToggleActive = { paymentMethodViewModel.togglePaymentMethodActive(it) },
                                onDelete = { paymentMethodViewModel.deletePaymentMethod(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingPaymentMethod != null) {
        AddEditPaymentMethodDialog(
            paymentMethod = editingPaymentMethod,
            onDismiss = {
                showAddDialog = false
                editingPaymentMethod = null
            },
            onConfirm = { name ->
                if (editingPaymentMethod != null) {
                    paymentMethodViewModel.updatePaymentMethod(editingPaymentMethod!!.copy(name = name))
                } else {
                    paymentMethodViewModel.addPaymentMethod(name)
                }
                showAddDialog = false
                editingPaymentMethod = null
            }
        )
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
    var isNameError by remember { mutableStateOf(false) }
    var shouldShake by remember { mutableStateOf(false) }
    
    val nameFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val offsetX by animateFloatAsState(
        targetValue = if (shouldShake) 10f else 0f,
        animationSpec = tween(durationMillis = 50),
        finishedListener = { shouldShake = false }
    )

    LaunchedEffect(Unit) {
        nameFocusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (paymentMethod == null) "Add Payment Method" else "Edit Payment Method") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                isError = isNameError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = offsetX.dp)
                    .focusRequester(nameFocusRequester)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        isNameError = true
                        shouldShake = true
                    } else {
                        onConfirm(name)
                        keyboardController?.hide()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (paymentMethod == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    keyboardController?.hide()
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
} 