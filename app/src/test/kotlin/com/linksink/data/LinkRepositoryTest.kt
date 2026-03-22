package com.linksink.data

import com.linksink.data.local.LinkEntity
import com.linksink.data.local.toDomain
import com.linksink.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkRepositoryTest {

    private fun linkEntity(
        id: Long,
        url: String,
        title: String?,
        domain: String = "example.com"
    ) = LinkEntity(
        id = id,
        url = url,
        title = title,
        description = null,
        thumbnailUrl = null,
        note = null,
        domain = domain,
        topicId = null,
        savedAt = 0L,
        syncStatus = SyncStatus.SYNCED.name,
        discordMessageId = null,
        retryCount = 0
    )

    @Test
    fun acceptance_searchLinksTransform_prefixQuery_returnsFuzzyMatchedTitle() {
        val react = linkEntity(1L, "https://example.com/r", "React Tutorial")
        val other = linkEntity(2L, "https://other.com/", "Kotlin Docs")

        val results = mapSearchResults("reac", listOf(react, other))

        assertEquals(1, results.size)
        assertEquals("React Tutorial", results.single().title)
    }

    @Test
    fun acceptance_searchLinksTransform_blankQuery_returnsAllFilteredLinks() {
        val a = linkEntity(1L, "https://a.com/", "A")
        val b = linkEntity(2L, "https://b.com/", "B")

        val results = mapSearchResults("", listOf(a, b))

        assertEquals(2, results.size)
        assertTrue(results.any { it.title == "A" })
        assertTrue(results.any { it.title == "B" })
    }

    @Test
    fun unit_mapSearchResults_nonBlank_matchesFuzzySearcherOutput() {
        val react = linkEntity(1L, "https://example.com/r", "React Tutorial")
        val entities = listOf(react)
        val links = entities.map { it.toDomain() }

        val fromMapper = mapSearchResults("reac", entities)
        val fromSearcher = FuzzySearcher.search("reac", links).map { it.link }

        assertEquals(fromSearcher, fromMapper)
    }

    @Test
    fun unit_searchLinksFlow_usesSameTransformAsMapSearchResults() = runBlocking {
        val react = linkEntity(1L, "https://example.com/r", "React Tutorial")
        val entities = listOf(react)
        val query = "reac"

        val viaFlow = flowOf(entities)
            .map { mapSearchResults(query, it) }

        assertEquals(
            mapSearchResults(query, entities),
            viaFlow.first()
        )
    }
}
