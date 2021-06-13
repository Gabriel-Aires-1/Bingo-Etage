package com.example.bingoetagelta.viewmodel

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BingoGrid::class], version = 1)
abstract class BingoGridDatabase: RoomDatabase()
{
    abstract fun bingoGridDAO(): BingoGridDAO
}
