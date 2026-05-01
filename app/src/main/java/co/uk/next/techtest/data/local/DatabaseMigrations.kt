package co.uk.next.techtest.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS bag_products (
                    id INTEGER NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    brand TEXT,
                    price REAL,
                    thumbnailUrl TEXT,
                    discountPercentage REAL,
                    rating REAL,
                    stock INTEGER,
                    addedAtMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }
