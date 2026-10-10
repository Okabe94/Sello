package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.sello.data.local.entity.OperationReceiptEntity

@Dao
interface OperationReceiptDao {
    /** Fails if the operation already has a receipt; the first one is never replaced. */
    @Insert
    suspend fun insert(receipt: OperationReceiptEntity)

    @Query("SELECT * FROM operation_receipt WHERE operation_id = :operationId")
    suspend fun find(operationId: String): OperationReceiptEntity?
}
