package com.example.bingoetage.viewmodel

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
        DELETE FROM BingoGrid
        WHERE 
                day = :bingoGridDay 
            AND month = :bingoGridMonth 
            AND year = :bingoGridYear
        """)
    suspend fun deleteDay(bingoGridDay: Int, bingoGridMonth: Int, bingoGridYear: Int)

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

    @Query("""
        SELECT * FROM bingoGrid 
        WHERE 
                editingBoolInput = :editing
        """)
    fun loadAllFromEditing(editing: Boolean): Flow<List<BingoGrid>>

    @Query("""
        SELECT * FROM bingoGrid 
        WHERE 
                year = :year
            AND editingBoolInput = :editing
        """)
    fun loadAllYearFromEditing(year: Int, editing: Boolean): Flow<List<BingoGrid>>

    @Query("""
        SELECT DISTINCT year FROM bingoGrid 
        ORDER BY year DESC
        """)
    fun loadDistinctYears(): Flow<List<Int>>

    @Query("""
        SELECT * FROM bingoGrid
        ORDER BY year, month, day
        """)
    suspend fun getAllGrids(): List<BingoGrid>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceGrids(list: List<BingoGrid>)
}