package com.example.bingoetagelta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.preference.PreferenceFragmentCompat

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
        }

/*        override fun onResume()
        {
            super.onResume()

            // Preference validation
            findPreference<EditTextPreference>("username")
                ?.onPreferenceChangeListener = activity as SettingsActivity
        }*/

    }