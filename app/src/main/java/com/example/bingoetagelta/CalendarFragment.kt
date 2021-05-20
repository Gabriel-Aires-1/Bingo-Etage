package com.example.bingoetagelta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.viewModels
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
class CalendarFragment : Fragment()
{
    private var currentDay: Int = 1
    private var currentMonth: Int = 1
    private var currentYear: Int = 1
    // Views
    private lateinit var calendarView: CalendarView
    private lateinit var averageTextView: TextView

    private val viewModel: BingoViewModel by viewModels()

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
        calendarViewSetup()

        // textView setup
        averageTextView = fragView.findViewById(R.id.textViewCalendar)
        averageTextView.text = resources.getString(R.string.text_calendar_average, "0")

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
}