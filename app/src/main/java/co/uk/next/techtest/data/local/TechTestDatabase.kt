package co.uk.next.techtest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedProductEntity::class], version = 1, exportSchema = false)
abstract class TechTestDatabase : RoomDatabase() {
    abstract fun savedProductDao(): SavedProductDao
}
