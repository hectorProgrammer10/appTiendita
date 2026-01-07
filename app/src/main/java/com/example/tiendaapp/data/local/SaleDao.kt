package com.example.tiendaapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    // Helper query to update status
    @Query("UPDATE sales SET paymentType = :status WHERE id = :id")
    suspend fun updateSaleStatus(id: String, status: com.example.tiendaapp.domain.model.PaymentType)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: String)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
}
