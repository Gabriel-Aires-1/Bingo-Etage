package com.example.bingoetage.viewmodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverter

@Entity(primaryKeys = ["day", "month", "year"])
data class BingoGrid
    (
    @ColumnInfo(name = "day") var day: Int,
    @ColumnInfo(name = "month") var month: Int,
    @ColumnInfo(name = "year") var year: Int,
    @ColumnInfo(name = "numberListShuffledInput") var numberListShuffledInput: List<String>,
    @ColumnInfo(name = "checkedListInput") var checkedArrayInput: List<Boolean>,
    @ColumnInfo(name = "editingBoolInput") var editingBoolInput: Boolean,
    @ColumnInfo(name = "totalValue") var totalValue: Int,
    @ColumnInfo(name = "layout") var layout: String,
)
{

    fun toCSV(): String
    {
        return "%d;%d;%d;%s;%s;%b;%d;%s".format(day, month, year, numberListShuffledInput.toString(), checkedArrayInput.toString(), editingBoolInput, totalValue, layout)
    }

    companion object
    {
        fun generateFromCSV(csvLine: String): BingoGrid
        {
            fun cleanStr(str:String): String
            {
                return str.trim().removeSurrounding("\"").trim()
            }
            fun strToStrList(str: String): List<String>
            {
                return cleanStr(str).removePrefix("[").removeSuffix("]").split(",")
                    .map { it.trim() }
            }
            fun strToBoolList(str: String): List<Boolean> = strToStrList(str).map { it.toBoolean() }

            val csvLineSplit = csvLine.split(";").map { it.trim() }

            return BingoGrid(
                cleanStr(csvLineSplit[0]).toInt(),
                cleanStr(csvLineSplit[1]).toInt(),
                cleanStr(csvLineSplit[2]).toInt(),
                strToStrList(csvLineSplit[3]),
                strToBoolList(csvLineSplit[4]),
                cleanStr(csvLineSplit[5]).toBoolean(),
                cleanStr(csvLineSplit[6]).toInt(),
                cleanStr(csvLineSplit[7]),
            )
        }
    }
}

class Converters
{
    @TypeConverter
    fun fromStrToListStr(str: String): List<String> = str.split(",")
    @TypeConverter
    fun fromListStrToStr(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun fromStrToListBool(str: String): List<Boolean> = str.split(",").map { it.toBoolean() }
    @TypeConverter
    fun fromListBoolToStr(list: List<Boolean>): String = list.joinToString(",")
}