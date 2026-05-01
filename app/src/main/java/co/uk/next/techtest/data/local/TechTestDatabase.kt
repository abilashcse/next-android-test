package co.uk.next.techtest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SavedProductEntity::class, BagProductEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TechTestDatabase : RoomDatabase() {
    abstract fun savedProductDao(): SavedProductDao

    abstract fun bagProductDao(): BagProductDao
}
