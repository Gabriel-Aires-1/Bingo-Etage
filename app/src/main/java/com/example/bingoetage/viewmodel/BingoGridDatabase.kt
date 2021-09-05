package com.example.bingoetage.viewmodel

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BingoGrid::class], version = 3)
@TypeConverters(Converters::class)
abstract class BingoGridDatabase: RoomDatabase()
{
    abstract fun bingoGridDAO(): BingoGridDAO
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the new table
        database.execSQL(
            "CREATE TABLE BingoGrid_new " +
                    "(" +
                    "day INTEGER NOT NULL, " +
                    "month INTEGER NOT NULL, " +
                    "year INTEGER NOT NULL, " +
                    "numberListShuffledInput TEXT NOT NULL, " +
                    "checkedListInput TEXT NOT NULL, " +
                    "editingBoolInput INTEGER NOT NULL, " +
                    "totalValue INTEGER NOT NULL, " +
                    "PRIMARY KEY(day, month, year)" +
                    ")")
        // Copy the data
        database.execSQL(
            "INSERT INTO BingoGrid_new (day, month, year, numberListShuffledInput, checkedListInput, editingBoolInput, totalValue) " +
                    "SELECT day, month, year, numberArrayShuffledInput, checkedArrayInput, editingBoolInput, totalValue FROM BingoGrid")
        // Remove the old table
        database.execSQL("DROP TABLE BingoGrid")
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE BingoGrid_new RENAME TO BingoGrid")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add layout column with a default value of "10"
        database.execSQL(
            "ALTER TABLE BingoGrid " +
                    "ADD COLUMN layout TEXT DEFAULT 10 NOT NULL"
        )
    }
}
