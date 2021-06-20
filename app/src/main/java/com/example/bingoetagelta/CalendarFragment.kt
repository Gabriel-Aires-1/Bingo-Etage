package com.example.bingoetagelta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.bingoetagelta.viewmodel.BingoGrid
import com.example.bingoetagelta.viewmodel.BingoViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


// the fragment initialization parameters keys
private const val CURRENT_DAY = "currentDay"
private const val CURRENT_MONTH = "currentMonth"
private const val CURRENT_YEAR = "currentYear"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class CalendarFragment : Fragment(), CalendarView.OnDateChangeListener
{
    private var currentDay: Int = 1
    private var currentMonth: Int = 1
    private var currentYear: Int = 1
    // Views
    private lateinit var calendarView: CalendarView
    private lateinit var averageTextView: TextView

    private val viewModel: BingoViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val currentDate = Calendar.getInstance()
            currentDay = it.getInt(CURRENT_DAY, currentDate.get(Calendar.DAY_OF_YEAR))
            currentMonth = it.getInt(CURRENT_MONTH, currentDate.get(Calendar.MONTH))
            currentYear = it.getInt(CURRENT_YEAR, currentDate.get(Calendar.YEAR))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        val fragView = inflater.inflate(R.layout.fragment_calendar, container, false)

        // calendarView setup
        calendarView = fragView.findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener(this)
        calendarViewSetup()

        // textView setup
        averageTextView = fragView.findViewById(R.id.textViewCalendar)
        //averageTextView.text = resources.getString(R.string.text_calendar_average, "0")
        updateAverageTextView(viewModel.currentMonthBingoGrids.value)

        // Observe result changes
        viewModel.currentMonthBingoGrids.observe(
            viewLifecycleOwner,
            { bingoGridList -> updateAverageTextView(bingoGridList) }
        )

        // Return fragment
        return fragView
    }

    private fun calendarViewSetup()
    {
        val currentDate = Calendar.getInstance()
        currentDate.set(Calendar.DAY_OF_YEAR, currentDay)
        currentDate.set(Calendar.MONTH, currentMonth)
        currentDate.set(Calendar.YEAR, currentYear)

        calendarView.setDate(currentDate.timeInMillis, false, false)
    }

    // Sets the date and trigger the event
    fun setDateCalendarView(date: Calendar)
    {
        calendarView.date = date.timeInMillis
        onSelectedDayChange(
            calendarView,
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH),
        )
    }

    private fun updateAverageTextView(bingoGridList: List<BingoGrid>?)
    {
        averageTextView.text = resources.getString(
            R.string.text_calendar_average,
            String.format("%.2f",
                getAverageValue(bingoGridList)
            )
        )
    }

    private fun getAverageValue(bingoGridList: List<BingoGrid>?): Float
    {
        if (bingoGridList == null || bingoGridList.isEmpty()) return 0.0f

        // Count only validated grids
        val filteredBingoGridList = bingoGridList.filterNot { it.editingBoolInput }

        var average = 0.0f
        filteredBingoGridList.forEach { bingoGrid -> average += bingoGrid.totalValue }
        average /= filteredBingoGridList.size
        return average
    }

    companion object
    {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param currentDay Parameter 1 : the current day (int)
         * @param currentMonth Parameter 2 : the current month (int)
         * @param currentYear Parameter 3 : the current year (int)
         * @return A new instance of fragment CalendarFragment.
         */
        @JvmStatic
        fun newInstance(currentDay: Int, currentMonth: Int, currentYear: Int) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putInt(CURRENT_DAY, currentDay)
                    putInt(CURRENT_MONTH, currentMonth)
                    putInt(CURRENT_YEAR, currentYear)
                }
            }
    }

    override fun onSelectedDayChange(view: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
        viewModel.changeCurrentDate(year, month, dayOfMonth)

        // As CalendarView does not provide MonthChangeListener
        // Changing the average on SelectedDateChange
        viewModel.changeSelectedMonth(month)
        updateAverageTextView(viewModel.currentMonthBingoGrids.value)
        viewModel.currentMonthBingoGrids.observe(
            viewLifecycleOwner,
            { bingoGridList -> updateAverageTextView(bingoGridList) }
        )
    }
}