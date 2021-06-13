package com.example.bingoetagelta.viewmodel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BingoGridDAO
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(bingoGrid: BingoGrid)

    @Query("SELECT * FROM bingoGrid WHERE id = :bingoGridId")
    fun load(bingoGridId: String): Flow<BingoGrid>
}