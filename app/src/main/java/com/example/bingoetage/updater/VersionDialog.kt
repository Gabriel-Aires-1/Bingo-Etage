package com.example.bingoetage.updater

import android.app.Dialog
import android.os.Bundle
import android.text.format.Formatter
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.bingoetage.R
import com.example.bingoetage.databinding.VersionDialogBinding

class VersionDialog(
    val update: UpdateSummaryContainer,
    private val versionDialogListener: VersionDialogListener
    )
    : DialogFragment()
{

    private var _binding: VersionDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)

            _binding = VersionDialogBinding.inflate(requireActivity().layoutInflater)
            builder.setView(binding.root)

            builder.setPositiveButton(
                resources.getString(R.string.version_dialog_positive_button)
            ) { _, _ -> versionDialogListener.onClickPositiveButton() }

            builder.setNegativeButton(
                resources.getString(R.string.version_dialog_negative_button)
            ) { _, _ -> versionDialogListener.onClickNegativeButton() }

            binding.versionNumberTextView.text = resources.getString(R.string.version_dialog_version_label).format(update.versionNumber)
            binding.downloadSizeTextView.text = resources.getString(R.string.version_dialog_download_size_label).format(
                Formatter.formatShortFileSize(requireContext(),update.downloadSizeInBytes)
            )
            binding.patchNoteTextView.text = update.patchNote
            binding.patchNoteTextView.movementMethod = ScrollingMovementMethod.getInstance()

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

}

interface VersionDialogListener
{
    /**
     * onClickPositiveButton method called after click on positive button
     * onClickNegativeButton method called after click on negative button
     */
    fun onClickPositiveButton()
    fun onClickNegativeButton()
}