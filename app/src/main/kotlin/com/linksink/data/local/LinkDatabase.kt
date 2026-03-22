package com.linksink.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LinkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LinkDatabase : RoomDatabase() {

    abstract fun linkDao(): LinkDao

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
            ).build()
        }
    }
}
