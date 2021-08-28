package com.example.bingoetage.statistictabs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import com.example.bingoetage.databinding.FragmentAveragePerMonthBinding
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


/**
 * A simple [Fragment] subclass.
 * Use the [AveragePerMonthFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AveragePerMonthFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val viewModel: BingoViewModel by activityViewModels()

    private var _binding: FragmentAveragePerMonthBinding? = null
    private val binding get() = _binding!!

    private lateinit var graphAveragePerMonths: HorizontalBarChart
    private lateinit var yearSpinner: Spinner
    private var bingoGridList: LiveData<List<BingoGrid>>? = null

    @ColorInt private var textColor = 0
    @ColorInt private var barColor = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Get colors from theme for bar chart display
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.stat_tab_text_color, typedValue, true)
        textColor = typedValue.data
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        barColor = typedValue.data

        // Inflate the layout for this fragment
        _binding = FragmentAveragePerMonthBinding.inflate(inflater, container, false)

        graphAveragePerMonths = binding.graphAveragePerMonths

        // display settings
        graphAveragePerMonths.setDrawBarShadow(false)
        graphAveragePerMonths.setDrawValueAboveBar(true)
        graphAveragePerMonths.description.isEnabled = false
        graphAveragePerMonths.setDrawGridBackground(false)
        graphAveragePerMonths.setFitBars(true)
        graphAveragePerMonths.animateY(1000)

        graphAveragePerMonths.legend.isEnabled = false

        // touch settings
        graphAveragePerMonths.setPinchZoom(false)
        graphAveragePerMonths.isDoubleTapToZoomEnabled = false

        // xAxis settings
        // The xaxis on an horizontal bar chart is on the left (bottom) or right (top)
        // The granularity controls the minimum interval between 2 values
        val xl = graphAveragePerMonths.xAxis
        xl.position = XAxisPosition.BOTTOM
        xl.setDrawAxisLine(false)
        xl.setDrawGridLines(false)
        xl.granularity = 1f
        xl.labelCount = 12

        xl.textColor = textColor
        // the value formatter controls the value display
        // In this case, it converts from float to month string
        xl.valueFormatter = object : ValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                val calFmt = Calendar.getInstance()
                // Negative month value for top to bottom ordering in chart
                calFmt.set(Calendar.MONTH, -value.toInt())
                return String.format("%1\$tb", calFmt)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }

        // Y axes settings
        val ylr = graphAveragePerMonths.axisRight
        ylr.setDrawAxisLine(true)
        ylr.setDrawGridLines(true)
        ylr.axisMinimum = 0f
        ylr.textColor = textColor
        ylr.granularity = 1f
        val yll = graphAveragePerMonths.axisLeft
        yll.setDrawAxisLine(true)
        yll.setDrawGridLines(true)
        yll.axisMinimum = 0f
        yll.granularity = 1f
        yll.textColor = textColor

        // Marker view controls the floating windows displayed on value selection
        val mv = XYMarkerView(requireContext(),
            object : ValueFormatter(){
                override fun getFormattedValue(value: Float): String {
                    val calFmt = Calendar.getInstance()
                    // Negative month value for top to bottom ordering in chart
                    calFmt.set(Calendar.MONTH, -value.toInt())
                    return String.format("%1\$tB", calFmt)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }
            })
        mv.chartView = graphAveragePerMonths // For bounds control
        graphAveragePerMonths.marker = mv


        // Year spinner
        // On year list data update, change the year list in the spinner
        // On selection change, change the livedata observed for graph update
        yearSpinner = binding.yearSpinner
        yearSpinner.onItemSelectedListener = this
        viewModel.getDistinctYears().observe(
            viewLifecycleOwner,
            { yearList ->

                val mutableYearList = yearList.toMutableList()
                val selectedYear = yearSpinner.selectedItem?.toString()?.toInt() ?: viewModel.currentDate.value!!.get(Calendar.YEAR)

                if (mutableYearList.isEmpty())
                    viewModel.currentDate.value?.let { mutableYearList.add(it.get(Calendar.YEAR)) }
                val adapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableYearList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                yearSpinner.adapter = adapter
                yearSpinner.setSelection(mutableYearList.indexOf(selectedYear))
            }
        )

        binding.typeSpinner.onItemSelectedListener = this

        return binding.root
    }

    // Observe the livedata corresponding to the year and update the chart
    private fun updateBarChartValues(year: Int?)
    {
        bingoGridList?.removeObservers(viewLifecycleOwner)
        if(year != null)
        {
            bingoGridList = viewModel.getYearEditingBingoGrids(year, false)
        }
        bingoGridList?.observe(
            viewLifecycleOwner,
            { bingoGridList ->
                updateBarChartDisplay(
                    getListForGraphAPM(bingoGridList),
                    year.toString(),
                )
            }
        )
    }

    // Update the chart display given the list of values
    private fun updateBarChartDisplay(seriesValues: List<BarEntry>, seriesName: String)
    {
        val dataSet: BarDataSet

        if (graphAveragePerMonths.data != null &&
            graphAveragePerMonths.data.dataSetCount > 0
        )
        {
            dataSet = graphAveragePerMonths.data.getDataSetByIndex(0) as BarDataSet
            dataSet.values = seriesValues
            graphAveragePerMonths.data.notifyDataChanged()
            graphAveragePerMonths.notifyDataSetChanged()
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

            graphAveragePerMonths.data = data
        }
    }

    // Transform the data to a list usable by the chart
    private fun getListForGraphAPM(bingoGridList: List<BingoGrid>?): List<BarEntry>
    {
        fun bingoGridValue(bingoGrid: BingoGrid) =
            when(binding.typeSpinner.selectedItemPosition)
            {
                0 -> {bingoGrid.totalValue}
                1 -> {bingoGrid.checkedArrayInput.count { it }}
                else -> {bingoGrid.totalValue}
            }

        val sumResultAndCountPerMonths = mutableMapOf<Int, Pair<Int, Int>>()

        bingoGridList?.forEach { bingoGrid ->
            sumResultAndCountPerMonths[bingoGrid.month] = Pair(
                sumResultAndCountPerMonths[bingoGrid.month]?.first?.plus(bingoGridValue(bingoGrid)) ?: bingoGridValue(bingoGrid),
                sumResultAndCountPerMonths[bingoGrid.month]?.second?.plus(1) ?: 1,
            )
        }

        val sortedAveragePerMonths = mutableMapOf<Int, Float>()
        for (i in 0..11) sortedAveragePerMonths[i] = 0.0f

        sumResultAndCountPerMonths.forEach { (month, sumCount) ->
            sortedAveragePerMonths[month] = sumCount.first / sumCount.second.toFloat()
        }

        val dataEntries = mutableListOf<BarEntry>()
        val calFmt = Calendar.getInstance()

        sortedAveragePerMonths
            .forEach { (month, average) ->
                calFmt.set(Calendar.MONTH, month)

                dataEntries.add(
                    BarEntry(
                        // Negative month value for top to bottom ordering in chart
                        -month.toFloat(),
                        average,
                    )
                )
            }
        // reversed to obtain a rising x-value list to prevent touch events bugs
        return dataEntries.reversed()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment StatFragment.
         */
        @JvmStatic
        fun newInstance() = AveragePerMonthFragment()
    }

    // Spinner selection logic
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent?.id)
        {
            R.id.yearSpinner -> updateBarChartValues(
                parent.getItemAtPosition(position).toString().toInt()
            )
            R.id.typeSpinner -> updateBarChartValues(
                null
            )
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    // XYMarkerView logic
    @SuppressLint("ViewConstructor")
    class XYMarkerView(context: Context?, private val valueFormatter: ValueFormatter) :
        MarkerView(context, R.layout.custom_hor_bar_chart_marker_view) {
        private val tvContent: TextView = findViewById(R.id.mvContent)
        private val format: DecimalFormat = DecimalFormat("###.0")

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