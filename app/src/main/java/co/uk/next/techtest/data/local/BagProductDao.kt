package co.uk.next.techtest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BagProductDao {
    @Query("SELECT * FROM bag_products ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<BagProductEntity>>

    @Query("SELECT * FROM bag_products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): BagProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BagProductEntity)

    @Query("DELETE FROM bag_products WHERE id = :id")
    suspend fun deleteById(id: Int)
}
