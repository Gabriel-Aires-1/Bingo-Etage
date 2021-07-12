package com.example.bingoetage.statistictabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.example.bingoetage.R
import com.example.bingoetage.databinding.FragmentFloorPieChartBinding
import com.example.bingoetage.viewmodel.BingoGrid
import com.example.bingoetage.viewmodel.BingoViewModel
import dagger.hilt.android.AndroidEntryPoint


/**
 * A simple [Fragment] subclass.
 * Use the [FloorPieChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class FloorPieChartFragment : Fragment() {

    private val viewModel: BingoViewModel by activityViewModels()

    private var _binding: FragmentFloorPieChartBinding? = null
    private val binding get() = _binding!!

    private lateinit var floorPieChart: AnyChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFloorPieChartBinding.inflate(inflater, container, false)

        floorPieChart = binding.floorPieChart
        floorPieChart.setProgressBar(binding.progressBar)

        val pie: Pie = AnyChart.pie()

        pie.animation(true)
            .title(resources.getString(R.string.floor_pie_chart_title))
        pie.labels()
            .position("outside")
            .format("\"{%x}\"\\n{%yPercentOfTotal}{decimalsCount:1}%")

        pie.legend(false)

        /*pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)*/

        pie.animation(true)

        floorPieChart.setChart(pie)

        viewModel.getEditingBingoGrids(false).observe(
            viewLifecycleOwner,
            { bingoGridList ->
                APIlib.getInstance().setActiveAnyChartView(floorPieChart)
                pie.data(getListForFPC(bingoGridList)) }
        )

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        APIlib.getInstance().setActiveAnyChartView(floorPieChart)
    }

    private fun getListForFPC(bingoGridList: List<BingoGrid>?): List<DataEntry>
    {
        val floorCount = mutableMapOf<Int, Int>()

        bingoGridList?.forEach { bingoGrid ->
            bingoGrid.checkedArrayInput
                .forEachIndexed { index, b ->
                    if(b)
                    {
                        floorCount[
                                bingoGrid.numberArrayShuffledInput[index]
                        ] =
                            floorCount[
                                    bingoGrid.numberArrayShuffledInput[index]
                            ]?.plus(1) ?: 1
                    }
                }
        }

        val dataEntries = mutableListOf<DataEntry>()

        floorCount.toSortedMap(compareBy { it })
            .forEach { (floor, count) ->
                dataEntries.add(
                    ValueDataEntry(
                        floor,
                        count,
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
        fun newInstance() = FloorPieChartFragment()
    }
}