package com.example.checkbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.checkbook.data.entity.TransactionType
import com.example.checkbook.ui.viewmodels.PaymentMethodViewModel
import com.example.checkbook.ui.viewmodels.TransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    transactionViewModel: TransactionViewModel = hiltViewModel(),
    paymentMethodViewModel: PaymentMethodViewModel = hiltViewModel()
) {
    val transactions by transactionViewModel.transactions.collectAsState()
    val paymentMethods by paymentMethodViewModel.paymentMethods.collectAsState()
    
    var selectedDateRange by remember { mutableStateOf(DateRange.MONTH) }
    var customStartDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var customEndDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    
    val filteredTransactions = remember(selectedDateRange, customStartDate, customEndDate, transactions) {
        when (selectedDateRange) {
            DateRange.DAY -> transactions.filter { it.date == LocalDate.now() }
            DateRange.WEEK -> {
                val startOfWeek = LocalDate.now().minusDays(7)
                transactions.filter { it.date >= startOfWeek && it.date <= LocalDate.now() }
            }
            DateRange.MONTH -> {
                val startOfMonth = LocalDate.now().minusMonths(1)
                transactions.filter { it.date >= startOfMonth && it.date <= LocalDate.now() }
            }
            DateRange.YEAR -> {
                val startOfYear = LocalDate.now().minusYears(1)
                transactions.filter { it.date >= startOfYear && it.date <= LocalDate.now() }
            }
            DateRange.CUSTOM -> transactions.filter { it.date >= customStartDate && it.date <= customEndDate }
        }
    }
    
    val paymentMethodExpenses = remember(filteredTransactions, paymentMethods) {
        paymentMethods.associateWith { paymentMethod ->
            filteredTransactions
                .filter { it.paymentMethodId == paymentMethod.id && it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Date range selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateRange.values().filter { it != DateRange.CUSTOM }.forEach { range ->
                        FilterChip(
                            selected = selectedDateRange == range,
                            onClick = { selectedDateRange = range },
                            label = { Text(range.label) }
                        )
                    }
                    
                    // Calendar icon button for custom date range
                    FilterChip(
                        selected = selectedDateRange == DateRange.CUSTOM,
                        onClick = { selectedDateRange = DateRange.CUSTOM },
                        label = { Icon(Icons.Default.DateRange, contentDescription = "Custom Date Range") }
                    )
                }
                
                if (selectedDateRange == DateRange.CUSTOM) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { 
                                isSelectingStartDate = true
                                showDatePicker = true 
                            }
                        ) {
                            Text("Start: ${customStartDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}")
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                isSelectingStartDate = false
                                showDatePicker = true 
                            }
                        ) {
                            Text("End: ${customEndDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}")
                        }
                    }
                }
            }
        }
        
        // Payment method expenses
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Expenses by Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn {
                    items(paymentMethods) { paymentMethod ->
                        val total = paymentMethodExpenses[paymentMethod] ?: 0.0
                        
                        if (total > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = paymentMethod.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Text(
                                    text = "$${String.format("%.2f", total)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            
                            Divider()
                        }
                    }
                }
            }
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (if (isSelectingStartDate) customStartDate else customEndDate)
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            
                            if (isSelectingStartDate) {
                                customStartDate = localDate
                            } else {
                                customEndDate = localDate
                            }
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
                        text = if (isSelectingStartDate) "Select Start Date" else "Select End Date",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    }
}

enum class DateRange(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year"),
    CUSTOM("Custom")
} 