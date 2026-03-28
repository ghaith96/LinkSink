package com.linksink.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterChipsClearAllTest {

    @Test
    fun `clear all chip is shown only when both topic and date filters are active`() {
        assertFalse(shouldOfferClearAllFiltersChip(topicFilterActive = false, dateRangeActive = false))
        assertFalse(shouldOfferClearAllFiltersChip(topicFilterActive = true, dateRangeActive = false))
        assertFalse(shouldOfferClearAllFiltersChip(topicFilterActive = false, dateRangeActive = true))
        assertTrue(shouldOfferClearAllFiltersChip(topicFilterActive = true, dateRangeActive = true))
    }
}
