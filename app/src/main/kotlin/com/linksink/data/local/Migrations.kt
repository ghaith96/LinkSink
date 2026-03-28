package com.linksink.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE topics (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                parent_id INTEGER,
                hook_mode TEXT NOT NULL DEFAULT 'USE_GLOBAL',
                custom_webhook_url TEXT,
                created_at INTEGER NOT NULL,
                color INTEGER
            )
            """.trimIndent()
        )

        db.execSQL("CREATE UNIQUE INDEX idx_topics_name ON topics(name)")
        db.execSQL("CREATE INDEX idx_topics_parent ON topics(parent_id)")

        db.execSQL("ALTER TABLE links ADD COLUMN topic_id INTEGER")

        db.execSQL("CREATE INDEX idx_links_topic ON links(topic_id)")
        db.execSQL("CREATE INDEX idx_links_saved_at ON links(saved_at)")

        db.execSQL(
            """
            CREATE VIRTUAL TABLE links_fts USING fts4(
                url,
                title,
                content=links,
                tokenize=simple
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO links_fts(rowid, url, title)
            SELECT id, url, COALESCE(title, '') FROM links
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TRIGGER links_ai AFTER INSERT ON links BEGIN
                INSERT INTO links_fts(rowid, url, title) 
                VALUES (new.id, new.url, COALESCE(new.title, ''));
            END
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TRIGGER links_ad AFTER DELETE ON links BEGIN
                INSERT INTO links_fts(links_fts, rowid, url, title) 
                VALUES ('delete', old.id, old.url, COALESCE(old.title, ''));
            END
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TRIGGER links_au AFTER UPDATE ON links BEGIN
                INSERT INTO links_fts(links_fts, rowid, url, title) 
                VALUES ('delete', old.id, old.url, COALESCE(old.title, ''));
                INSERT INTO links_fts(rowid, url, title) 
                VALUES (new.id, new.url, COALESCE(new.title, ''));
            END
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TRIGGER IF EXISTS links_ai")
        db.execSQL("DROP TRIGGER IF EXISTS links_ad")
        db.execSQL("DROP TRIGGER IF EXISTS links_au")
        db.execSQL("DROP TABLE IF EXISTS links_fts")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE topics ADD COLUMN emoji TEXT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE topics ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE links ADD COLUMN is_read INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE links ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0")
    }
}
