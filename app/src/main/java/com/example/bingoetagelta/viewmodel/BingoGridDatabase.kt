package com.example.bingoetagelta.viewmodel

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [BingoGrid::class], version = 1)
@TypeConverters(Converters::class)
abstract class BingoGridDatabase: RoomDatabase()
{
    abstract fun bingoGridDAO(): BingoGridDAO
}
