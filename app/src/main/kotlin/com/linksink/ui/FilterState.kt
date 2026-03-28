package com.linksink.ui

import com.linksink.model.DateRange
import com.linksink.viewmodel.LinkListUiState

data class FilterState(
    val topicId: Long?,
    val dateRange: DateRange?,
    val groupByTopic: Boolean = true
) {
    fun clearAll(): FilterState = copy(topicId = null, dateRange = null, groupByTopic = true)

    fun hasPendingChanges(applied: FilterState): Boolean = this != applied

    fun toApplySnapshot(): FilterApplySnapshot =
        FilterApplySnapshot(topicId = topicId, dateRange = dateRange, groupByTopic = groupByTopic)

    companion object {
        val DEFAULT = FilterState(topicId = null, dateRange = null, groupByTopic = true)
    }
}

data class FilterApplySnapshot(
    val topicId: Long?,
    val dateRange: DateRange?,
    val groupByTopic: Boolean
)

fun filterStateFromApplied(
    topicId: Long?,
    dateRange: DateRange?,
    groupByTopic: Boolean
): FilterState = FilterState(topicId = topicId, dateRange = dateRange, groupByTopic = groupByTopic)

internal fun useSectionedLayout(state: LinkListUiState.Success, groupByTopic: Boolean): Boolean =
    groupByTopic && state.topicSections.isNotEmpty() && !state.hasActiveFilters
