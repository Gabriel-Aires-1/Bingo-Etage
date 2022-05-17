package com.example.bingoetage.customdialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.example.bingoetage.R
import java.lang.Integer.max


/**
 * Class defining the dialog prompting the user at the first opening of the application
 *
 * @param c: Context in which the dialog is executed
 * @param listener: The FirstOpeningDialogListener used for implementing the callback
 * @param layout: The layout currently selected
 * @param username: The username currently selected
 */
class FirstOpeningDialog(
    private val c: Context,
    private val listener: FirstOpeningDialogListener,
    private val layout: String,
    private val username: String
    )
    : Dialog(c), View.OnClickListener, TextWatcher
{

    private lateinit var yesButton: Button
    private lateinit var cancelButton: Button
    private lateinit var layoutSpinner: Spinner
    private lateinit var usernameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.first_opening_dialog)

        layoutSpinner = findViewById(R.id.layoutSpinner)

        usernameEditText = findViewById(R.id.usernameEditText)
        usernameEditText.addTextChangedListener(this)

        yesButton = findViewById(R.id.yesButton)
        cancelButton = findViewById(R.id.cancelButton)
        yesButton.setOnClickListener(this)
        cancelButton.setOnClickListener(this)

        usernameEditText.setText(username)

        val spinnerValues = c.resources.getStringArray(R.array.floor_layout_entries)
        layoutSpinner.setSelection(max(0,spinnerValues.indexOf(layout)))
    }



    override fun onClick(p0: View?) {
        when(p0?.id)
        {
            R.id.yesButton ->
            {
                listener.onClickPositiveButton(
                    layoutSpinner.selectedItem.toString(),
                    usernameEditText.text.toString()
                )
                dismiss()
            }
            R.id.cancelButton ->
            {
                listener.onClickNegativeButton()
                dismiss()
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // Do nothing
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // Do nothing
    }

    /**
     * Disables the yes button when the username is empty
     */
    override fun afterTextChanged(p0: Editable?) {
        yesButton.isEnabled = p0?.length != 0
    }
}

/**
 * Listener for FirstOpeningDialog
 *
 * onClickPositiveButton method called after click on positive button
 * onClickNegativeButton method called after click on negative button
 */
interface FirstOpeningDialogListener
{
    /**
     * @param floor: The chosen floor value
     * @param username: The chosen username value
     */
    fun onClickPositiveButton(floor:String, username:String)

    /**
     * No values retained
     */
    fun onClickNegativeButton()
}

