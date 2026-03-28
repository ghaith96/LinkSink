package com.linksink.data

import com.linksink.model.LinkMetadata
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class MetadataFetcher(
    private val httpClient: HttpClient
) : MetadataFetcherPort {
    companion object {
        private const val FETCH_TIMEOUT_MS = 5000L
        private const val MAX_RETRIES = 3

        private val ogTitleRegex =
            """<meta[^>]*property=["']og:title["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
        private val ogTitleAltRegex =
            """<meta[^>]*content=["']([^"']+)["'][^>]*property=["']og:title["']""".toRegex(RegexOption.IGNORE_CASE)
        private val ogDescRegex =
            """<meta[^>]*property=["']og:description["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
        private val ogDescAltRegex =
            """<meta[^>]*content=["']([^"']+)["'][^>]*property=["']og:description["']""".toRegex(RegexOption.IGNORE_CASE)
        private val ogImageRegex =
            """<meta[^>]*property=["']og:image["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
        private val ogImageAltRegex =
            """<meta[^>]*content=["']([^"']+)["'][^>]*property=["']og:image["']""".toRegex(RegexOption.IGNORE_CASE)
        private val ogSiteNameRegex =
            """<meta[^>]*property=["']og:site_name["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)

        private val twitterTitleRegex =
            """<meta[^>]*name=["']twitter:title["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
        private val twitterDescRegex =
            """<meta[^>]*name=["']twitter:description["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
        private val twitterImageRegex =
            """<meta[^>]*name=["']twitter:image["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)

        private val htmlTitleRegex =
            """<title[^>]*>([^<]+)</title>""".toRegex(RegexOption.IGNORE_CASE)
        private val metaDescRegex =
            """<meta[^>]*name=["']description["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
        private val metaDescAltRegex =
            """<meta[^>]*content=["']([^"']+)["'][^>]*name=["']description["']""".toRegex(RegexOption.IGNORE_CASE)
    }

    override suspend fun fetch(url: String): Result<LinkMetadata> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                val result = withTimeoutOrNull(FETCH_TIMEOUT_MS) {
                    val response = httpClient.get(url)
                    val html = response.bodyAsText()
                    parseHtml(html)
                }

                if (result != null) {
                    return@withContext Result.success(result)
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: Exception("Failed to fetch metadata after $MAX_RETRIES attempts"))
    }

    internal fun parseHtml(html: String): LinkMetadata {
        val title = extractFirst(html, ogTitleRegex, ogTitleAltRegex)
            ?: extractFirst(html, twitterTitleRegex)
            ?: extractFirst(html, htmlTitleRegex)

        val description = extractFirst(html, ogDescRegex, ogDescAltRegex)
            ?: extractFirst(html, twitterDescRegex)
            ?: extractFirst(html, metaDescRegex, metaDescAltRegex)

        val imageUrl = extractFirst(html, ogImageRegex, ogImageAltRegex)
            ?: extractFirst(html, twitterImageRegex)

        val siteName = extractFirst(html, ogSiteNameRegex)

        return LinkMetadata(
            title = title?.trim()?.decodeHtmlEntities(),
            description = description?.trim()?.decodeHtmlEntities(),
            imageUrl = imageUrl?.trim(),
            siteName = siteName?.trim()?.decodeHtmlEntities()
        )
    }

    private fun extractFirst(html: String, vararg patterns: Regex): String? {
        for (pattern in patterns) {
            pattern.find(html)?.groupValues?.getOrNull(1)?.let { return it }
        }
        return null
    }

    private fun String.decodeHtmlEntities(): String = this
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&#x27;", "'")
        .replace("&#x2F;", "/")
}
