package com.example.bingoetage.statistictabs

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import kotlin.collections.ArrayList


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

    private lateinit var floorPieChart: PieChart

    @ColorInt private var textColor = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Get colors from theme for bar chart display
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.stat_tab_text_color, typedValue, true)
        textColor = typedValue.data
        // Inflate the layout for this fragment
        _binding = FragmentFloorPieChartBinding.inflate(inflater, container, false)

        floorPieChart = binding.floorPieChart

        // display settings
        floorPieChart.animateY(1000, Easing.EaseInOutQuad)
        floorPieChart.legend.isEnabled = false
        floorPieChart.isDrawHoleEnabled = false
        floorPieChart.description.isEnabled = false

        // entry label styling
        floorPieChart.setEntryLabelColor(Color.WHITE)
        floorPieChart.setEntryLabelTextSize(20f)

        viewModel.getEditingBingoGrids(false).observe(
            viewLifecycleOwner,
            { bingoGridList ->
                updatePieChartDisplay(getListForFPC(bingoGridList))
            }
        )

        return binding.root
    }

    private fun getListForFPC(bingoGridList: List<BingoGrid>?): List<PieEntry>
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
        data.setValueTextColor(Color.WHITE)
        floorPieChart.data = data

        // undo all highlights
        floorPieChart.highlightValues(null)

        floorPieChart.invalidate()
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
        fun newInstance() = FloorPieChartFragment()
    }
}