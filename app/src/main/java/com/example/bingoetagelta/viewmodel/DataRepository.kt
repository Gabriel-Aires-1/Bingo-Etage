package com.example.bingoetagelta.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.example.bingoetagelta.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(@ApplicationContext val context: Context)
{
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val db = Room.databaseBuilder(
        context,
        BingoGridDatabase::class.java, "database-name"
    ).build()
    private val bingoGridDAO = db.bingoGridDAO()

    suspend fun saveBingoGrid(bingoGrid: BingoGrid) = bingoGridDAO.insert(bingoGrid)
    suspend fun getBingoGrids(bingoGridDay: Int, bingoGridMonth: Int, bingoGridYear: Int) =
        bingoGridDAO.load(bingoGridDay,bingoGridMonth,bingoGridYear)
    suspend fun getBingoGridsFromMonth(bingoGridMonth: Int) =
        bingoGridDAO.loadForMonth(bingoGridMonth)
    fun getBingoGridsFromMonthFlow(bingoGridMonth: Int)=
        bingoGridDAO.loadForMonthFlow(bingoGridMonth)
    fun getBingoGridsFromYearMonthFlow(bingoGridYear: Int, bingoGridMonth: Int)=
        bingoGridDAO.loadForYearMonthFlow(bingoGridYear, bingoGridMonth)
    fun getBingoGridFlow(bingoGridDay: Int, bingoGridMonth: Int, bingoGridYear: Int)=
        bingoGridDAO.loadFlow(bingoGridDay,bingoGridMonth,bingoGridYear)


    fun getUsername() = pref.getString("username", "")

    val caseValue = context.resources.getInteger(R.integer.caseValue)
    val lineValue = context.resources.getInteger(R.integer.lineValue)
    val columnValue = context.resources.getInteger(R.integer.columnValue)
    val diagValue = context.resources.getInteger(R.integer.diagValue)
    val bonusValue = context.resources.getInteger(R.integer.bonusValue)

    val line2DArray = arrayOf(
        context.resources.getIntArray(R.array.lineArray_0),
        context.resources.getIntArray(R.array.lineArray_1),
        context.resources.getIntArray(R.array.lineArray_2),
    )
    val column2DArray = arrayOf(
        context.resources.getIntArray(R.array.columnArray_0),
        context.resources.getIntArray(R.array.columnArray_1),
        context.resources.getIntArray(R.array.columnArray_2),
    )
    val diag2DArray = arrayOf(
        context.resources.getIntArray(R.array.diagArray_0),
        context.resources.getIntArray(R.array.diagArray_1),
    )
    val bonusArray = context.resources.getIntArray(R.array.bonusArray)

    val floorList = listOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20)

}