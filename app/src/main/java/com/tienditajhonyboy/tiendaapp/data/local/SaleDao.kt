package com.tienditajhonyboy.tiendaapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE workspaceId = :workspaceId ORDER BY date DESC")
    fun getSalesByWorkspace(workspaceId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): SaleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Query("UPDATE sales SET paymentType = :status WHERE id = :id")
    suspend fun updateSaleStatus(id: String, status: com.tienditajhonyboy.tiendaapp.domain.model.PaymentType)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: String)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()

    @Query("DELETE FROM sales WHERE workspaceId = :workspaceId")
    suspend fun deleteSalesByWorkspace(workspaceId: String)

    @Query("DELETE FROM sales WHERE workspaceId = :workspaceId AND paymentType = :paymentType")
    suspend fun deleteSalesByWorkspaceAndType(workspaceId: String, paymentType: com.tienditajhonyboy.tiendaapp.domain.model.PaymentType)
}


