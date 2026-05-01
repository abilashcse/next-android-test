package co.uk.next.techtest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedProductDao {
    @Query("SELECT * FROM saved_products ORDER BY savedAtMillis DESC")
    fun observeAll(): Flow<List<SavedProductEntity>>

    @Query("SELECT * FROM saved_products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SavedProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedProductEntity)

    @Query("DELETE FROM saved_products WHERE id = :id")
    suspend fun deleteById(id: Int)
}
