package com.linksink.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {

    @Query("SELECT * FROM links ORDER BY saved_at DESC")
    fun getAllLinks(): Flow<List<LinkEntity>>

    @Query("SELECT * FROM links WHERE sync_status = 'PENDING' ORDER BY saved_at ASC")
    suspend fun getPendingLinks(): List<LinkEntity>

    @Query("SELECT * FROM links WHERE id = :id")
    suspend fun getLinkById(id: Long): LinkEntity?

    @Insert
    suspend fun insert(link: LinkEntity): Long

    @Update
    suspend fun update(link: LinkEntity)

    @Delete
    suspend fun delete(link: LinkEntity)

    @Query("UPDATE links SET sync_status = :status, discord_message_id = :messageId WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: String, messageId: String?)

    @Query("UPDATE links SET retry_count = retry_count + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)

    @Query("UPDATE links SET sync_status = 'FAILED' WHERE id = :id")
    suspend fun markFailed(id: Long)

    @Query("SELECT COUNT(*) FROM links WHERE sync_status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM links")
    fun getTotalCount(): Flow<Int>
}
