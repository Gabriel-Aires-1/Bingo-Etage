package com.example.bingoetagelta

import android.app.AlertDialog
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import com.example.bingoetagelta.colors.ColorConverter
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.sqrt


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

    private var selectedDate: DayViewContainer? = null
    private lateinit var todayDate: DayViewContainer
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
        requireContext().theme.resolveAttribute(R.attr.calendar_day_border_color, value, true)
        val borderColor = value.data
        requireContext().theme.resolveAttribute(R.attr.calendar_day_selected_border_color, value, true)
        val selectedBorderColor = value.data
        DayViewContainer.setColors(defaultBackGroundColor, defaultTextColor, backGroundColorMin, backGroundColorMax, textDisabledColor, borderColor, selectedBorderColor)
        DayViewContainer.setMinMaxValues(viewModel.minValue, viewModel.maxValue)


        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.setDayVar(day)

                if (day.owner == DayOwner.THIS_MONTH) {
                    // If date in current month

                    // Attach bingo grid LiveData to DayViewContainer
                    container.attachBingoGrid(viewModel.getDayBingoGrid(
                        day.date.dayOfMonth,
                        day.date.monthValue-1,
                        day.date.year)
                    )

                    // Attach observer to container to update display with value
                    container.dayBingoGrid.observe(
                        viewLifecycleOwner,
                        { bingoGrid -> container.updateDayDisplayWithGrid(bingoGrid) }
                    )

                    // Select the current date if no other date was selected
                    if (day.date.dayOfMonth == currentDay &&
                        day.date.monthValue - 1  == currentMonth &&
                        day.date.year == currentYear)
                    {
                        // At calendar initialisation, the selected date is null
                        // set the selected date to current day
                        if (selectedDate == null)
                            changeSelectedDate(container, day)

                        todayDate = container
                    }

                    // Set OnClickListener to change selected date and update ViewModel
                    container.view.setOnClickListener {
                        changeSelectedDate(container, day)
                    }

                    // Set OnLongClickListener to prompt to remove database row
                    container.view.setOnLongClickListener {
                        deleteDBObject(day)
                        true
                    }
                }
            }
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, month: CalendarMonth) {

                // Set the month and year TextView to current local
                container.monthYearTextView.text =
                    resources.getString(R.string.calendar_header_date).
                        format(
                            String.format("%1\$tB", month.yearMonth).capitalize(Locale.ROOT),
                            String.format("%1\$tY", month.yearMonth)
                        )

                // Attach month bingoGrid LiveData to corresponding header
                container.attachBingoGrids(viewModel.getYearMonthBingoGrids(month.yearMonth.monthValue-1, month.yearMonth.year))

                // Observe LiveData to change the average result
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

    // Method to change the selected date
    // Select the container passed in argument
    // Unselect the container in selectedDate variable
    // Update the viewModel
    private fun changeSelectedDate(container: DayViewContainer, day: CalendarDay)
    {
        selectedDate?.selectDay(false)
        container.selectDay(true)

        selectedDate = container

        // Update the viewModel
        viewModel.changeCurrentDate(
            day.date.year,
            day.date.monthValue - 1,
            day.date.dayOfMonth
        )
    }

    // Function to select current date
    fun setSelectedDateToToday()
    {
        calendarView.notifyDateChanged(LocalDate.now())
        changeSelectedDate(todayDate, todayDate.day)
    }

    // Function to prompt for database row deletion
    // If Yes, delete the row
    // Display is automatically updated
    fun deleteDBObject(day: CalendarDay)
    {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(resources.getString(R.string.delete_DB_object_title))
        alertDialogBuilder.setMessage(resources.getString(R.string.delete_DB_object_message))
        alertDialogBuilder.setPositiveButton(resources.getString(
            R.string.delete_DB_object_yes_button_text)
        ) { _, _ ->
            // Proceed with delete operation
            viewModel.deleteGrid(day.date.dayOfMonth, day.date.monthValue - 1, day.date.year)
        }
        alertDialogBuilder.setNegativeButton(resources.getString(
            R.string.delete_DB_object_no_button_text),
        null)
        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert)
        alertDialogBuilder.show()
    }

    // Update the monthYear TextView with the new average value of corresponding month
    fun updateAverageTextView(averageTextView: TextView, bingoGridList: List<BingoGrid>?)
    {
        averageTextView.text = resources.getString(
            R.string.calendar_header_average
        ).format(getAverageValue(bingoGridList))
    }

    // Calculate the average total value of BingoGrids given in argument
    // Only validated list (editing boolean to false) are accounted for
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

    // Container for DayViews
    class DayViewContainer(view: View) : ViewContainer(view) {
        private val textView: TextView = view.findViewById(R.id.dayText)
        private val layout: ConstraintLayout = view.findViewById(R.id.dayLayout)
        private val notifTextView: TextView = view.findViewById(R.id.dayNotification)
        lateinit var day: CalendarDay
        var dayBingoGrid: LiveData<BingoGrid> = MutableLiveData()
        private val gradientDrawable = layout.background as GradientDrawable

        private var selected = false

        // Reset selected state at initialisation
        init
        {
            selectDay(false)
        }

        // Attach the given BingoGrid LiveData to current DayViewContainer
        fun attachBingoGrid(bingoGrid: LiveData<BingoGrid>)
        {
            dayBingoGrid = bingoGrid
        }

        // Attach the given CalendarDay to current DayViewContainer,
        // update text accordingly and make invisible if not in current month
        fun setDayVar(day: CalendarDay)
        {
            this@DayViewContainer.day = day
            textView.text = day.date.dayOfMonth.toString()
            if (day.owner != DayOwner.THIS_MONTH) makeInvisible()
        }

        // Function to set the notification Visibility
        private fun changeNotificationVisibility(visibility: Boolean)
        {
            notifTextView.visibility = if (visibility) View.VISIBLE else View.INVISIBLE
        }

        // Function to make current DayViewContainer invisible
        fun makeInvisible()
        {
            gradientDrawable.setColor(defaultBackGroundColor)
            textView.setTextColor(defaultTextColor)
            changeNotificationVisibility(false)
            layout.visibility = View.INVISIBLE
        }

        // Function to update the background color in accordance with a given BingoGrid
        // to be called on LiveData update
        fun updateDayDisplayWithGrid(bingoGrid: BingoGrid?)
        {
            layout.visibility = View.VISIBLE
            if (bingoGrid == null)
            {
                // If BingoGrid is null (= no data in database)
                // set display to default, no notifications
                gradientDrawable.setColor(defaultBackGroundColor)
                textView.setTextColor(textDisabledColor)
                changeNotificationVisibility(false)
            }
            else
            {
                // Otherwise, uses BingoGrid value to calculate the background color by interpolation
                gradientDrawable.setColor(
                    ColorConverter.interpolateFromRGB(
                        // Using square root to add more spaces between low values and reduce space between high values
                        // helps using the color range better
                        sqrt((bingoGrid.totalValue - minValue).toFloat() / (maxValue - minValue)),
                        backGroundColorMin,
                        backGroundColorMax,
                    )
                )
                textView.setTextColor(defaultTextColor)
                changeNotificationVisibility(bingoGrid.editingBoolInput)
            }
        }

        // Function to change selected day and display accordingly
        fun selectDay(selected: Boolean)
        {
            this.selected = selected
            if (selected)
            {
                gradientDrawable.setStroke(8, selectedBorderColor)
                textView.setTypeface(null, Typeface.BOLD)
            }
            else
            {
                gradientDrawable.setStroke(2, defaultBorderColor)
                textView.setTypeface(null, Typeface.NORMAL)
            }
        }

        // Companion object to encapsulate glabal variables and functions (setters)
        companion object
        {
            private var defaultBackGroundColor: Int = 0
            private var defaultTextColor: Int = 0
            private var backGroundColorMin: Int = 0
            private var backGroundColorMax: Int = 0
            private var textDisabledColor: Int = 0
            private var defaultBorderColor: Int = 0
            private var selectedBorderColor: Int = 0

            private var minValue = 0
            private var maxValue = 0

            fun setColors(defaultBackGroundColor: Int,defaultTextColor: Int,
                          backGroundColorMin:Int,backGroundColorMax: Int,
                          textDisabledColor: Int,
                          defaultBorderColor: Int, selectedBorderColor: Int)
            {
                this.defaultBackGroundColor=defaultBackGroundColor
                this.defaultTextColor= defaultTextColor
                this.backGroundColorMin=backGroundColorMin
                this.backGroundColorMax=backGroundColorMax
                this.textDisabledColor=textDisabledColor
                this.defaultBorderColor = defaultBorderColor
                this.selectedBorderColor = selectedBorderColor
            }

            fun setMinMaxValues(min: Int, max: Int)
            {
                minValue = min
                maxValue = max
            }
        }
    }

    // Container for MonthViews
    class MonthViewContainer(view: View) : ViewContainer(view) {
        val monthYearTextView: TextView = view.findViewById(R.id.headerMonthYearText)
        val averageTextView: TextView = view.findViewById(R.id.headerAverageText)
        var yearMonthBingoGrids: LiveData<List<BingoGrid>> = MutableLiveData()

        // Attach the given LiveData to current MonthView
        fun attachBingoGrids(bingoGrids: LiveData<List<BingoGrid>>)
        {
            yearMonthBingoGrids = bingoGrids
        }
    }
}