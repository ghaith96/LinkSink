package com.linksink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linksink.data.LinkRepository
import com.linksink.model.Link
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface LinkListUiState {
    data object Loading : LinkListUiState
    data class Success(
        val links: List<Link>,
        val pendingCount: Int
    ) : LinkListUiState
    data class Empty(val message: String = "No links saved yet") : LinkListUiState
    data class Error(val message: String) : LinkListUiState
}

class LinkListViewModel(
    private val repository: LinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LinkListUiState>(LinkListUiState.Loading)
    val uiState: StateFlow<LinkListUiState> = _uiState.asStateFlow()

    init {
        loadLinks()
    }

    private fun loadLinks() {
        viewModelScope.launch {
            combine(
                repository.getLinks(),
                repository.getPendingSyncCount()
            ) { links, pendingCount ->
                if (links.isEmpty()) {
                    LinkListUiState.Empty()
                } else {
                    LinkListUiState.Success(links, pendingCount)
                }
            }.catch { e ->
                _uiState.value = LinkListUiState.Error(
                    e.message ?: "Failed to load links"
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            repository.deleteLink(link.id)
        }
    }

    fun syncPendingLinks() {
        viewModelScope.launch {
            repository.syncPendingLinks()
        }
    }
}
