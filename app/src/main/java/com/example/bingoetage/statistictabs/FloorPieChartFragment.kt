package com.example.bingoetage.statistictabs

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import com.example.bingoetage.R
import com.example.bingoetage.databinding.FragmentFloorPieChartBinding
import com.example.bingoetage.viewmodel.BingoGrid
import com.example.bingoetage.viewmodel.BingoViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [FloorPieChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class FloorPieChartFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val viewModel: BingoViewModel by activityViewModels()

    private var _binding: FragmentFloorPieChartBinding? = null
    private val binding get() = _binding!!
    private var _yearSpinner: Spinner? = null
    private val yearSpinner get() = _yearSpinner!!

    private var _floorPieChart: PieChart? = null
    private val floorPieChart get() = _floorPieChart!!

    private var bingoGridList: LiveData<List<BingoGrid>>? = null

    @ColorInt private var textColor = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFloorPieChartBinding.inflate(inflater, container, false)

        _floorPieChart = binding.floorPieChart

        setPieChartSettings()

        setYearSpinnerSettings()

        return binding.root
    }

    private fun setPieChartSettings()
    {
        // Get colors from theme for bar chart display
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.stat_tab_text_color, typedValue, true)
        textColor = typedValue.data

        // display settings
        floorPieChart.animateY(1000, Easing.EaseInOutQuad)
        floorPieChart.legend.isEnabled = false
        floorPieChart.isDrawHoleEnabled = false
        floorPieChart.description.isEnabled = false
        floorPieChart.isRotationEnabled = false

        // entry label styling
        floorPieChart.setEntryLabelColor(textColor)
        floorPieChart.setEntryLabelTextSize(20f)
    }

    private fun setYearSpinnerSettings()
    {
        // Year spinner
        // On year list data update, change the year list in the spinner
        // On selection change, change the livedata observed for graph update
        _yearSpinner = binding.yearSpinner
        yearSpinner.onItemSelectedListener = this
        viewModel.getDistinctYears().observe(
            viewLifecycleOwner
        ) { yearList ->

            val valueList = yearList.map { it.toString() }.toMutableList()
            val currentYear = viewModel.currentDate.value?.get(Calendar.YEAR)?.toString()

            valueList.add(0, resources.getString(R.string.year_spinner_all_option))

            if (valueList.count() == 1)
                currentYear?.let { valueList.add(it) }

            val previouslySelectedValue =
                yearSpinner.selectedItem?.toString() ?: currentYear

            val valueToSelect = (
                    if (valueList.contains(previouslySelectedValue)) previouslySelectedValue else currentYear
                    ) ?: valueList[0]

            val adapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    valueList
                )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            yearSpinner.adapter = adapter
            yearSpinner.setSelection(valueList.indexOf(valueToSelect))
        }
    }

    // Observe the livedata corresponding to the year and update the chart
    private fun updatePieChartValues(allYears: Boolean, year: Int?=null)
    {
        bingoGridList?.removeObservers(viewLifecycleOwner)

        if (allYears) bingoGridList = viewModel.getEditingBingoGrids(false)
        else if (year != null) bingoGridList = viewModel.getYearEditingBingoGrids(year, false)

        bingoGridList?.observe(
            viewLifecycleOwner
        ) { bingoGridList ->
            updatePieChartDisplay(getListForFPC(bingoGridList))
        }
    }

    // Transform the data to a list usable by the chart
    private fun getListForFPC(bingoGridList: List<BingoGrid>?): List<PieEntry>
    {
        val floorCount = mutableMapOf<String, Int>()

        bingoGridList?.forEach { bingoGrid ->
            bingoGrid.checkedArrayInput
                .forEachIndexed { index, b ->
                    if(b && bingoGrid.numberListShuffledInput[index] != "null")
                    {
                        floorCount[
                                bingoGrid.numberListShuffledInput[index]
                        ] =
                            floorCount[
                                    bingoGrid.numberListShuffledInput[index]
                            ]?.plus(1) ?: 1
                    }
                }
        }

        var total = 0
        floorCount.forEach { (_, count) -> total += count }
        val dataEntries = mutableListOf<PieEntry>()

        floorCount.toSortedMap(compareBy { it })
            .forEach { (floor, count) ->
                dataEntries.add(
                    PieEntry(
                        count.toFloat()/total,
                        floor.toString(),
                    )
                )
            }
        return dataEntries
    }

    // Update the chart display given the list of values
    private fun updatePieChartDisplay(seriesValues: List<PieEntry>)
    {
        val dataSet = PieDataSet(seriesValues, "")

        dataSet.setDrawIcons(false)

        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f


        // add a lot of colors
        val colors = ArrayList<Int>()
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        dataSet.colors = colors

        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(object : ValueFormatter(){
                override fun getFormattedValue(value: Float): String {
                    return "%.1f%%".format(value*100)
                }
            }
        )
        data.setValueTextSize(18f)
        data.setValueTextColor(textColor)
        floorPieChart.data = data

        // undo all highlights
        floorPieChart.highlightValues(null)

        floorPieChart.invalidate()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
        _floorPieChart = null
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
        fun newInstance() = FloorPieChartFragment()
    }

    // Spinner selection logic
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent?.id)
        {
            R.id.yearSpinner ->
                if (position == 0) updatePieChartValues(true)
                else updatePieChartValues(false, parent.getItemAtPosition(position).toString().toInt())
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}