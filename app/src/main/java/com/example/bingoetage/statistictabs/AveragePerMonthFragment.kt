package com.example.bingoetage.statistictabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Bar
import com.anychart.data.Set
import com.example.bingoetage.R
import com.example.bingoetage.databinding.FragmentAveragePerMonthBinding
import com.example.bingoetage.viewmodel.BingoGrid
import com.example.bingoetage.viewmodel.BingoViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


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

    private lateinit var graphAveragePerMonths: AnyChartView
    private lateinit var bar: Bar
    private lateinit var spinner: Spinner
    private var bingoGridList: LiveData<List<BingoGrid>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAveragePerMonthBinding.inflate(inflater, container, false)

        graphAveragePerMonths = binding.graphAveragePerMonths
        graphAveragePerMonths.setProgressBar(binding.progressBar)

        val vertical: Cartesian = AnyChart.vertical()

        vertical.animation(true)
            .title(resources.getString(R.string.average_graph_title))

        val set = Set.instantiate()
        val barData = set.mapAs("{ x: 'x', value: 'value' }")
        bar = vertical.bar(barData)
        bar.labels().format("{%Value}")

        vertical.yScale().minimum(0.0)
        vertical.labels(true)
            .xAxis(true)
            .yAxis(true)

        vertical.xZoom().setToPointsCount(12, false, vertical.xScale())


        graphAveragePerMonths.setChart(vertical)

        spinner = binding.spinner
        spinner.onItemSelectedListener = this
        viewModel.getDistinctYears().observe(
            viewLifecycleOwner,
            { yearList ->

                val mutableYearList = yearList.toMutableList()
                val selectedYear = spinner.selectedItem?.toString()?.toInt() ?: viewModel.currentDate.value!!.get(Calendar.YEAR)

                if (mutableYearList.isEmpty())
                    viewModel.currentDate.value?.let { mutableYearList.add(it.get(Calendar.YEAR)) }
                val adapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableYearList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                spinner.adapter = adapter
                spinner.setSelection(mutableYearList.indexOf(selectedYear))
            }
        )

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        APIlib.getInstance().setActiveAnyChartView(graphAveragePerMonths)
    }

    private fun updateBarChart(year: Int)
    {
        bingoGridList?.removeObservers(viewLifecycleOwner)
        bingoGridList = viewModel.getYearEditingBingoGrids(year, false)
        bingoGridList!!.observe(
            viewLifecycleOwner,
            { bingoGridList ->
                APIlib.getInstance().setActiveAnyChartView(graphAveragePerMonths)
                bar.data(getListForGraphAPM(bingoGridList)) }
        )
    }

    private fun getListForGraphAPM(bingoGridList: List<BingoGrid>?): MutableList<DataEntry>
    {
        val sumResultAndCountPerMonths = mutableMapOf<Int, Pair<Int, Int>>()

        bingoGridList?.forEach { bingoGrid ->
            sumResultAndCountPerMonths[bingoGrid.month] = Pair(
                sumResultAndCountPerMonths[bingoGrid.month]?.first?.plus(bingoGrid.totalValue) ?: bingoGrid.totalValue,
                sumResultAndCountPerMonths[bingoGrid.month]?.second?.plus(1) ?: 1,
            )
        }

        val sortedAveragePerMonths = mutableMapOf<Int, Double>()
        for (i in 0..11) sortedAveragePerMonths[i] = 0.0

        sumResultAndCountPerMonths.forEach { (month, sumCount) ->
            sortedAveragePerMonths[month] = sumCount.first / sumCount.second.toDouble()
        }

        val dataEntries = mutableListOf<DataEntry>()
        val calFmt = Calendar.getInstance()

        sortedAveragePerMonths
            .forEach { (month, average) ->
                calFmt.set(Calendar.MONTH, month)

                dataEntries.add(
                    ValueDataEntry(
                        String.format("%1\$tb", calFmt)
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        average,
                    )
                )
            }
        return dataEntries
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
        APIlib.getInstance().setActiveAnyChartView(null)
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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
            updateBarChart(parent.getItemAtPosition(position).toString().toInt())
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}