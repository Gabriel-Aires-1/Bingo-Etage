package com.example.bingoetage.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.example.bingoetage.R
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
    )
        .addMigrations(MIGRATION_1_2)
        .addMigrations(MIGRATION_2_3)
        .build()
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
    fun getEditingBingoGrids(editing: Boolean)=
        bingoGridDAO.loadAllFromEditing(editing)
    fun getYearEditingBingoGrids(year:Int, editing: Boolean)=
        bingoGridDAO.loadAllYearFromEditing(year, editing)
    fun getDistinctYears()=
        bingoGridDAO.loadDistinctYears()
    fun getBingoGridFlow(bingoGridDay: Int, bingoGridMonth: Int, bingoGridYear: Int)=
        bingoGridDAO.loadFlow(bingoGridDay,bingoGridMonth,bingoGridYear)
    suspend fun deleteDay(bingoGridDay: Int, bingoGridMonth: Int, bingoGridYear: Int) =
        bingoGridDAO.deleteDay(bingoGridDay,bingoGridMonth,bingoGridYear)
    suspend fun getAllGrids() = bingoGridDAO.getAllGrids()
    suspend fun replaceGrids(list: List<BingoGrid>) = bingoGridDAO.replaceGrids(list)
    suspend fun deleteDatabase() = bingoGridDAO.deleteDatabase()


    fun getUsername() = pref.getString("username", "")
    fun getLayout() = pref.getString("number_floors", "18")!!

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

    val floorListMap = hashMapOf(
        "21" to listOf("11", "12", "13", "14", "15", "16", "17", "18", "19", "20"),
        "18"  to listOf("11", "12", "13", "14", "15", "16", "17", "null", "null"),
        "17"  to listOf("11", "12", "13", "14", "15", "16", "null", "null", "null"),
    )

    val linesMap = hashMapOf<String, Array<IntArray>?>(
        "21" to line2DArray,
        "18" to line2DArray,
        "17" to line2DArray,
    )

    val columnMap = hashMapOf<String, Array<IntArray>?>(
        "21" to column2DArray,
        "18" to column2DArray,
        "17" to column2DArray,
    )

    val diagMap = hashMapOf<String, Array<IntArray>?>(
        "21" to diag2DArray,
        "18" to diag2DArray,
        "17" to diag2DArray,
    )

    val bonusMap = hashMapOf<String, IntArray?>(
        "21" to bonusArray,
        "18" to null,
        "17" to null,
    )

}