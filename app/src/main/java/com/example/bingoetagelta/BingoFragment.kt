package com.example.bingoetagelta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.activityViewModels
import com.example.bingoetagelta.viewmodel.BingoViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

// the fragment initialization parameters keys
private const val NUMBER_ARRAY_SHUFFLED = "NUMBER_ARRAY_SHUFFLED"
private const val CHECKED_ARRAY = "CHECKED_ARRAY"
private const val EDITING_BOOL = "EDITING_BOOL"

/**
 * A simple [Fragment] subclass.
 * Use the [BingoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class BingoFragment : Fragment(), View.OnClickListener
{
    // Rewritten in the onCreate function
    private var numberOfButton: Int = 0

    private var numberArrayShuffled: IntArray = IntArray(numberOfButton)
    private var checkedArray: BooleanArray = BooleanArray(numberOfButton)
    private var editingBool: Boolean = false

    private lateinit var buttonArray : Array<ToggleButton>
    private lateinit var textVBingoCount : TextView
    private lateinit var okButton : Button
    private lateinit var editButton: ImageButton
    private lateinit var textViewDate: TextView

    private val viewModel: BingoViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        numberOfButton = viewModel.numberOfButton
        arguments?.let {
            numberArrayShuffled = it.getIntArray(NUMBER_ARRAY_SHUFFLED) ?: IntArray(numberOfButton)
            checkedArray = it.getBooleanArray(CHECKED_ARRAY) ?: BooleanArray(numberOfButton)
            editingBool = it.getBoolean(EDITING_BOOL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        val fragView = inflater.inflate(R.layout.fragment_bingo, container, false)
        // Input initialization

        // array of bingo buttons
        buttonArray = Array(numberOfButton)
        {
                i -> fragView.findViewById(
            resources.getIdentifier(
                "button${i + 1}",
                "id",
                context?.packageName
            ))
        }
        for (button in buttonArray) button.setOnClickListener(this)

        // Validation button
        okButton = fragView.findViewById(R.id.okButton)
        okButton.setOnClickListener(this)

        // Edition button
        editButton = fragView.findViewById(R.id.editButton)
        editButton.setOnClickListener(this)

        // Bingo value textView
        textVBingoCount = fragView.findViewById(R.id.textViewBingoCount)

        // Text view for date display
        textViewDate = fragView.findViewById(R.id.textViewDate)

        // Initialize TextView and Buttons
        changeBingoGrid()

        // Listen to calendar changes
        viewModel.bingoGrid.observe(
            viewLifecycleOwner,
            { changeBingoGrid() }
        )

        // return the view
        return fragView
    }

    // Get the viewModel values and update display
    private fun changeBingoGrid()
    {
        setBingoGridBundleVar(
            viewModel.bingoGrid.value!!.numberArrayShuffledInput.toIntArray(),
            viewModel.bingoGrid.value!!.checkedArrayInput.toBooleanArray(),
            viewModel.bingoGrid.value!!.editingBoolInput,
        )
        setEditing(editingBool)
        updateDateDisplay()
        updateBingoGridDisplay()
    }

    // Set the variables and bundle to the new values
    private fun setBingoGridBundleVar(numberArrayShuffledInput: IntArray?,
                                      checkedArrayInput: BooleanArray?,
                                      editingBoolInput: Boolean)
    {
        numberArrayShuffled = numberArrayShuffledInput ?: IntArray(numberOfButton)
        checkedArray = checkedArrayInput ?: BooleanArray(numberOfButton)
        editingBool = editingBoolInput

        arguments = Bundle().apply {
            putIntArray(NUMBER_ARRAY_SHUFFLED, numberArrayShuffled)
            putBooleanArray(CHECKED_ARRAY, checkedArray)
            putBoolean(EDITING_BOOL, editingBool)
        }
    }

    // Update the date display
    private fun updateDateDisplay()
    {
        val date = Calendar.getInstance()
        date.set(Calendar.YEAR, viewModel.bingoGrid.value!!.year)
        date.set(Calendar.MONTH, viewModel.bingoGrid.value!!.month)
        date.set(Calendar.DAY_OF_MONTH, viewModel.bingoGrid.value!!.day)
        textViewDate.text = String.format(resources.getString(R.string.date_format), date)
    }

    // Update the bingoFragment display according to the data stored in viewModel
    private fun updateBingoGridDisplay()
    {
        fun updateText(button: ToggleButton, newText: String)
        {
            button.text = newText
            button.textOff = newText
            button.textOn = newText
        }

        for ((index, button) in buttonArray.withIndex())
        {
            updateText(button, numberArrayShuffled[index].toString())
            button.isChecked = checkedArray[index]
        }
        updateBingoCount()
    }

    override fun onClick(v: View?)
    {
        if (v==null) return
        when(v.id)
        {
            R.id.okButton -> editingBool = false
            R.id.editButton -> editingBool = true
        }
        // Common part
        // Update the view model with current states (checked buttons)
        // The update of the viewModel calls back the observer on the livedata to update the display
        val buttonStateArray = Array(numberOfButton) { i -> buttonArray[i].isChecked }
        viewModel.updateCheckedValues(buttonStateArray.toList(), editingBool)
    }

    private fun updateBingoCount()
    {
        textVBingoCount.text = resources.getString(
            R.string.text_bingo_count,
            viewModel.bingoGrid.value!!.totalValue.toString()
        )
    }

    private fun setEditing(edit: Boolean)
    {
        editingBool = edit
        okButton.visibility = if (editingBool) Button.VISIBLE else Button.INVISIBLE
        editButton.visibility = if (!editingBool) Button.VISIBLE else Button.INVISIBLE
        for (button in buttonArray) button.isEnabled = editingBool
    }

    companion object
    {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param numberArrayShuffled Parameter 1 : the bingo grid for the selected day
         * @param checkedArray Parameter 2 : the checked (or not) values
         * @param editingBool Parameter 3 : the editing mode
         * @return A new instance of fragment BingoFragment.
         */
        @JvmStatic
        fun newInstance(numberArrayShuffled: IntArray?,
                        checkedArray: BooleanArray?,
                        editingBool: Boolean) =
            BingoFragment().apply {
                arguments = Bundle().apply {
                    putIntArray(NUMBER_ARRAY_SHUFFLED, numberArrayShuffled)
                    putBooleanArray(CHECKED_ARRAY, checkedArray)
                    putBoolean(EDITING_BOOL, editingBool)
                }
            }
    }
}