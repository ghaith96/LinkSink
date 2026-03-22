package com.linksink.data

import ca.solostudios.fuzzykt.FuzzyKt
import com.linksink.model.Link
import com.linksink.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import kotlin.math.max

class FuzzySearcherTest {

    private fun link(
        url: String,
        title: String? = null,
        domain: String = "example.com"
    ) = Link(
        url = url,
        title = title,
        description = null,
        thumbnailUrl = null,
        note = null,
        domain = domain,
        topicId = null,
        savedAt = Instant.EPOCH,
        syncStatus = SyncStatus.SYNCED
    )

    // --- Acceptance: end-to-end with Link objects ---

    @Test
    fun acceptance_prefixQuery_matchesTitle() {
        val react = link(
            url = "https://example.com/r",
            title = "React Tutorial"
        )
        val results = FuzzySearcher.search("reac", listOf(react))
        assertEquals(1, results.size)
        assertEquals(react, results.single().link)
        assertTrue(results.single().score >= 0.6)
    }

    @Test
    fun acceptance_typoQuery_matchesTitle() {
        val react = link(
            url = "https://example.com/r",
            title = "React Tutorial"
        )
        val results = FuzzySearcher.search("rect", listOf(react))
        assertEquals(1, results.size)
        assertEquals(react, results.single().link)
        assertTrue(results.single().score >= 0.6)
    }

    @Test
    fun acceptance_excludesBelowDefaultThreshold() {
        val strong = link(url = "https://react.dev/", title = "React")
        val weak = link(url = "https://unrelated.net/abc", title = "Nothing alike")
        val results = FuzzySearcher.search("react", listOf(strong, weak), threshold = 0.6)
        assertTrue(results.any { it.link == strong })
        assertTrue(results.none { it.link == weak })
    }

    @Test
    fun acceptance_sortedByScoreDescending() {
        val a = link(url = "https://react.dev/learn", title = "React")
        val b = link(url = "https://react.dev/blog", title = "React Blog")
        val results = FuzzySearcher.search("react", listOf(b, a))
        assertEquals(2, results.size)
        assertTrue(results[0].score >= results[1].score)
    }

    // --- Unit: scoring via public search() ---

    @Test
    fun unit_urlScoring_usesKtFuzzyOnUrlWhenTitleDoesNotDominate() {
        val q = "alphabetagamma"
        val u = "https://z.zz/u/alphabetagamma-extra"
        val title = "unrelated unrelated"
        val l = link(url = u, title = title)
        val ql = q.lowercase()
        val ul = u.lowercase()
        val tl = title.lowercase()
        val expectedUrl = max(FuzzyKt.ratio(ql, ul), FuzzyKt.partialRatio(ql, ul))
        val expectedTitle = max(FuzzyKt.ratio(ql, tl), FuzzyKt.partialRatio(ql, tl))
        val expected = max(expectedUrl, expectedTitle)
        val actual = FuzzySearcher.search(q, listOf(l), threshold = 0.0).single().score
        assertEquals(expected, actual, 1e-12)
        assertTrue(expectedUrl > expectedTitle)
    }

    @Test
    fun unit_titleScoring_contributesWhenQueryMatchesTitle() {
        val l = link(url = "https://x.com/a", title = "UniqueTitleToken")
        val score = FuzzySearcher.search("UniqueTitleToken", listOf(l), threshold = 0.0).single().score
        assertTrue(score >= 0.6)
    }

    @Test
    fun unit_maxScore_isGreaterOfUrlAndTitleContributions() {
        val urlOnly = link(url = "https://x.com/a", title = null)
        val titleMatch = link(url = "https://x.com/a", title = "UniqueTitleToken")
        val q = "UniqueTitleToken"
        val sUrl = FuzzySearcher.search(q, listOf(urlOnly), threshold = 0.0).single().score
        val sTitle = FuzzySearcher.search(q, listOf(titleMatch), threshold = 0.0).single().score
        assertTrue(sTitle > sUrl)
    }

    @Test
    fun unit_usesMaxOfUrlAndTitleScores() {
        val l = link(
            url = "https://alpha.com/foo",
            title = "alpha tutorial"
        )
        val full = FuzzySearcher.search("alpha", listOf(l), threshold = 0.0).single().score
        val urlOnly = FuzzySearcher.search("alpha", listOf(l.copy(title = "")), threshold = 0.0).single().score
        val titleOnly = FuzzySearcher.search(
            "alpha",
            listOf(l.copy(url = "https://beta.com/")),
            threshold = 0.0
        ).single().score
        assertEquals(max(urlOnly, titleOnly), full, 1e-9)
    }

    @Test
    fun unit_thresholdFiltersResults() {
        val links = listOf(
            link(url = "https://react.dev/", title = "React"),
            link(url = "https://other.com/", title = "Unrelated")
        )
        val strict = FuzzySearcher.search("react", links, threshold = 0.85)
        val loose = FuzzySearcher.search("react", links, threshold = 0.2)
        assertTrue(strict.size < loose.size)
        assertTrue(loose.size >= 2)
    }

    @Test
    fun blankQuery_returnsEmpty() {
        assertTrue(FuzzySearcher.search("   ", listOf(link(url = "https://a.com", title = "A"))).isEmpty())
    }
}
