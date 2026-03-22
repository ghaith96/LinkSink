package com.linksink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linksink.data.LinkRepository
import com.linksink.data.TopicRepository
import com.linksink.model.DateRange
import com.linksink.model.Link
import com.linksink.model.Topic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LinkListUiState {
    data object Loading : LinkListUiState
    data class Success(
        val links: List<Link>,
        val pendingCount: Int,
        val searchQuery: String = "",
        val topicFilter: Long? = null,
        val dateRange: DateRange? = null
    ) : LinkListUiState {
        val hasActiveFilters: Boolean get() =
            searchQuery.isNotEmpty() || topicFilter != null || dateRange != null
    }
    data class Empty(val message: String = "No links saved yet") : LinkListUiState
    data class Error(val message: String) : LinkListUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class LinkListViewModel(
    private val repository: LinkRepository,
    private val topicRepository: TopicRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _topicFilter = MutableStateFlow<Long?>(null)
    val topicFilter: StateFlow<Long?> = _topicFilter.asStateFlow()

    private val _dateRange = MutableStateFlow<DateRange?>(null)
    val dateRange: StateFlow<DateRange?> = _dateRange.asStateFlow()

    private val _uiState = MutableStateFlow<LinkListUiState>(LinkListUiState.Loading)
    val uiState: StateFlow<LinkListUiState> = _uiState.asStateFlow()

    val topics: StateFlow<List<Topic>> = topicRepository.getAllTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadLinks()
    }

    private fun loadLinks() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _topicFilter,
                _dateRange
            ) { query, topic, dates ->
                Triple(query, topic, dates)
            }.flatMapLatest { (query, topic, dates) ->
                combine(
                    repository.searchLinks(query, topic, dates),
                    repository.getPendingSyncCount()
                ) { links, pendingCount ->
                    if (links.isEmpty()) {
                        if (query.isNotEmpty() || topic != null || dates != null) {
                            LinkListUiState.Empty("No links match your filters")
                        } else {
                            LinkListUiState.Empty()
                        }
                    } else {
                        LinkListUiState.Success(
                            links = links,
                            pendingCount = pendingCount,
                            searchQuery = query,
                            topicFilter = topic,
                            dateRange = dates
                        )
                    }
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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTopicFilter(topicId: Long?) {
        _topicFilter.value = topicId
    }

    fun setDateRange(range: DateRange?) {
        _dateRange.value = range
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _topicFilter.value = null
        _dateRange.value = null
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
