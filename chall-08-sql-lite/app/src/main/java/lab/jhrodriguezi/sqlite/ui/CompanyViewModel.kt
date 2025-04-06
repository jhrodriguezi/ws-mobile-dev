package lab.jhrodriguezi.sqlite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lab.jhrodriguezi.sqlite.data.Company
import lab.jhrodriguezi.sqlite.data.DatabaseHelper

// CompanyViewModel.kt
class CompanyViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    private val _companies = MutableStateFlow<List<Company>>(emptyList())
    val companies: StateFlow<List<Company>> = _companies.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedClassification = MutableStateFlow<String?>(null)
    val selectedClassification: StateFlow<String?> = _selectedClassification.asStateFlow()

    init {
        loadCompanies()
    }

    fun loadCompanies() {
        viewModelScope.launch(Dispatchers.IO) {
            val companies = dbHelper.searchCompanies(_searchQuery.value, _selectedClassification.value)
            _companies.value = companies
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        loadCompanies()
    }

    fun updateClassification(classification: String?) {
        _selectedClassification.value = classification
        loadCompanies()
    }

    fun addCompany(company: Company) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.insertCompany(company)
            loadCompanies()
        }
    }

    fun getCompany(id: Int): Company? {
        return dbHelper.getCompany(id)
    }

    fun updateCompany(company: Company) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.updateCompany(company)
            loadCompanies()
        }
    }

    fun deleteCompany(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteCompany(id)
            loadCompanies()
        }
    }
}