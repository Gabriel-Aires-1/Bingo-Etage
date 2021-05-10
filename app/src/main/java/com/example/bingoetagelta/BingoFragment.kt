package com.example.bingoetagelta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.preference.PreferenceManager
import java.util.*
import kotlin.random.Random

class BingoFragment : Fragment(), View.OnClickListener {

    private lateinit var buttonArray : Array<ToggleButton>
    private lateinit var textVBingoCount : TextView
    private val floorNumbers = arrayOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
    private val numberOfButton=10

    private val caseValue = 1
    private val lineValue = 2
    private val columnValue = 2
    private val diagValue = 2
    private val bonusValue = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragView = inflater.inflate(R.layout.fragment_bingo, container, false)
        // Input initialization
        buttonArray = Array(numberOfButton){
                i -> fragView.findViewById(
            resources.getIdentifier(
                "button${i + 1}",
                "id",
                context?.packageName
            ))
        }
        for (button in buttonArray) button.setOnClickListener(this)

        textVBingoCount = fragView.findViewById(R.id.textViewBingoCount)

        // Randomize bingo buttons
        randomizeBingoButtons()

        // Initialize TextView
        calculateBingoCount()

        // return the view
        return fragView
    }

    private fun randomizeBingoButtons(){
        fun updateText(button: ToggleButton, newText: String){
            button.text = newText
            button.textOff = newText
            button.textOn = newText
        }
        fun getSeed(): Int{
            val currentDate = Calendar.getInstance()
            // Set to 12:0:0.000
            currentDate.set(Calendar.HOUR_OF_DAY, 12)
            currentDate.set(Calendar.MINUTE, 0)
            currentDate.set(Calendar.SECOND, 0)
            currentDate.set(Calendar.MILLISECOND, 0)
            // Return hashcode
            val nameHashCode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("username","")
                .hashCode()
            return currentDate.hashCode() xor nameHashCode
        }

        val arrayShuffled = floorNumbers.copyOf()
        arrayShuffled.shuffle(Random(getSeed()))

        for ((index, button) in buttonArray.withIndex()) {
            updateText(button, arrayShuffled[index].toString())
        }
    }

    override fun onClick(v: View?) {
        calculateBingoCount()
    }

    private fun calculateBingoCount(){

        val buttonStateArray = Array(numberOfButton) { i -> buttonArray[i].isChecked }

        var result = 0

        for (buttonState in buttonStateArray){
            if (buttonState) result+=caseValue
        }

        // line check
        for (i in 1..3){
            var lineChecked = true
            for (j in 1..3){
                if (!buttonStateArray[(i - 1) * 3 + j - 1]) lineChecked = false
            }
            if (lineChecked) result+=lineValue
        }

        // column check
        for (j in 1..3){
            var lineChecked = true
            for (i in 1..3){
                if (!buttonStateArray[(i - 1) * 3 + j - 1]) lineChecked = false
            }
            if (lineChecked) result+=columnValue
        }

        // diag check
        if (buttonStateArray[0] && buttonStateArray[4] && buttonStateArray[8]) result +=diagValue
        if (buttonStateArray[2] && buttonStateArray[4] && buttonStateArray[6]) result +=diagValue

        // bonus check
        if (buttonStateArray[9]) result += bonusValue

        textVBingoCount.text = resources.getString(R.string.text_bingo_count, result.toString())
    }
}