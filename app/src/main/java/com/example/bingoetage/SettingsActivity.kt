package com.example.bingoetage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.bingoetage.updater.*
import kotlinx.coroutines.runBlocking
import java.io.IOError


class SettingsActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
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

    }



    class SettingsFragment : PreferenceFragmentCompat()
    {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            this.findPreference<Preference>("update_now")?.setOnPreferenceClickListener {
                updateApplication()
                true
            }
        }

        /***
         * Starts an application update in background
         * The user will be prompted to download the update (if available) through a versionDialog
         * A toast will be displayed if no update is available if silent is false
         * @param silent        if true, no toast is displayed when the application is up to date
         *                      or if there is no internet connection
         */
        fun updateApplication(silent: Boolean = false)
        {
            // Updater definition
            val updater = GitHubUpdater("Gabriel-Aires-1", "Bingo-Etage")

            // Check update availability
            // On success and if a new version is available, display a dialog to the user
            // On error, display a toast if not silent
            UpdaterHelper.checkUpdate(
                requireContext(),
                updater,
                object: UpdateListener
                {
                    override fun onSuccess(update: UpdateSummaryContainer)
                    {
                        if (UpdaterHelper.isNewVersionAvailable(update))
                        {
                            val versionDialog = VersionDialog(
                                update,
                                UpdaterHelper.getVersionDialogListener(requireContext(), update, updater),
                            )
                            versionDialog.show(parentFragmentManager, "VersionDialog")
                        }
                    }
                    override fun onFailed(error: IOError)
                    {
                        if (!silent)
                            Toast.makeText(
                                context,
                                resources.getString(R.string.toast_update_chack_failed),
                                Toast.LENGTH_LONG
                            ).show()
                    }
                }
            )
        }

/*        override fun onResume()
        {
            super.onResume()

            // Preference validation
            findPreference<EditTextPreference>("username")
                ?.onPreferenceChangeListener = activity as SettingsActivity
        }*/

    }