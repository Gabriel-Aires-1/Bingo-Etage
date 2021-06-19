package com.example.bingoetagelta.viewmodel

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BingoGridDAO
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bingoGrid: BingoGrid)

    @Delete
    suspend fun delete(bingoGrid: BingoGrid)

    @Query("""
        SELECT * FROM bingoGrid 
        WHERE 
                day = :bingoGridDay 
            AND month = :bingoGridMonth 
            AND year = :bingoGridYear
        """)
    suspend fun load(bingoGridDay: Int, bingoGridMonth: Int, bingoGridYear: Int): BingoGrid

    @Query("""
        SELECT * FROM bingoGrid 
        WHERE month = :bingoGridMonth 
        """)
    suspend fun loadForMonth(bingoGridMonth: Int): List<BingoGrid>
}