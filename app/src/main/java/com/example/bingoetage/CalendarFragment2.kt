package com.example.bingoetage

import android.app.AlertDialog
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bingoetage.colors.ColorConverter
import com.example.bingoetage.databinding.FragmentCalendar2Binding
import com.example.bingoetage.databinding.FragmentCalendar2DayBinding
import com.example.bingoetage.databinding.FragmentCalendar2HeaderBinding
import com.example.bingoetage.viewmodel.BingoGrid
import com.example.bingoetage.viewmodel.BingoViewModel
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.pow


// the fragment initialization parameters keys
private const val CURRENT_DAY = "currentDay"
private const val CURRENT_MONTH = "currentMonth"
private const val CURRENT_YEAR = "currentYear"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class CalendarFragment2 : Fragment()
{
    private var currentDay: Int = 1
    private var currentMonth: Int = 1
    private var currentYear: Int = 1

    private var selectedDate: LocalDate? = null
    private lateinit var todayDate: LocalDate

    // First and last months
    private lateinit var firstMonth: YearMonth
    private lateinit var lastMonth: YearMonth

    // Views
    private var _binding: FragmentCalendar2Binding? = null
    private val binding get() = _binding!!
    private var _calendarView: CalendarView? = null
    private val calendarView get() = _calendarView!!

    private val viewModel: BingoViewModel by activityViewModels()

    // Colors for day view variables
    private var defaultDayBackGroundColor: Int = 0
    private var defaultDayTextColor: Int = 0
    private var dayBackGroundColorMin: Int = 0
    private var dayBackGroundColorMax: Int = 0
    private var dayTextDisabledColor: Int = 0
    private var defaultDayBorderColor: Int = 0
    private var selectedDayBorderColor: Int = 0

    private var dayMinValue = 0
    private var dayMaxValue = 0

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
    ): View
    {
        // Inflate the layout for this fragment
        _binding = FragmentCalendar2Binding.inflate(inflater, container, false)
        val fragView = binding.root

        // calendarView setup
        _calendarView = binding.calendarView2

        // Set the day legend to current local
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY,0)
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND,0)
        cal.set(Calendar.MILLISECOND,0)
        binding.legendLayout.root.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek + index)
                text = String.format("%1\$ta", cal).uppercase(Locale.ROOT)
            }
        }

        // Get colors from theme
        val getColorFromTheme = { resid: Int ->
            val value = TypedValue()
            requireContext().theme.resolveAttribute(resid, value, true)
            value.data
        }
        defaultDayBackGroundColor = getColorFromTheme(R.attr.calendar_day_default_background_color)
        defaultDayTextColor = getColorFromTheme(R.attr.calendar_day_default_text_color)
        dayBackGroundColorMin = getColorFromTheme(R.attr.calendar_day_background_color_min)
        dayBackGroundColorMax = getColorFromTheme(R.attr.calendar_day_background_color_max)
        dayTextDisabledColor = getColorFromTheme(R.attr.calendar_day_text_disabled_color)
        defaultDayBorderColor = getColorFromTheme(R.attr.calendar_day_border_color)
        selectedDayBorderColor = getColorFromTheme(R.attr.calendar_day_selected_border_color)

        dayMinValue = viewModel.minValue
        dayMaxValue = viewModel.maxValue


        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.setDayVar(day)

                if (day.owner == DayOwner.THIS_MONTH) {
                    // If date in current month

                    // Select the current date if no other date was selected
                    if (day.date.dayOfMonth == currentDay &&
                        day.date.monthValue - 1  == currentMonth &&
                        day.date.year == currentYear)
                    {
                        // At calendar initialisation, the selected date is null
                        // set the selected date to current day
                        if (selectedDate == null) selectedDate = day.date

                        todayDate = day.date
                    }

                    // Attach bingo grid LiveData to DayViewContainer
                    container.attachBingoGrid(viewModel.getDayBingoGrid(
                        day.date.dayOfMonth,
                        day.date.monthValue-1,
                        day.date.year)
                    )

                    // Attach observer to container to update display with value
                    container.dayBingoGrid.observe(
                        viewLifecycleOwner
                    ) { bingoGrid -> container.updateDayDisplayWithGrid(bingoGrid) }

                    // Change selected state in accordance with selected date data
                    container.selectDay(selectedDate == day.date)
                }
            }
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, month: CalendarMonth) {

                // tB and tY not working with YearMonth class for API 25 and lower so converting to
                // calendar before formatting
                val calFmt = Calendar.getInstance()
                calFmt.set(Calendar.DAY_OF_MONTH, 1)
                calFmt.set(Calendar.MONTH, month.yearMonth.monthValue - 1)
                calFmt.set(Calendar.YEAR, month.yearMonth.year)

                // Set the month and year TextView to current local
                container.monthYearTextView.text =
                    resources.getString(R.string.calendar_header_date).
                        format(
                            String.format("%1\$tB", calFmt)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                            String.format("%1\$tY", calFmt)
                        )

                // Attach month bingoGrid LiveData to corresponding header
                container.attachBingoGrids(viewModel.getYearMonthBingoGrids(month.yearMonth.monthValue-1, month.yearMonth.year))

                // Observe LiveData to change the average result
                container.yearMonthBingoGrids.observe(
                    viewLifecycleOwner
                ) { bingoGridList ->
                    updateAverageTextView(
                        container.averageTextView,
                        bingoGridList
                    )
                }
            }
        }


        // Calendar view setup
        val currentMonth = YearMonth.now()
        firstMonth = currentMonth.minusMonths(12)
        lastMonth = currentMonth.plusMonths(12)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(firstMonth,lastMonth,firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        // Setup calendarViewScrollMonthListener
        calendarView.monthScrollListener = { month ->
            if(firstMonth.plusMonths(6)>month.yearMonth)
            {
                firstMonth = firstMonth.minusMonths(12)
                calendarView.updateMonthRange(firstMonth, lastMonth)
            }
            else if(lastMonth.minusMonths(6)<month.yearMonth)
            {
                lastMonth = lastMonth.plusMonths(12)
                calendarView.updateMonthRange(firstMonth, lastMonth)
            }
        }

        // Allow to change the selected date from outside event
        viewModel.changeSelectedDate.observe(
            viewLifecycleOwner
        ) { selectedCal ->
            // converts the calendar instance to LocalDate
            val date = LocalDate.of(
                selectedCal.get(Calendar.YEAR),
                selectedCal.get(Calendar.MONTH) + 1,
                selectedCal.get(Calendar.DAY_OF_MONTH),
            )

            // Scroll to date in case it is not currently displayed
            calendarView.scrollToMonth(date.yearMonth)

            changeSelectedDate(date)
        }

        // Return fragment
        return fragView
    }

    // Method to change the selected date
    // Select the container passed in argument
    // Unselect the container in selectedDate variable
    // Update the viewModel
    private fun changeSelectedDate(date: LocalDate)
    {
        val oldDate = selectedDate
        selectedDate = date

        calendarView.notifyDateChanged(date)
        oldDate?.let { calendarView.notifyDateChanged(it) }

        // Update the viewModel
        viewModel.changeCurrentDate(
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
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

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
        _calendarView = null
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
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        private val binding = FragmentCalendar2DayBinding.bind(view)
        private val textView: TextView = binding.dayText
        private val layout: ConstraintLayout = binding.dayLayout
        private val notifTextView: TextView = binding.dayNotification
        private val resultTextView: TextView = binding.dayResult
        private lateinit var day: CalendarDay
        var dayBingoGrid: LiveData<BingoGrid> = MutableLiveData()
        private val gradientDrawable = layout.background as GradientDrawable

        init {
            // Set OnClickListener to change selected date and update ViewModel
            layout.setOnClickListener {
                changeSelectedDate(day.date)
            }

            // Set OnLongClickListener to prompt to remove database row
            layout.setOnLongClickListener {
                deleteDBObject(day)
                true
            }
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

        // Function to set the result Visibility
        private fun changeResultVisibility(visibility: Boolean)
        {
            resultTextView.visibility = if (visibility) View.VISIBLE else View.INVISIBLE
        }

        // Function to make current DayViewContainer invisible
        private fun makeInvisible()
        {
            gradientDrawable.setColor(defaultDayBackGroundColor)
            textView.setTextColor(defaultDayTextColor)
            changeNotificationVisibility(false)
            changeResultVisibility(false)
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
                gradientDrawable.setColor(defaultDayBackGroundColor)
                textView.setTextColor(dayTextDisabledColor)
                changeNotificationVisibility(false)
                changeResultVisibility(false)
            }
            else
            {
                // Otherwise, uses BingoGrid value to calculate the background color by interpolation
                gradientDrawable.setColor(
                    ColorConverter.interpolateFromRGB(
                        // Interpolation between possible score values to add more spaces between
                        // low values and reduce space between high values
                        // helps using the color range better
                        // Possible values :
                        // 2 3 4 5 6 7 8 9 10 11 12 13 15 16 17 18 20 25
                        // Polynomial interpolation : -1.039E-1 + 5.127E-2 * x + 1.202E-3 * x^2 + 5.918E-5 * x^3
                        (
                                -1.039E-1
                                + 5.127E-2 * bingoGrid.totalValue
                                + 1.202E-3 * bingoGrid.totalValue.toFloat().pow(2)
                                - 5.918E-5 * bingoGrid.totalValue.toFloat().pow(3)
                        ).toFloat(),
                        dayBackGroundColorMin,
                        dayBackGroundColorMax,
                    )
                )
                textView.setTextColor(defaultDayTextColor)
                changeNotificationVisibility(bingoGrid.editingBoolInput)
                changeResultVisibility(true)
                resultTextView.text = bingoGrid.totalValue.toString()
            }
        }

        // Function to change selected day and display accordingly
        fun selectDay(selectedState: Boolean)
        {
            if (selectedState)
            {
                gradientDrawable.setStroke(8, selectedDayBorderColor)
                textView.setTypeface(null, Typeface.BOLD)
            }
            else
            {
                gradientDrawable.setStroke(2, defaultDayBorderColor)
                textView.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    // Container for MonthViews
    class MonthViewContainer(view: View) : ViewContainer(view) {
        private val binding = FragmentCalendar2HeaderBinding.bind(view)
        val monthYearTextView: TextView = binding.headerMonthYearText
        val averageTextView: TextView = binding.headerAverageText
        var yearMonthBingoGrids: LiveData<List<BingoGrid>> = MutableLiveData()

        // Attach the given LiveData to current MonthView
        fun attachBingoGrids(bingoGrids: LiveData<List<BingoGrid>>)
        {
            yearMonthBingoGrids = bingoGrids
        }
    }
}