package com.example.bingoetagelta.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BingoViewModel @Inject constructor(
    //savedStateHandle: SavedStateHandle,
    private var repository: DataRepository
    ): ViewModel()
{
    private val _currentDate = MutableLiveData(setCalendarTime(Calendar.getInstance()))
    val currentDate: LiveData<Calendar> = _currentDate

    private val _bingoGrid = MutableLiveData(generateBingoGrid())

    val bingoGrid: LiveData<BingoGrid> = _bingoGrid

    val numberOfButton = 10

    fun changeCurrentDate(year: Int, month: Int, dayOfMonth: Int)
    {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        setCalendarTime(cal)
        _currentDate.value = cal
        _bingoGrid.value = generateBingoGrid()
    }

    private fun setCalendarTime(cal: Calendar): Calendar
    {
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun updateCheckedValues(checkedValues: List<Boolean>, editingBool: Boolean)
    {
        val tmpBingoGrid = bingoGrid.value!!
        tmpBingoGrid.checkedArrayInput = checkedValues
        tmpBingoGrid.editingBoolInput = editingBool
        tmpBingoGrid.totalValue = calculateBingoCount(checkedValues.toTypedArray())
        _bingoGrid.value = tmpBingoGrid
        saveCurrentGrid()
    }

    private fun generateBingoGrid(): BingoGrid
    {
        var bingoGrid : BingoGrid?
        runBlocking {
            bingoGrid = repository.getBingoGrids(
                currentDate.value!!.get(Calendar.DAY_OF_MONTH),
                currentDate.value!!.get(Calendar.MONTH),
                currentDate.value!!.get(Calendar.YEAR),
            )
        }
        return bingoGrid ?: generateBingoGridFromCurrentDate()
    }

    private fun generateBingoGridFromCurrentDate(): BingoGrid
    {
        val nonNullDay = currentDate.value ?: Calendar.getInstance()

        fun getSeed(): Int
        {
            // Set to 12:0:0.000
            setCalendarTime(nonNullDay)
            // Return hashcode
            var nameHashCode: Int = 0
            nameHashCode = repository.getUsername().hashCode()
            return nonNullDay.hashCode() xor nameHashCode
        }

        val arrayShuffled = mutableListOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
        arrayShuffled.shuffle(Random(getSeed()))

        return BingoGrid(
            nonNullDay.get(Calendar.DAY_OF_MONTH),
            nonNullDay.get(Calendar.MONTH),
            nonNullDay.get(Calendar.YEAR),
            arrayShuffled,
            BooleanArray(numberOfButton).toList(),
            false,
            0
        )
    }

    private fun calculateBingoCount(checkedStateArray: Array<Boolean>): Int
    {
        fun loop2DArrayAndSum(array: Array<IntArray>, value: Int): Int
        {
            var result = 0
            for (line in array)
            {
                var lineChecked = true
                for (caseNum in line)
                {
                    if (!checkedStateArray[caseNum]) lineChecked = false
                }
                if (lineChecked) result+=value
            }
            return result
        }

        val caseValue = repository.caseValue
        val lineValue = repository.lineValue
        val columnValue = repository.columnValue
        val diagValue = repository.diagValue
        val bonusValue = repository.bonusValue

        val line2DArray = repository.line2DArray
        val column2DArray = repository.column2DArray
        val diag2DArray = repository.diag2DArray
        val bonusArray = repository.bonusArray

        var result = 0

        for (buttonState in checkedStateArray)
        {
            if (buttonState) result+=caseValue
        }

        // line check
        result += loop2DArrayAndSum(line2DArray,lineValue)

        // column check
        result += loop2DArrayAndSum(column2DArray,columnValue)

        // diag check
        result += loop2DArrayAndSum(diag2DArray,diagValue)

        // bonus check
        for (caseNum in bonusArray)
        {
            if (checkedStateArray[caseNum]) result += bonusValue
        }

        return result
    }

    fun saveCurrentGrid()
    {
        viewModelScope.launch(Dispatchers.IO)
        {
            repository.saveBingoGrid(bingoGrid.value!!)
        }
    }
}