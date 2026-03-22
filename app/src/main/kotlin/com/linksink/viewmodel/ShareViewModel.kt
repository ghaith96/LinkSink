package com.linksink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linksink.data.LinkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ShareUiState {
    data object Idle : ShareUiState
    data class Extracting(val rawText: String) : ShareUiState
    data class Ready(val url: String, val domain: String) : ShareUiState
    data object Saving : ShareUiState
    data object Success : ShareUiState
    data class Error(val message: String) : ShareUiState
    data object NoUrl : ShareUiState
}

class ShareViewModel(
    private val repository: LinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShareUiState>(ShareUiState.Idle)
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    fun processSharedText(text: String?) {
        if (text.isNullOrBlank()) {
            _uiState.value = ShareUiState.NoUrl
            return
        }

        _uiState.value = ShareUiState.Extracting(text)

        val url = LinkRepository.extractUrlFromText(text)
        if (url == null) {
            _uiState.value = ShareUiState.NoUrl
            return
        }

        val domain = LinkRepository.extractDomain(url)
        _uiState.value = ShareUiState.Ready(url = url, domain = domain)
    }

    fun saveLink() {
        val currentState = _uiState.value
        if (currentState !is ShareUiState.Ready) return

        _uiState.value = ShareUiState.Saving

        viewModelScope.launch {
            val result = repository.saveLink(currentState.url)
            _uiState.value = if (result.isSuccess) {
                ShareUiState.Success
            } else {
                ShareUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to save link"
                )
            }
        }
    }

    fun retry() {
        val currentState = _uiState.value
        if (currentState is ShareUiState.Error) {
            _uiState.value = ShareUiState.Idle
        }
    }
}
