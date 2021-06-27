package com.example.bingoetagelta

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
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

        // Set the day legend to current local
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY,0)
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND,0)
        cal.set(Calendar.MILLISECOND,0)
        fragView.findViewById<LinearLayout>(R.id.legendLayout).children.forEachIndexed { index, view ->
            (view as TextView).apply {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek + index)
                text = String.format("%1\$ta", cal).toUpperCase(Locale.ROOT)
            }
        }

        // Get colors from theme
        val value = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.calendar_day_default_background_color, value, true)
        val defaultBackGroundColor = value.data
        requireContext().theme.resolveAttribute(R.attr.calendar_day_default_text_color, value, true)
        val defaultTextColor = value.data
        requireContext().theme.resolveAttribute(R.attr.calendar_day_background_color_min, value, true)
        val backGroundColorMin = value.data
        requireContext().theme.resolveAttribute(R.attr.calendar_day_background_color_max, value, true)
        val backGroundColorMax = value.data
        requireContext().theme.resolveAttribute(R.attr.calendar_day_text_disabled_color, value, true)
        val textDisabledColor = value.data

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.setDayVar(day)
                container.setColors(defaultBackGroundColor, defaultTextColor, backGroundColorMin, backGroundColorMax, textDisabledColor)

                if (day.owner == DayOwner.THIS_MONTH) {
                    // If date in current month
                    container.attachBingoGrid(viewModel.getDayBingoGrid(
                        day.date.dayOfMonth,
                        day.date.monthValue-1,
                        day.date.year)
                    )
                    container.dayBingoGrid.observe(
                        viewLifecycleOwner,
                        { bingoGrid -> container.updateDayDisplay(bingoGrid) }
                    )
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
        private val textView: TextView = view.findViewById(R.id.dayText)
        private val layout: ConstraintLayout = view.findViewById(R.id.dayLayout)
        private val notifTextView: TextView = view.findViewById(R.id.dayNotification)
        lateinit var day: CalendarDay
        var dayBingoGrid: LiveData<BingoGrid> = MutableLiveData()
        private var defaultBackGroundColor: Int = 0
        private var defaultTextColor: Int = 0
        private var backGroundColorMin: Int = 0
        private var backGroundColorMax: Int = 0
        private var textDisabledColor: Int = 0

        enum class DayDisplay {
            DISABLED, ENABLED, INVISIBLE
        }

        fun attachBingoGrid(bingoGrid: LiveData<BingoGrid>)
        {
            dayBingoGrid = bingoGrid
        }

        fun setDayVar(day: CalendarDay)
        {
            this@DayViewContainer.day = day
            textView.text = day.date.dayOfMonth.toString()
            if (day.owner == DayOwner.THIS_MONTH) setDayDisplay(DayDisplay.DISABLED, null)
                else setDayDisplay(DayDisplay.INVISIBLE, null)
        }

        private fun changeNotificationVisibility(visibility: Boolean)
        {
            notifTextView.visibility = if (visibility) View.VISIBLE else View.INVISIBLE
        }

        private fun setDayDisplay(display: DayDisplay, bingoGrid: BingoGrid?)
        {
            val gradientDrawable = layout.background as GradientDrawable
            when(display)
            {
                DayDisplay.DISABLED ->
                {
                    gradientDrawable.setColor(defaultBackGroundColor)
                    textView.setTextColor(textDisabledColor)
                    changeNotificationVisibility(false)
                    layout.visibility = View.VISIBLE
                }
                DayDisplay.INVISIBLE ->
                {
                    gradientDrawable.setColor(defaultBackGroundColor)
                    textView.setTextColor(defaultTextColor)
                    changeNotificationVisibility(false)
                    layout.visibility = View.INVISIBLE
                }
                DayDisplay.ENABLED ->
                {
                    gradientDrawable.setColor(backGroundColorMax)
                    textView.setTextColor(defaultTextColor)
                    changeNotificationVisibility(bingoGrid?.editingBoolInput ?: false)
                    layout.visibility = View.VISIBLE
                }
            }
        }

        fun updateDayDisplay(bingoGrid: BingoGrid?)
        {
            if (bingoGrid == null)
            {
                setDayDisplay(DayDisplay.DISABLED, null)
            }
            else
            {
                setDayDisplay(DayDisplay.ENABLED, bingoGrid)
            }
        }

        fun setColors(defaultBackGroundColor: Int,defaultTextColor: Int,
                      backGroundColorMin:Int,backGroundColorMax: Int,
                      textDisabledColor: Int)
        {
            this.defaultBackGroundColor=defaultBackGroundColor
            this.defaultTextColor= defaultTextColor
            this.backGroundColorMin=backGroundColorMin
            this.backGroundColorMax=backGroundColorMax
            this.textDisabledColor=textDisabledColor
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