package com.linksink.viewmodel

import com.linksink.data.SectionStateSerializer
import com.linksink.model.Link
import com.linksink.model.Topic

data class TopicSection(
    val topic: Topic?,
    val links: List<Link>
) {
    val sectionKey: String
        get() = topic?.id?.toString() ?: SectionStateSerializer.UNCATEGORIZED_KEY
}

fun groupLinksByTopic(links: List<Link>, topics: List<Topic>): List<TopicSection> {
    if (links.isEmpty()) return emptyList()

    val topicById = topics.associateBy { it.id }
    val grouped = links.groupBy { it.topicId }

    val namedSections = grouped
        .mapNotNull { (topicId, sectionLinks) ->
            val topic = topicId?.let { topicById[it] } ?: return@mapNotNull null
            TopicSection(topic = topic, links = sectionLinks)
        }
        .sortedWith(compareBy({ it.topic!!.displayOrder }, { it.topic!!.name }))

    // Links with null topicId OR an unknown topicId (deleted topic) go to Uncategorized
    val uncategorizedLinks = links.filter { link ->
        link.topicId == null || topicById[link.topicId] == null
    }

    val uncategorizedSection = if (uncategorizedLinks.isNotEmpty()) {
        listOf(TopicSection(topic = null, links = uncategorizedLinks))
    } else {
        emptyList()
    }

    return namedSections + uncategorizedSection
}
