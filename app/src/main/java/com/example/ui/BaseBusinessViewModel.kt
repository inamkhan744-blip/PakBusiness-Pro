package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BusinessEntity
import com.example.data.BusinessRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EntityUiState<out T> {
    data object Loading : EntityUiState<Nothing>
    data class Success<T>(val data: T) : EntityUiState<T>
    data object Empty : EntityUiState<Nothing>
    data class Error(val message: String) : EntityUiState<Nothing>
}

abstract class BaseBusinessViewModel(application: Application) : AndroidViewModel(application) {

    protected val database: AppDatabase = AppDatabase.getDatabase(application)
    protected val businessRepository: BusinessRepository = BusinessRepository(database.businessDao())

    val activeBusiness: StateFlow<BusinessEntity?> = businessRepository.activeBusiness.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    protected val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    protected val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    protected fun launchWithLoading(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val handler = CoroutineExceptionHandler { _, exception ->
            _isLoading.value = false
            _errorMessage.value = exception.localizedMessage ?: "An unexpected error occurred"
            onError?.invoke(exception)
        }
        viewModelScope.launch(handler) {
            _isLoading.value = true
            try {
                block()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
