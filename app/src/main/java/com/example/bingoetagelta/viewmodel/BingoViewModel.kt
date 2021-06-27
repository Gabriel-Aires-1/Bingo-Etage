package com.example.bingoetagelta.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
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
    // Number of bingo buttons
    val numberOfButton = repository.floorList.size

    // Current date displayed in the app
    // Updated by the CalendarFragment on selection
    private val _currentDate = MutableLiveData(setCalendarTime(Calendar.getInstance()))
    val currentDate: LiveData<Calendar> = _currentDate

    // Database object
    // Updated when the date has been changed or when the user changed selection
    // Observed by the BingoFragment to update display
    private val _bingoGrid = MutableLiveData(generateBingoGrid())
    val bingoGrid: LiveData<BingoGrid> = _bingoGrid

    // Current selected month in calendarView results as liveData
    private var _currentMonthBingoGrids = repository.getBingoGridsFromMonthFlow(
            currentDate.value!!.get(Calendar.MONTH)
        ).distinctUntilChanged().asLiveData()
    var currentMonthBingoGrids: LiveData<List<BingoGrid>> = _currentMonthBingoGrids

    // Update the current date and regenerate the grid in accordance
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

    // sets the time to a standard value in order
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
            val nameHashCode = repository.getUsername().hashCode()
            return nonNullDay.hashCode() xor nameHashCode
        }

        val arrayShuffled = repository.floorList.toMutableList()
        arrayShuffled.shuffle(Random(getSeed()))

        return BingoGrid(
            nonNullDay.get(Calendar.DAY_OF_MONTH),
            nonNullDay.get(Calendar.MONTH),
            nonNullDay.get(Calendar.YEAR),
            arrayShuffled,
            BooleanArray(numberOfButton).toList(),
            true,
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

    // Save the current BingoGrid to database
    private fun saveCurrentGrid()
    {
        viewModelScope.launch(Dispatchers.IO)
        {
            repository.saveBingoGrid(bingoGrid.value!!)
        }
    }

    // Change the month reflected in currentMonthBingoGrids
    fun changeSelectedMonth(month: Int)
    {
        _currentMonthBingoGrids = repository.getBingoGridsFromMonthFlow(month).distinctUntilChanged().asLiveData()
        currentMonthBingoGrids = _currentMonthBingoGrids
    }

    fun getYearMonthBingoGrids(month: Int, year: Int) = repository.getBingoGridsFromYearMonthFlow(year,month).distinctUntilChanged().asLiveData()

    fun getDayBingoGrid(day: Int, month: Int, year: Int) = repository.getBingoGridFlow(day, month, year).distinctUntilChanged().asLiveData()
}