package com.example.bingoetage.statistictabs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import com.example.bingoetage.viewmodel.BingoGrid
import com.example.bingoetage.viewmodel.BingoViewModel
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.MPPointF
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.util.*
import androidx.annotation.ColorInt


import android.util.TypedValue
import com.example.bingoetage.R
import com.example.bingoetage.databinding.FragmentAveragePerDayBinding
import com.github.mikephil.charting.charts.BarChart


/**
 * A simple [Fragment] subclass.
 * Use the [AveragePerDayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AveragePerDayFragment : ChartFragment(), AdapterView.OnItemSelectedListener {

    override val viewModel: BingoViewModel by activityViewModels()

    private var _binding: FragmentAveragePerDayBinding? = null
    private val binding get() = _binding!!

    private var _graphAveragePerDays: HorizontalBarChart? = null
    private val graphAveragePerDays get() = _graphAveragePerDays!!
    private var _yearSpinner: Spinner? = null
    private val yearSpinner get() = _yearSpinner!!
    override var bingoGridList: LiveData<List<BingoGrid>>? = null

    @ColorInt private var textColor = 0
    @ColorInt private var barColor = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentAveragePerDayBinding.inflate(inflater, container, false)

        _graphAveragePerDays = binding.graphAveragePerDays

        setBarChartSettings(graphAveragePerDays)

        setYearSpinnerSettings()

        binding.typeSpinner.onItemSelectedListener = this

        return binding.root
    }

    private fun setBarChartSettings(barChart: BarChart)
    {
        // Get colors from theme for bar chart display
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.stat_tab_text_color, typedValue, true)
        textColor = typedValue.data
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        barColor = typedValue.data

        // the value formatter controls the value display
        // In this case, it converts from float to day string
        val valueFormatter = object : ValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                // Negative day value for top to bottom ordering in chart
                val cal = Calendar.getInstance()
                val dayOfWeek = -value.toInt() - 1 + cal.firstDayOfWeek

                cal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                return String.format("%1\$ta", cal)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }


        // display settings
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setFitBars(true)
        barChart.animateY(1000)

        barChart.legend.isEnabled = false

        // touch settings
        barChart.setPinchZoom(false)
        barChart.isDoubleTapToZoomEnabled = false

        // xAxis settings
        // The xaxis on an horizontal bar chart is on the left (bottom) or right (top)
        // The granularity controls the minimum interval between 2 values
        val xl = barChart.xAxis
        xl.position = XAxisPosition.BOTTOM
        xl.setDrawAxisLine(false)
        xl.setDrawGridLines(false)
        xl.granularity = 1f
        xl.labelCount = 7

        xl.textColor = textColor
        // the value formatter controls the value display
        // In this case, it converts from float to month string
        xl.valueFormatter = valueFormatter

        // Y axes settings
        val ylr = barChart.axisRight
        ylr.setDrawAxisLine(true)
        ylr.setDrawGridLines(true)
        ylr.axisMinimum = 0f
        ylr.textColor = textColor
        ylr.granularity = 1f
        val yll = barChart.axisLeft
        yll.setDrawAxisLine(true)
        yll.setDrawGridLines(true)
        yll.axisMinimum = 0f
        yll.granularity = 1f
        yll.textColor = textColor

        // Marker view controls the floating windows displayed on value selection
        val mv = XYMarkerView(requireContext(), valueFormatter)
        mv.chartView = barChart // For bounds control
        barChart.marker = mv
    }

    private fun setYearSpinnerSettings()
    {
        // Year spinner
        // On year list data update, change the year list in the spinner
        // On selection change, change the livedata observed for graph update
        _yearSpinner = binding.yearSpinner
        yearSpinner.onItemSelectedListener = this
        setYearSpinnerYearObserver(yearSpinner)
    }

    override fun updateChartDisplay(bingoGridList: List<BingoGrid>?)
    {
        updateBarChartDisplay(
            getListForGraphAPM(bingoGridList),
            yearSpinner.selectedItem.toString(),
        )
    }

    // Update the chart display given the list of values
    private fun updateBarChartDisplay(seriesValues: List<BarEntry>, seriesName: String)
    {
        val dataSet: BarDataSet

        if (
            graphAveragePerDays.data != null &&
            graphAveragePerDays.data.dataSetCount > 0
        )
        {
            dataSet = graphAveragePerDays.data.getDataSetByIndex(0) as BarDataSet
            dataSet.values = seriesValues
            graphAveragePerDays.data.notifyDataChanged()
            graphAveragePerDays.notifyDataSetChanged()
        }
        else
        {
            dataSet = BarDataSet(seriesValues, seriesName)
            dataSet.setDrawIcons(false)
            dataSet.color = barColor
            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(dataSet)
            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.setValueTextColor(textColor)

            graphAveragePerDays.data = data
        }
    }

    // Transform the data to a list usable by the chart
    private fun getListForGraphAPM(bingoGridList: List<BingoGrid>?): List<BarEntry>
    {
        fun bingoGridValue(bingoGrid: BingoGrid) =
            when(binding.typeSpinner.selectedItemPosition)
            {
                0 -> {bingoGrid.totalValue}
                1 -> {
                    var count = 0
                    bingoGrid.numberListShuffledInput.forEachIndexed { index, s ->
                        if (s != "null" && bingoGrid.checkedArrayInput[index]) count++
                    }
                    count
                }
                else -> {bingoGrid.totalValue}
            }

        val sumResultAndCountPerDays = mutableMapOf<Int, Pair<Int, Int>>()

        val cal = Calendar.getInstance()

        bingoGridList?.forEach { bingoGrid ->
            cal.set(bingoGrid.year, bingoGrid.month, bingoGrid.day)
            val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - cal.firstDayOfWeek + 7) % 7 + 1

            sumResultAndCountPerDays[dayOfWeek] = Pair(
                sumResultAndCountPerDays[dayOfWeek]?.first?.plus(bingoGridValue(bingoGrid)) ?: bingoGridValue(bingoGrid),
                sumResultAndCountPerDays[dayOfWeek]?.second?.plus(1) ?: 1,
            )
        }

        val sortedAveragePerDays = mutableMapOf<Int, Float>()

        sumResultAndCountPerDays.forEach { (day, sumCount) ->
            sortedAveragePerDays[day] = sumCount.first / sumCount.second.toFloat()
        }

        val dataEntries = mutableListOf<BarEntry>()

        sortedAveragePerDays
            .forEach { (day, average) ->
                dataEntries.add(
                    BarEntry(
                        // Negative month value for top to bottom ordering in chart
                        -day.toFloat(),
                        average,
                    )
                )
            }
        // reversed to obtain a rising x-value list to prevent touch events bugs
        return dataEntries.sortedBy { it.x }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
        _graphAveragePerDays = null
        _yearSpinner = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment StatFragment.
         */
        @JvmStatic
        fun newInstance() = AveragePerDayFragment()
    }

    // Spinner selection logic
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val allYears = yearSpinner.selectedItemPosition == 0

        when(parent?.id)
        {
            R.id.yearSpinner ->
                if (allYears) updateChartValues(true)
                else updateChartValues(false, parent.getItemAtPosition(position).toString().toInt())
            R.id.typeSpinner -> updateChartValues(allYears, null)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    // XYMarkerView logic
    @SuppressLint("ViewConstructor")
    class XYMarkerView(context: Context?, private val valueFormatter: ValueFormatter) :
        MarkerView(context, R.layout.custom_hor_bar_chart_marker_view) {
        private val tvContent: TextView = findViewById(R.id.mvContent)
        private val format: DecimalFormat = DecimalFormat("0.0")

        // runs every time the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        override fun refreshContent(e: Entry, highlight: Highlight) {
            tvContent.text = String.format(
                "%s: %s",
                valueFormatter.getFormattedValue(e.x),
                format.format(e.y.toDouble()),
            )
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): MPPointF {
            return MPPointF((-(width * 1.2)).toFloat(), (-height).toFloat())
        }

    }
}