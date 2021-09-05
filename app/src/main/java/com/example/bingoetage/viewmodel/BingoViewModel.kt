package com.example.bingoetage.viewmodel

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
    var numberOfButton = repository.floorListMap[repository.getLayout()]!!.size
    val minValue = calculateBingoCount(BooleanArray(10) { false }.toTypedArray(), "21")
    val maxValue = calculateBingoCount(BooleanArray(10) { true }.toTypedArray(), "21")

    // Current date displayed in the app
    // Updated by the CalendarFragment on selection
    private val _currentDate = MutableLiveData(setCalendarTime(Calendar.getInstance()))
    val currentDate: LiveData<Calendar> = _currentDate

    // Current date displayed in the app
    // Updated by the CalendarFragment on selection
    private val _changeSelectedDate = MutableLiveData(setCalendarTime(Calendar.getInstance()))
    val changeSelectedDate: LiveData<Calendar> = _changeSelectedDate

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
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.YEAR, year)
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

    // Update the values of current BingoGrid var and save it to database
    fun updateCheckedValuesAndSave(checkedValues: List<Boolean>, editingBool: Boolean)
    {
        updateCheckedValues(checkedValues, editingBool)
        saveCurrentGrid()
    }

    // Update the values of current BingoGrid var
    private fun updateCheckedValues(checkedValues: List<Boolean>, editingBool: Boolean)
    {
        val tmpBingoGrid = bingoGrid.value!!
        tmpBingoGrid.checkedArrayInput = checkedValues
        tmpBingoGrid.editingBoolInput = editingBool
        tmpBingoGrid.totalValue = calculateBingoCount(checkedValues.toTypedArray(), tmpBingoGrid.layout)
        _bingoGrid.value = tmpBingoGrid
    }

    // generate a bingo grid from current date or return from database
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
        numberOfButton = if (bingoGrid==null) repository.floorListMap[repository.getLayout()]!!.size
                            else bingoGrid!!.numberListShuffledInput.size

        return bingoGrid ?: generateBingoGridFromCurrentDate()
    }

    // Generate bingo grid from current date and username hashcode
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

        val arrayShuffled = repository.floorListMap[repository.getLayout()]!!.toMutableList()
        arrayShuffled.shuffle(Random(getSeed()))

        val checkedStateArray = Array<Boolean>(numberOfButton) { it -> arrayShuffled[it] == "null" }

        return BingoGrid(
            nonNullDay.get(Calendar.DAY_OF_MONTH),
            nonNullDay.get(Calendar.MONTH),
            nonNullDay.get(Calendar.YEAR),
            arrayShuffled,
            checkedStateArray.toList(),
            true,
            calculateBingoCount(checkedStateArray, repository.getLayout()),
            repository.getLayout()
        )
    }

    // Calculate the bingo total
    private fun calculateBingoCount(checkedStateArray: Array<Boolean>, layout: String): Int
    {
        fun loop2DArrayAndSum(array: Array<IntArray>?, value: Int): Int
        {
            array ?: return 0

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

        val line2DArray = repository.linesMap[layout]
        val column2DArray = repository.columnMap[layout]
        val diag2DArray = repository.diagMap[layout]
        val bonusArray = repository.bonusMap[layout]

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
        bonusArray?.let {
            for (caseNum in it)
            {
                if (checkedStateArray[caseNum]) result += bonusValue
            }
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

    // Delete the grid corresponding to these values
    fun deleteGrid(bingoGridDay: Int, bingoGridMonth: Int, bingoGridYear: Int)
    {
        // In main thread in order to update calendar afterwards if needed
        runBlocking { repository.deleteDay(bingoGridDay,bingoGridMonth,bingoGridYear) }
        // Update the values in current bingoGrid var to reflect the database deletion
        reloadBingoGrid()
    }

    // Change the month reflected in currentMonthBingoGrids
    fun changeSelectedMonth(month: Int)
    {
        _currentMonthBingoGrids = repository.getBingoGridsFromMonthFlow(month).distinctUntilChanged().asLiveData()
        currentMonthBingoGrids = _currentMonthBingoGrids
    }

    // Get the grids from database corresponding to given month and year
    fun getYearMonthBingoGrids(month: Int, year: Int) = repository.getBingoGridsFromYearMonthFlow(year,month).distinctUntilChanged().asLiveData()

    // Get the grid from database corresponding to the given day
    fun getDayBingoGrid(day: Int, month: Int, year: Int) = repository.getBingoGridFlow(day, month, year).distinctUntilChanged().asLiveData()

    fun getEditingBingoGrids(editing: Boolean) = repository.getEditingBingoGrids(editing).distinctUntilChanged().asLiveData()

    fun getYearEditingBingoGrids(year:Int, editing: Boolean) = repository.getYearEditingBingoGrids(year, editing).distinctUntilChanged().asLiveData()

    fun getDistinctYears() = repository.getDistinctYears().distinctUntilChanged().asLiveData()

    fun reloadBingoGrid()
    {
        changeCurrentDate(
            currentDate.value!!.get(Calendar.YEAR),
            currentDate.value!!.get(Calendar.MONTH),
            currentDate.value!!.get(Calendar.DAY_OF_MONTH),
        )
    }

    fun changeSelectedDateTo(cal: Calendar)
    {
        _changeSelectedDate.value = cal
    }
}