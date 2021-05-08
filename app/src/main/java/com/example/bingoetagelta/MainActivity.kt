package com.example.bingoetagelta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import java.util.*
import kotlin.random.Random


class MainActivity : AppCompatActivity(){
    private lateinit var buttonArray : Array<ToggleButton>
    private val floorNumbers = arrayOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
    private val numberOfButton=10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onStart() {
        super.onStart()
        buttonArray = Array(numberOfButton){ i -> findViewById(
            resources.getIdentifier("button${i + 1}", "id", packageName)
        )
        }
        randomizeText()
    }

    private fun randomizeText(){
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
            val nameHashCode = PreferenceManager.getDefaultSharedPreferences(this)
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

    fun clickButton(view: View){
        calculateBingoCount()
    }

    private fun calculateBingoCount(){

        val caseValue = 1
        val lineValue = 2
        val columnValue = 2
        val diagValue = 2
        val bonusValue = 2

        val buttonStateArray = Array(numberOfButton) { i -> buttonArray[i].isChecked }

        val textVBingoCount = findViewById<TextView>(R.id.textViewBingoCount)


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

        textVBingoCount.text = result.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.setting_menu -> {
            // User chose the "Settings" item, show the app settings UI...
            val intent = Intent()
            intent.setClassName(this, "com.example.bingoetagelta.SettingsActivity")
            startActivity(intent)

            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

/*    Preference preference = findPreference(key);
    preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // Do something extra when the preference has changed.
        }
    });*/
}