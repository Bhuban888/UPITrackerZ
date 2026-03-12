package com.upitracker.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upitracker.app.data.model.Budget
import com.upitracker.app.data.model.Transaction
import com.upitracker.app.data.model.TransactionType
import com.upitracker.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0,
    val categoryBreakdown: Map<String, Double> = emptyMap(),
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType = _filterType.asStateFlow()

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        repository.getAllTransactions(),
        _searchQuery,
        _filterType
    ) { txList, query, type ->
        txList.filter { tx ->
            val matchesQuery = query.isEmpty() ||
                    tx.description.contains(query, ignoreCase = true) ||
                    tx.upiId.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true)
            val matchesType = type == null || tx.type == type
            matchesQuery && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardState: StateFlow<DashboardUiState> = run {
        val (start, end) = repository.currentMonthRange()
        combine(
            repository.getAllTransactions(),
            repository.getTotalCreditInRange(start, end),
            repository.getTotalDebitInRange(start, end),
            repository.getSpendingByCategory(start, end),
            repository.getBudgetsForCurrentMonth()
        ) { txList, credit, debit, categories, budgets ->
            DashboardUiState(
                transactions = txList.take(5),
                totalCredit = credit ?: 0.0,
                totalDebit = debit ?: 0.0,
                categoryBreakdown = categories.associate { it.category to it.total },
                budgets = budgets,
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setFilterType(type: TransactionType?) { _filterType.value = type }
    fun deleteTransaction(transaction: Transaction) { viewModelScope.launch { repository.delete(transaction) } }
    fun updateTransaction(transaction: Transaction) { viewModelScope.launch { repository.update(transaction) } }
    fun addManualTransaction(transaction: Transaction) { viewModelScope.launch { repository.insert(transaction) } }
    fun upsertBudget(budget: Budget) { viewModelScope.launch { repository.upsertBudget(budget) } }
    fun deleteBudget(budget: Budget) { viewModelScope.launch { repository.deleteBudget(budget) } }
}
