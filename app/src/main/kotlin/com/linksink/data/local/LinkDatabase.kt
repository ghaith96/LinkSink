package com.linksink.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LinkEntity::class, TopicEntity::class],
    version = 6,
    exportSchema = false
)
abstract class LinkDatabase : RoomDatabase() {

    abstract fun linkDao(): LinkDao
    abstract fun topicDao(): TopicDao

    companion object {
        private const val DATABASE_NAME = "linksink.db"

        @Volatile
        private var instance: LinkDatabase? = null

        fun getInstance(context: Context): LinkDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): LinkDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                LinkDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()
        }
    }
}
