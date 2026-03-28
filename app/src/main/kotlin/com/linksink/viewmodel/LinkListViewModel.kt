package com.linksink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linksink.data.LinkRepository
import com.linksink.data.SettingsStore
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

enum class LinkFilter {
    ALL,
    UNREAD,
    ARCHIVED
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

sealed interface LinkListUiState {
    data object Loading : LinkListUiState
    data class Success(
        val links: List<Link>,
        val pendingCount: Int,
        val searchQuery: String = "",
        val topicFilter: Long? = null,
        val dateRange: DateRange? = null,
        val linkFilter: LinkFilter = LinkFilter.ALL,
        val topicSections: List<TopicSection> = emptyList()
    ) : LinkListUiState {
        val hasActiveFilters: Boolean get() =
            searchQuery.isNotEmpty() || topicFilter != null || dateRange != null || linkFilter != LinkFilter.ALL
        
        val hasLinks: Boolean get() = links.isNotEmpty()
        
        val hasReadLinks: Boolean get() = links.any { it.isRead }
    }
    data class Empty(val message: String = "No links saved yet") : LinkListUiState
    data class Error(val message: String) : LinkListUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class LinkListViewModel(
    private val repository: LinkRepository,
    private val topicRepository: TopicRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _topicFilter = MutableStateFlow<Long?>(null)
    val topicFilter: StateFlow<Long?> = _topicFilter.asStateFlow()

    private val _dateRange = MutableStateFlow<DateRange?>(null)
    val dateRange: StateFlow<DateRange?> = _dateRange.asStateFlow()

    private val _linkFilter = MutableStateFlow(LinkFilter.ALL)
    val linkFilter: StateFlow<LinkFilter> = _linkFilter.asStateFlow()

    private val _uiState = MutableStateFlow<LinkListUiState>(LinkListUiState.Loading)
    val uiState: StateFlow<LinkListUiState> = _uiState.asStateFlow()

    private var loadLinksJob: kotlinx.coroutines.Job? = null

    val topics: StateFlow<List<Topic>> = topicRepository.getAllTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sectionStates: StateFlow<Map<String, Boolean>> = settingsStore.sectionStates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        loadLinks()
    }

    private fun loadLinks() {
        loadLinksJob?.cancel()
        loadLinksJob = viewModelScope.launch {
            combine(
                _searchQuery,
                _topicFilter,
                _dateRange,
                _linkFilter
            ) { query, topic, dates, filter ->
                Quad(query, topic, dates, filter)
            }.flatMapLatest { (query, topic, dates, filter) ->
                val linksFlow = when (filter) {
                    LinkFilter.ALL -> repository.searchLinks(query, topic, dates)
                    LinkFilter.UNREAD -> repository.getUnreadLinks()
                    LinkFilter.ARCHIVED -> repository.getArchivedLinks()
                }
                
                combine(
                    linksFlow,
                    repository.getPendingSyncCount(),
                    topics
                ) { links, pendingCount, topicList ->
                    if (links.isEmpty()) {
                        val filterMessage = when (filter) {
                            LinkFilter.UNREAD -> "No unread links"
                            LinkFilter.ARCHIVED -> "No archived links"
                            LinkFilter.ALL -> if (query.isNotEmpty() || topic != null || dates != null) {
                                "No links match your filters"
                            } else {
                                "No links saved yet"
                            }
                        }
                        LinkListUiState.Empty(filterMessage)
                    } else {
                        val sections = if (query.isBlank() && topic == null && dates == null && filter == LinkFilter.ALL) {
                            groupLinksByTopic(links, topicList)
                        } else {
                            emptyList()
                        }
                        LinkListUiState.Success(
                            links = links,
                            pendingCount = pendingCount,
                            searchQuery = query,
                            topicFilter = topic,
                            dateRange = dates,
                            linkFilter = filter,
                            topicSections = sections
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

    fun toggleSection(key: String) {
        viewModelScope.launch {
            val current = sectionStates.value[key] ?: true
            settingsStore.setSectionExpanded(key, !current)
        }
    }

    fun setSectionExpanded(key: String, expanded: Boolean) {
        viewModelScope.launch {
            settingsStore.setSectionExpanded(key, expanded)
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
        _linkFilter.value = LinkFilter.ALL
    }

    fun setLinkFilter(filter: LinkFilter) {
        _linkFilter.value = filter
    }

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            repository.deleteLink(link.id)
        }
    }

    fun toggleReadStatus(link: Link) {
        viewModelScope.launch {
            repository.toggleReadStatus(link.id)
        }
    }

    fun archiveLink(link: Link) {
        viewModelScope.launch {
            repository.archiveLink(link.id)
        }
    }

    fun unarchiveLink(link: Link) {
        viewModelScope.launch {
            repository.unarchiveLink(link.id)
        }
    }

    fun openLink(link: Link) {
        viewModelScope.launch {
            repository.openLink(link.id)
        }
    }

    fun syncPendingLinks() {
        viewModelScope.launch {
            repository.syncPendingLinks()
        }
    }

    fun refresh() {
        _uiState.value = LinkListUiState.Loading
        loadLinks()
    }
}
