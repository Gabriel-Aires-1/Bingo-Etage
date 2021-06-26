package com.example.bingoetagelta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bingoetagelta.viewmodel.BingoGrid
import com.example.bingoetagelta.viewmodel.BingoViewModel
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import java.time.YearMonth
import java.time.temporal.WeekFields
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
class CalendarFragment2 : Fragment()
{
    private var currentDay: Int = 1
    private var currentMonth: Int = 1
    private var currentYear: Int = 1
    // Views
    private lateinit var calendarView: CalendarView

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
        val fragView = inflater.inflate(R.layout.fragment_calendar2, container, false)

        // calendarView setup
        calendarView = fragView.findViewById(R.id.calendarView2)

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                if (day.owner == DayOwner.THIS_MONTH) {
                    // If date in current month
                    container.layout.visibility = View.VISIBLE
                    container.textView.text = day.date.dayOfMonth.toString()

                    container.attachBingoGrid(viewModel.getDayBingoGrid(
                        day.date.dayOfMonth,
                        day.date.monthValue-1,
                        day.date.year)
                    )
                    container.dayBingoGrid.observe(
                        viewLifecycleOwner,
                        { bingoGrid -> updateDayColor(container, bingoGrid) }
                    )
                }
                else
                {
                    // day not in current month, render invisible
                    container.layout.visibility = View.INVISIBLE
                }
            }
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                container.monthYearTextView.text =
                    resources.getString(R.string.calendar_header_date).
                        format(
                            String.format("%1\$tB", month.yearMonth).capitalize(Locale.ROOT),
                            String.format("%1\$tY", month.yearMonth)
                        )

                container.attachBingoGrids(viewModel.getYearMonthBingoGrids(month.yearMonth.monthValue-1, month.yearMonth.year))
                container.yearMonthBingoGrids.observe(
                    viewLifecycleOwner,
                    { bingoGridList -> updateAverageTextView(container.averageTextView, bingoGridList) }
                )
            }
        }

        // Calendar view setup
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth,lastMonth,firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        // Return fragment
        return fragView
    }

    fun updateAverageTextView(averageTextView: TextView, bingoGridList: List<BingoGrid>?)
    {
        averageTextView.text = resources.getString(
            R.string.calendar_header_average
        ).format(getAverageValue(bingoGridList))
    }

    private fun getAverageValue(bingoGridList: List<BingoGrid>?): Float
    {
        if (bingoGridList == null || bingoGridList.isEmpty()) return 0.0f

        // Count only validated grids
        val filteredBingoGridList = bingoGridList.filterNot { it.editingBoolInput }
        if (filteredBingoGridList.isEmpty()) return 0.0f

        var average = 0.0f
        filteredBingoGridList.forEach { bingoGrid -> average += bingoGrid.totalValue }
        average /= filteredBingoGridList.size
        return average
    }

    fun updateDayColor(container: DayViewContainer, bingoGrid: BingoGrid?)
    {
        if (bingoGrid == null || bingoGrid.editingBoolInput)
        {
            // Not validated or null, indicate to user that something is missing
        }
        else
        {
            // Validated, display color scale
            container.layout.setBackgroundResource(R.color.green)
        }
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
            CalendarFragment2().apply {
                arguments = Bundle().apply {
                    putInt(CURRENT_DAY, currentDay)
                    putInt(CURRENT_MONTH, currentMonth)
                    putInt(CURRENT_YEAR, currentYear)
                }
            }
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
        val layout: ConstraintLayout = view.findViewById(R.id.dayLayout)
        var dayBingoGrid: LiveData<BingoGrid> = MutableLiveData()

        fun attachBingoGrid(bingoGrid: LiveData<BingoGrid>)
        {
            dayBingoGrid = bingoGrid
        }

    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        val monthYearTextView: TextView = view.findViewById(R.id.headerMonthYearText)
        val averageTextView: TextView = view.findViewById(R.id.headerAverageText)
        var yearMonthBingoGrids: LiveData<List<BingoGrid>> = MutableLiveData()

        fun attachBingoGrids(bingoGrids: LiveData<List<BingoGrid>>)
        {
            yearMonthBingoGrids = bingoGrids
        }
    }
}