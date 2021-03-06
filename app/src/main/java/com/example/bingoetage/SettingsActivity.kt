package com.example.bingoetage

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.bingoetage.databaseIO.csv.CSVExporter
import com.example.bingoetage.databaseIO.csv.CSVImporter
import com.example.bingoetage.viewmodel.BingoGrid
import com.example.bingoetage.viewmodel.BingoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileReader
import java.io.FileWriter
import java.util.*

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity()
{
    private val viewModel: BingoViewModel by viewModels()

    private lateinit var exportResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var importResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        exportResultLauncher = getExportActivityResultLauncher()
        importResultLauncher = getImportActivityResultLauncher()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        // Add the fragment if not saved
        if (savedInstanceState == null)
        {
            /*supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_layout, SettingsFragment())
                .commit()*/
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<SettingsFragment>(R.id.settings_layout)
            }
        }
        setupToolbar()
    }

    private fun setupToolbar()
    {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    /*override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key)
        {
            "username" -> {
                when (newValue){
                    null, "" ->{
                        Toast.makeText(
                            this,
                            resources.getString(R.string.username_empty_reply),
                            Toast.LENGTH_SHORT).show()
                        false}
                    else -> true
                }
            }
            else -> true
        }
    }*/
    /*** Starts Database export to CSV in background
     * The user will be prompted when the export is over through a toast
     */
    fun exportDatabaseToCSV() = CSVExporter.createFile(
        exportResultLauncher,
        resources.getString(R.string.csv_export_file_format).format(Calendar.getInstance())
    )

    /*** Execute the export on activity callback
     * The user will be prompted when the export is over through a toast
     */
    private fun getExportActivityResultLauncher(): ActivityResultLauncher<Intent>
    {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val fileURI: Uri = result.data?.data ?: return@registerForActivityResult

                var writer: BufferedWriter? = null
                try {
                    val pfd = applicationContext.contentResolver.openFileDescriptor(fileURI, "w")!!
                    writer = BufferedWriter(FileWriter(pfd.fileDescriptor))

                    lifecycleScope.launchWhenResumed {
                        val grids = viewModel.getAllGrids()
                        withContext(Dispatchers.IO) {
                            writer.appendLine(resources.getString(R.string.csv_file_header))
                            grids.forEach { writer.appendLine(it.toCSV()) }
                            writer.flush()
                            writer.close()
                        }
                        Toast.makeText(applicationContext, resources.getString(R.string.csv_export_complete_toast_text), Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    writer?.let {it.flush();it.close()}
                    Toast.makeText(applicationContext, resources.getString(R.string.csv_export_error_toast_text), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    /*** Starts Database export to CSV in background
     * The user will be prompted when the export is over through a toast
     */
    fun importDatabaseToCSV() = CSVImporter.readFile(importResultLauncher)

    /*** Execute the export on activity callback
     * The user will be prompted when the export is over through a toast
     */
    private fun getImportActivityResultLauncher(): ActivityResultLauncher<Intent>
    {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val fileURI: Uri = result.data?.data ?: return@registerForActivityResult

                var reader: BufferedReader? = null
                try {
                    val pfd = applicationContext.contentResolver.openFileDescriptor(fileURI, "r")!!
                    reader = BufferedReader(FileReader(pfd.fileDescriptor))

                    lifecycleScope.launchWhenResumed {
                        viewModel.deleteDatabase()
                        viewModel.replaceGrids(
                            reader.readLines().drop(1).map { BingoGrid.generateFromCSV(it) }
                        )
                        reader.close()
                        toggleCSVLoadPreference()
                        Toast.makeText(applicationContext, resources.getString(R.string.csv_import_complete_toast_text), Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    reader?.close()
                    Toast.makeText(applicationContext, resources.getString(R.string.csv_import_error_toast_text), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleCSVLoadPreference()
    {
        val defSharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val prefStr = resources.getString(R.string.csv_import_preference_toggle_name)
        val toggleValue = defSharedPref.getBoolean(prefStr,false)
        defSharedPref.edit().putBoolean(prefStr,!toggleValue).apply()
    }

    }



    class SettingsFragment : PreferenceFragmentCompat()
    {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            this.findPreference<Preference>("update_now")?.setOnPreferenceClickListener {
                MainActivity.updateApplication(requireContext(), parentFragmentManager, resources)
                true
            }

            this.findPreference<Preference>("export_CSV")?.setOnPreferenceClickListener {
                (activity as? SettingsActivity)?.exportDatabaseToCSV()
                true
            }

            this.findPreference<Preference>("import_CSV")?.setOnPreferenceClickListener {

                val alertDialogBuilder = AlertDialog.Builder(requireContext())

                alertDialogBuilder.setTitle(resources.getString(R.string.csv_import_alert_dialog_title))
                alertDialogBuilder.setMessage(resources.getString(R.string.csv_import_alert_dialog_message))
                alertDialogBuilder.setPositiveButton(resources.getString(
                    R.string.csv_import_alert_dialog_pos_button)
                ) { _, _ -> // Proceed with operation
                    (activity as? SettingsActivity)?.importDatabaseToCSV()
                }
                alertDialogBuilder.setNegativeButton(resources.getString(
                    R.string.csv_import_alert_dialog_neg_button),
                    // Do nothing
                    null)
                alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert)
                alertDialogBuilder.show()

                true
            }
        }

/*        override fun onResume()
        {
            super.onResume()

            // Preference validation
            findPreference<EditTextPreference>("username")
                ?.onPreferenceChangeListener = activity as SettingsActivity
        }*/

    }