package com.example.bingoetagelta.viewmodel

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BingoGrid(
    @PrimaryKey(autoGenerate = true) var id: Long,
    var name: String,
    var lastName: String
)