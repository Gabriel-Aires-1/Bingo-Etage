package com.example.bingoetage.statistictabs

import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.example.bingoetage.R
import com.example.bingoetage.viewmodel.BingoGrid
import com.example.bingoetage.viewmodel.BingoViewModel
import java.util.*

abstract class ChartFragment: Fragment() {

    protected abstract val viewModel: BingoViewModel
    protected abstract var bingoGridList: LiveData<List<BingoGrid>>?

    protected fun setYearSpinnerYearObserver(yearSpinner: Spinner)
    {
        // On year list data update, change the year list in the spinner
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
    protected fun updateChartValues(allYears: Boolean, year: Int?=null)
    {
        bingoGridList?.removeObservers(viewLifecycleOwner)

        if (allYears) bingoGridList = viewModel.getEditingBingoGrids(false)
        else if (year != null) bingoGridList = viewModel.getYearEditingBingoGrids(year, false)

        bingoGridList?.observe(
            viewLifecycleOwner
        ) { bingoGridList ->
            updateChartDisplay(bingoGridList)
        }
    }

    protected abstract fun updateChartDisplay(bingoGridList: List<BingoGrid>?)

}