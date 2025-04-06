package lab.jhrodriguezi.webservice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lab.jhrodriguezi.webservice.data.CovidCase
import lab.jhrodriguezi.webservice.data.CovidRepository
import lab.jhrodriguezi.webservice.data.SearchParams

class CovidViewModel : ViewModel() {
    private val repository = CovidRepository()
    private val _cases = MutableStateFlow<List<CovidCase>>(emptyList())
    val cases: StateFlow<List<CovidCase>> = _cases.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchParams = MutableStateFlow(SearchParams())
    val searchParams: StateFlow<SearchParams> = _searchParams.asStateFlow()

    fun updateSearchParams(params: SearchParams) {
        _searchParams.value = params
    }

    fun searchCases() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _cases.value = repository.getCovidCases(_searchParams.value)
                if (_cases.value.isEmpty()) {
                    _error.value = "No se encontraron resultados"
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar los datos: ${e.message}"
                _cases.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}