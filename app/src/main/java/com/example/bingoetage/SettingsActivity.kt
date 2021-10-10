package com.example.bingoetage

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.bingoetage.updater.GitHubUpdater
import com.example.bingoetage.updater.UpdateListener
import com.example.bingoetage.updater.UpdateSummaryContainer
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.IOError
import android.widget.Toast

import android.net.NetworkInfo

import android.net.ConnectivityManager
import com.example.bingoetage.updater.UpdaterHelper


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
/*                AppUpdater(requireContext())
                    .setDisplay(Display.DIALOG)
                    .setUpdateFrom(UpdateFrom.GITHUB)
                    .setGitHubUserAndRepo("RedSpheal", "Bingo-Etage")
                    .start()*/
                /*val test = AppUpdaterUtils(requireContext()).withListener(object :
                    AppUpdaterUtils.UpdateListener {
                    override fun onSuccess(update: Update?, isUpdateAvailable: Boolean?) {
                        update ?: return
                        Log.d("AppUpdater Latest Version", update.latestVersion)
                        Log.d("AppUpdater Latest Version Code", update.latestVersionCode.toString())
                        Log.d("AppUpdater Release notes", update.releaseNotes ?: "")
                        Log.d("AppUpdater URL", update.urlToDownload.toString())
                        Log.d("AppUpdater Is update available?", isUpdateAvailable.toString())
                    }

                    override fun onFailed(error: AppUpdaterError?) {
                        Log.d("AppUpdater Error", "Something went wrong")
                    }
                })
                test
                    .setUpdateFrom(UpdateFrom.GITHUB)
                    .setGitHubUserAndRepo("RedSpheal", "Bingo-Etage")
                    .start()*/
                UpdaterHelper().startUpdate(
                    this.requireActivity(),
                    requireContext(),
                    GitHubUpdater("RedSpheal", "Bingo-Etage")
                )
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