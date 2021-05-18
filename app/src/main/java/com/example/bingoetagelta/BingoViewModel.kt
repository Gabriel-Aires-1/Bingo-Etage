package com.example.bingoetagelta

import android.content.ClipData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class BingoViewModel: ViewModel() {
    private val currentDate = MutableLiveData<Calendar>()
}