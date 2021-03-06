package com.example.bingoetage

import android.os.Build
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
import com.example.bingoetage.viewmodel.BingoViewModel
import dagger.hilt.android.AndroidEntryPoint
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
    // Rewritten in the onCreate and onCreateView functions
    private var numberOfButton: Int = 0

    private var numberArrayShuffled: Array<String> = Array(numberOfButton){""}
    private var checkedArray: BooleanArray = BooleanArray(numberOfButton)
    private var editingBool: Boolean = false

    private var _buttonArray : Array<ToggleButton>? = null
    private val buttonArray get() = _buttonArray!!
    private var _textVBingoCount : TextView? = null
    private val textVBingoCount get() = _textVBingoCount!!
    private var _okButton : Button? = null
    private val okButton get() = _okButton!!
    private var _editButton: ImageButton? = null
    private val editButton get() = _editButton!!
    private var _textViewDate: TextView? = null
    private val textViewDate get() = _textViewDate!!

    // Maps the layouts in the database to the BingoFragment layouts
    private val layoutsMap = hashMapOf(
        "21" to R.layout.fragment_bingo_21,
        "18"  to R.layout.fragment_bingo_18,
        "17"  to R.layout.fragment_bingo_18,
    )
    // Maps the layouts in the database to the BingoFragment layouts number of buttons
    private val nbButtonMap = hashMapOf(
        "21" to 10,
        "18"  to 9,
        "17"  to 9,
    )
    // Stores the currently loaded layout (default 18)
    private var loadedLayout = "18"

    private val viewModel: BingoViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        numberOfButton = viewModel.bingoGrid.value?.layout.let {
            nbButtonMap[it]
        } ?: nbButtonMap[loadedLayout]!!
        arguments?.let {
            numberArrayShuffled = it.getStringArray(NUMBER_ARRAY_SHUFFLED) ?: Array(numberOfButton){""}
            checkedArray = it.getBooleanArray(CHECKED_ARRAY) ?: BooleanArray(numberOfButton)
            editingBool = it.getBoolean(EDITING_BOOL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        loadedLayout = viewModel.bingoGrid.value?.layout ?: loadedLayout
        numberOfButton = nbButtonMap[loadedLayout]!!

        // Inflate the layout for this fragment
        val fragView = inflater.inflate(layoutsMap[loadedLayout]!!, container, false)
        // Input initialization

        // array of bingo buttons
        _buttonArray = Array(numberOfButton)
        {
                i -> fragView.findViewById(
            resources.getIdentifier(
                "button${i + 1}",
                "id",
                requireContext().packageName
            ))
        }
        for (button in buttonArray) button.setOnClickListener(this)

        // Validation button
        _okButton = fragView.findViewById(R.id.okButton)
        okButton.setOnClickListener(this)

        // Edition button
        _editButton = fragView.findViewById(R.id.editButton)
        editButton.setOnClickListener(this)

        // Bingo value textView
        _textVBingoCount = fragView.findViewById(R.id.textViewBingoCount)

        // Text view for date display
        _textViewDate = fragView.findViewById(R.id.textViewDate)

        // Listen to calendar changes
        viewModel.bingoGrid.observe(
            viewLifecycleOwner
        ) { changeBingoGrid() }

        // return the view
        return fragView
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _buttonArray = null
        _textVBingoCount = null
        _editButton = null
        _okButton = null
        _textViewDate = null
    }

    // Get the viewModel values and update display
    private fun changeBingoGrid()
    {
        setBingoGridBundleVar(
            viewModel.bingoGrid.value!!.numberListShuffledInput.toTypedArray(),
            viewModel.bingoGrid.value!!.checkedArrayInput.toBooleanArray(),
            viewModel.bingoGrid.value!!.editingBoolInput,
        )
        // If the loaded layout and the bingoGrid layout mismatch, reload the fragment
        // Else pursue with the update
        if (loadedLayout != viewModel.bingoGrid.value!!.layout)
        {
            reloadFragment()
        }
        else
        {
            setEditing(editingBool)
            updateDateDisplay()
            updateBingoGridDisplay()
        }
    }

    // Reloads the fragment. Needed in order to update the loaded layout
    private fun reloadFragment()
    {
        // transactions should be sequenced in order to prevent optimization from nullifying them
        val transaction1 = parentFragmentManager.beginTransaction()
        if (Build.VERSION.SDK_INT >= 26) transaction1.setReorderingAllowed(false)
        transaction1.detach(this)

        val transaction2 = parentFragmentManager.beginTransaction()
        if (Build.VERSION.SDK_INT >= 26) transaction2.setReorderingAllowed(false)
        transaction2.attach(this)

        // If the reload is triggered from a preference change
        // The first commitNow will throw java.lang.IllegalStateException
        // In this case, do commit instead of commitNow and the reloadBingoGridFragment will be triggered in the MainActivity
        try
        {
            transaction1.commitNow()
            transaction2.commitNow()
            (activity as MainActivity).reloadBingoGridFragment()
        }catch (e: IllegalStateException)
        {
            transaction1.commit()
            transaction2.commit()
        }
    }

    // Set the variables and bundle to the new values
    private fun setBingoGridBundleVar(numberArrayShuffledInput: Array<String>?,
                                      checkedArrayInput: BooleanArray?,
                                      editingBoolInput: Boolean)
    {
        numberArrayShuffled = numberArrayShuffledInput ?: Array(numberOfButton){""}
        checkedArray = checkedArrayInput ?: BooleanArray(numberOfButton)
        editingBool = editingBoolInput

        arguments = Bundle().apply {
            putStringArray(NUMBER_ARRAY_SHUFFLED, numberArrayShuffled)
            putBooleanArray(CHECKED_ARRAY, checkedArray)
            putBoolean(EDITING_BOOL, editingBool)
        }
    }

    // Update the date display
    private fun updateDateDisplay()
    {
        val date = Calendar.getInstance()
        date.set(Calendar.DAY_OF_MONTH, viewModel.bingoGrid.value!!.day)
        date.set(Calendar.MONTH, viewModel.bingoGrid.value!!.month)
        date.set(Calendar.YEAR, viewModel.bingoGrid.value!!.year)
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
            // If string is null, disable the button and check it
            if (numberArrayShuffled[index] == "null")
            {
                updateText(button, "-")
                button.isEnabled = false
            }
            else
            {
                updateText(button, numberArrayShuffled[index])
            }
            button.isChecked = checkedArray[index]
        }
        updateBingoCountTV()
    }

    // Deal with onClick events
    override fun onClick(v: View?)
    {
        if (v==null) return
        when(v.id)
        {
            R.id.okButton -> editingBool = false
            R.id.editButton -> editingBool = true
        }
        updateBingoCountInVM()
    }

    // Update the total in viewModel
    private fun updateBingoCountInVM()
    {
        // Common part
        // Update the view model with current states (checked buttons)
        // The update of the viewModel calls back the observer on the livedata to update the display
        val buttonStateArray = Array(numberOfButton) { i -> buttonArray[i].isChecked }
        viewModel.updateCheckedValuesAndSave(buttonStateArray.toList(), editingBool)
    }

    // Update the total in the textview
    private fun updateBingoCountTV()
    {
        textVBingoCount.text = resources.getString(
            R.string.text_bingo_count,
            viewModel.bingoGrid.value!!.totalValue.toString()
        )
    }

    // Sets the editing state of the fragment
    // False means all buttons are disabled and the edit button is visible
    // True means the buttons are enabled and the edit button is invisible
    // The okButton state is the contrary of the edit button
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
        fun newInstance(numberArrayShuffled: Array<String>?,
                        checkedArray: BooleanArray?,
                        editingBool: Boolean) =
            BingoFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(NUMBER_ARRAY_SHUFFLED, numberArrayShuffled)
                    putBooleanArray(CHECKED_ARRAY, checkedArray)
                    putBoolean(EDITING_BOOL, editingBool)
                }
            }
    }
}