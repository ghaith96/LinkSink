package com.linksink.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    @Query("SELECT * FROM topics ORDER BY name ASC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getById(id: Long): TopicEntity?

    @Query(
        """
        SELECT t.id, t.name, t.parent_id as parentId, t.hook_mode as hookMode, 
               t.custom_webhook_url as customWebhookUrl, t.created_at as createdAt, 
               t.color, COUNT(l.id) as linkCount 
        FROM topics t 
        LEFT JOIN links l ON l.topic_id = t.id 
        WHERE t.id = :id 
        GROUP BY t.id
        """
    )
    suspend fun getWithLinkCount(id: Long): TopicWithCount?

    @Insert
    suspend fun insert(topic: TopicEntity): Long

    @Update
    suspend fun update(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE links SET topic_id = NULL WHERE topic_id = :topicId")
    suspend fun unlinkAllFromTopic(topicId: Long)

    @Query("DELETE FROM links WHERE topic_id = :topicId")
    suspend fun deleteLinksInTopic(topicId: Long)

    @Query(
        """
        SELECT DISTINCT t.* FROM topics t
        INNER JOIN links l ON l.topic_id = t.id
        ORDER BY l.saved_at DESC
        LIMIT :limit
        """
    )
    fun getRecentlyUsed(limit: Int): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TopicEntity?
}
