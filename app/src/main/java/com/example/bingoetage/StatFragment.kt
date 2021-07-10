package com.example.bingoetage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bingoetage.databinding.FragmentStatBinding
import com.example.bingoetage.viewmodel.BingoGrid
import com.example.bingoetage.viewmodel.BingoViewModel
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [StatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class StatFragment : Fragment() {

    private val viewModel: BingoViewModel by activityViewModels()

    private var _binding: FragmentStatBinding? = null
    private val binding get() = _binding!!

    private lateinit var graphAveragePerMonths: GraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStatBinding.inflate(inflater, container, false)

        graphAveragePerMonths = binding.graphAveragePerMonths
        graphAveragePerMonths.title = resources.getString(R.string.average_graph_title)
        graphAveragePerMonths.viewport.isScrollable = true
        graphAveragePerMonths.viewport.isXAxisBoundsManual = true
        graphAveragePerMonths.viewport.setMinX(202100.0)
        graphAveragePerMonths.viewport.setMaxX(202105.0)
        graphAveragePerMonths.gridLabelRenderer.horizontalAxisTitle = resources.getString(R.string.average_graph_x_title)
        graphAveragePerMonths.gridLabelRenderer.verticalAxisTitle = resources.getString(R.string.average_graph_y_title)
        graphAveragePerMonths.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.BOTH
        graphAveragePerMonths.gridLabelRenderer.numHorizontalLabels = 6

        graphAveragePerMonths.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    // X values month and year
                    val year = value.toInt()/100
                    val month = value.toInt() % 100

                    val calFmt = Calendar.getInstance()
                    calFmt.set(Calendar.MONTH, month)
                    calFmt.set(Calendar.YEAR, year)

                    resources.getString(R.string.average_graph_x_label_format)
                        .format(
                            //((value.toInt() + 1) % 100).toString(),
                            String.format("%1\$tb", calFmt)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                            (value.toInt()/100).toString()
                        )
                } else {
                    // y values converted to Int
                    value.toInt().toString()
                }
            }
        }

        // Setup observer for month average graph view
        viewModel.getEditingBingoGrids(false).observe(
            viewLifecycleOwner,
            { bingoGridList -> updateGraphAPM(bingoGridList) }
        )

        return binding.root
    }

    private fun updateGraphAPM(bingoGridList: List<BingoGrid>?)
    {
        val sumResultAndCountPerMonths = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()
        bingoGridList?.forEach { bingoGrid ->
            val yearMonthPair = Pair<Int, Int>(bingoGrid.year, bingoGrid.month)
            sumResultAndCountPerMonths[yearMonthPair] = Pair(
                sumResultAndCountPerMonths[yearMonthPair]?.first?.plus(bingoGrid.totalValue) ?: bingoGrid.totalValue,
                sumResultAndCountPerMonths[yearMonthPair]?.second?.plus(1) ?: 1,
            )
        }

        val averagePerMonths = mutableMapOf<Pair<Int, Int>, Double>()
        sumResultAndCountPerMonths.forEach { (yearMonth, sumCount) ->
            averagePerMonths[yearMonth] = sumCount.first / sumCount.second.toDouble()
        }

        val datapoints = mutableListOf<DataPoint>()

        averagePerMonths
            .toSortedMap(compareBy({it.first}, {it.second}))
            .forEach { (yearMonth, average) ->
                datapoints.add(
                    DataPoint(
                        yearMonth.first*100+yearMonth.second.toDouble(),
                        average
                    )
                )
            }

        val series = BarGraphSeries(
            datapoints.toTypedArray()
        )

        graphAveragePerMonths.addSeries(series)
        graphAveragePerMonths.viewport.setMinY(0.0)
        graphAveragePerMonths.viewport.isYAxisBoundsManual = true
        series.isDrawValuesOnTop = true
        series.valuesOnTopColor = graphAveragePerMonths.titleColor
        series.spacing = 50
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
        fun newInstance() = StatFragment()
    }
}