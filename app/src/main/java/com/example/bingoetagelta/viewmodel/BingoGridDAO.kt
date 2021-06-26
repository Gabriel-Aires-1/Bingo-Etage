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

    @Query("""
        SELECT * FROM bingoGrid 
        WHERE month = :bingoGridMonth 
        """)
    fun loadForMonthFlow(bingoGridMonth: Int): Flow<List<BingoGrid>>

    @Query("""
        SELECT * FROM bingoGrid 
        WHERE 
                month = :bingoGridMonth 
            AND year = :bingoGridYear
        """)
    fun loadForYearMonthFlow(bingoGridYear: Int, bingoGridMonth: Int): Flow<List<BingoGrid>>

    @Query("""
        SELECT * FROM bingoGrid 
        WHERE  
                day = :bingoGridDay 
            AND month = :bingoGridMonth 
            AND year = :bingoGridYear
        """)
    fun loadFlow(bingoGridDay: Int, bingoGridMonth: Int, bingoGridYear: Int): Flow<BingoGrid>

    @Query("DELETE FROM bingoGrid")
    suspend fun deleteDatabase()
}