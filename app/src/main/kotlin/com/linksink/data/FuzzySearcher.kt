package com.linksink.data

import ca.solostudios.fuzzykt.FuzzyKt
import com.linksink.model.Link
import kotlin.math.max

object FuzzySearcher {

    data class SearchResult(
        val link: Link,
        val score: Double
    )

    fun search(
        query: String,
        links: List<Link>,
        threshold: Double = 0.6
    ): List<SearchResult> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()

        return links.asSequence()
            .map { link ->
                val urlScore = scoreField(q, link.url)
                val titleScore = scoreField(q, link.title.orEmpty())
                SearchResult(link, max(urlScore, titleScore))
            }
            .filter { it.score >= threshold }
            .sortedByDescending { it.score }
            .toList()
    }

    private fun scoreField(query: String, field: String): Double {
        if (field.isEmpty()) return 0.0
        val ql = query.lowercase()
        val fl = field.lowercase()
        return max(FuzzyKt.ratio(ql, fl), FuzzyKt.partialRatio(ql, fl))
    }
}
