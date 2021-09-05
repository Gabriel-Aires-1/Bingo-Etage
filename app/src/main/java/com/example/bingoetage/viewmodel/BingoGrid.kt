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
)

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