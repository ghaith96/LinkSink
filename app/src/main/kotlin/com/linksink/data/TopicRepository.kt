package com.linksink.data

import com.linksink.data.local.TopicDao
import com.linksink.data.local.TopicWithCount
import com.linksink.data.local.toDomain
import com.linksink.data.local.toEntity
import com.linksink.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TopicRepository(
    private val topicDao: TopicDao
) {
    fun getAllTopics(): Flow<List<Topic>> =
        topicDao.getAllTopics().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getRecentTopics(limit: Int = 5): Flow<List<Topic>> =
        topicDao.getRecentlyUsed(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getTopicById(id: Long): Topic? =
        topicDao.getById(id)?.toDomain()

    suspend fun createTopic(topic: Topic): Long {
        val existingTopic = topicDao.getByName(topic.name)
        if (existingTopic != null) {
            throw IllegalArgumentException("Topic name already exists")
        }
        return topicDao.insert(topic.toEntity())
    }

    suspend fun updateTopic(topic: Topic) {
        val existingWithSameName = topicDao.getByName(topic.name)
        if (existingWithSameName != null && existingWithSameName.id != topic.id) {
            throw IllegalArgumentException("Topic name already exists")
        }
        topicDao.update(topic.toEntity())
    }

    suspend fun deleteTopic(id: Long, deleteLinks: Boolean) {
        if (deleteLinks) {
            topicDao.deleteLinksInTopic(id)
        } else {
            topicDao.unlinkAllFromTopic(id)
        }
        topicDao.delete(id)
    }

    suspend fun getTopicWithLinkCount(id: Long): TopicWithCount? =
        topicDao.getWithLinkCount(id)
}
