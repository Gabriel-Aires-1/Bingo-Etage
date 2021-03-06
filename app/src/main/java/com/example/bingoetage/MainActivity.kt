package com.example.bingoetage

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.bingoetage.customdialogs.FirstOpeningDialog
import com.example.bingoetage.customdialogs.FirstOpeningDialogListener
import com.example.bingoetage.updater.*
import com.example.bingoetage.viewmodel.BingoViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOError
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.Period
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    FirstOpeningDialogListener
{
    private var _viewPager: ViewPager2? = null
    private val viewPager get() = _viewPager!!
    private var _viewPagerAdapter: ViewPagerFragmentAdapter? = null
    private val viewPagerAdapter get() = _viewPagerAdapter!!
    private var _tabLayout: TabLayout? = null
    private val tabLayout get() = _tabLayout!!
    private val viewModel: BingoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        // ViewPager
        _viewPager = findViewById<ViewPager2>(R.id.view_pager_main)
        _viewPagerAdapter = ViewPagerFragmentAdapter(supportFragmentManager, lifecycle, viewModel)

        viewPager.adapter = viewPagerAdapter

        _tabLayout = findViewById<TabLayout>(R.id.tab_layout_main)

        TabLayoutMediator(tabLayout, viewPager)
        { tab, position ->
            tab.text = when(position)
                        {
                            0 -> resources.getString(R.string.first_tab_name)
                            1 -> resources.getString(R.string.second_tab_name)
                            2 -> resources.getString(R.string.third_tab_name)
                            else -> resources.getString(R.string.unknown_tab)
                        }
        }.attach()

        // Loading all tabs at once
        viewPager.offscreenPageLimit = 2

        // Set listener for theme preference
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
        applyDayNightMode()

        usernameCheck()
        updateCheck()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        viewPager.adapter = null
        _viewPager = null
        _viewPagerAdapter = null
        _tabLayout = null
    }

    private fun usernameCheck()
    {
        // Check if username is setup
        if (PreferenceManager.getDefaultSharedPreferences(this)
            .getString("username","") == "")
        {
            val dialog = FirstOpeningDialog(
                this,
                this,
                PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("number_floors",resources.getString(R.string.default_layout))
                    ?: resources.getString(R.string.default_layout),
                PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("username","") ?: ""
            )
            dialog.show()
        }
    }

    private fun updateCheck()
    {
        val nowDate = LocalDate.now()
        val lastUpdateDate = LocalDate.ofEpochDay(PreferenceManager.getDefaultSharedPreferences(this).getLong("last_update_date", 0))
        var deltaDay = 0
        var deltaMonth = 0
        when (PreferenceManager.getDefaultSharedPreferences(this)
                .getString("update_frequency_preference","never"))
        {
            "daily" -> deltaDay = 1
            "weekly" -> deltaDay = 7
            "monthly" -> deltaMonth = 1
            "never" -> return
        }
        val delta = Period.between(lastUpdateDate, nowDate)
        if (delta.days >= deltaDay && delta.months >= deltaMonth)
            updateApplication(this, supportFragmentManager, resources, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId)
    {
        R.id.calendar_menu ->
        {
            viewModel.changeSelectedDateTo(Calendar.getInstance())
            viewPager.currentItem = 0
            Toast.makeText(
                this,
                resources.getString(R.string.calendar_toast_text),
                Toast.LENGTH_SHORT
            ).show()

            true
        }
        R.id.setting_menu ->
        {
            // User chose the "Settings" item, show the app settings UI...
            val intent = Intent()
            intent.setClassName(this, "com.example.bingoetage.SettingsActivity")
            startActivity(intent)

            true
        }

        else ->
        {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    class ViewPagerFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, viewModel: BingoViewModel) :
        FragmentStateAdapter(fragmentManager, lifecycle)
    {
        // Retrieve the current date from viewModel
        private val currentDate = viewModel.currentDate.value ?: Calendar.getInstance()

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment
        {
            return when(position)
            {
                0 -> BingoFragment.newInstance(
                    null,
                    null,
                    false
                )
                1 -> CalendarFragment2.newInstance(
                    currentDate.get(Calendar.DAY_OF_MONTH),
                    currentDate.get(Calendar.MONTH),
                    currentDate.get(Calendar.YEAR)
                )
                2-> StatFragment.newInstance()
                else -> Fragment()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?)
    {
        when(key)
        {
            "theme_preference" -> applyDayNightMode()
            "username" -> reloadBingoGrid()
            "number_floors", resources.getString(R.string.csv_import_preference_toggle_name) ->
            {
                reloadBingoGrid()
                reloadBingoGridFragment()
            }
        }
    }

    fun reloadBingoGridFragment(){
        // when the activity reloads the viewPagerAdapter can sometimes be null
        // In this case, do nothing (the view will be updated when the adapter initializes)
        _viewPagerAdapter ?: return
        viewPagerAdapter.notifyItemChanged(0)
    }

    private fun applyDayNightMode()
    {
        AppCompatDelegate.setDefaultNightMode(
            when(PreferenceManager.getDefaultSharedPreferences(this)
                .getString("theme_preference",""))
            {
                // empty string for the first startup
                "system", "" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> throw IllegalStateException("Invalid theme preference value")
            }
        )
    }

    private fun reloadBingoGrid() = viewModel.reloadBingoGrid()

    companion object {
        /***
         * Starts an application update in background
         * The user will be prompted to download the update (if available) through a versionDialog
         * A toast will be displayed if no update is available if silent is false
         * @param silent        if true, no toast is displayed when the application is up to date
         *                      or if there is no internet connection
         */
        fun updateApplication(context: Context, fragmentManager: FragmentManager, resources: Resources, silent: Boolean = false) {
            // Updater definition
            val updater = GitHubUpdater("Gabriel-Aires-1", "Bingo-Etage")

            // Check update availability
            // On success and if a new version is available, display a dialog to the user
            // On error, display a toast if not silent
            UpdaterHelper.checkUpdate(
                context,
                updater,
                object : UpdateListener {
                    override fun onSuccess(update: UpdateSummaryContainer) {
                        // Update last update date
                        PreferenceManager.getDefaultSharedPreferences(context).edit()
                            .putLong("last_update_date", LocalDate.now().toEpochDay())
                            .apply()

                        if (UpdaterHelper.isNewVersionAvailable(update)) {
                            val versionDialog = VersionDialog(
                                update,
                                UpdaterHelper.getVersionDialogListener(
                                    context,
                                    update,
                                    updater
                                ),
                            )
                            versionDialog.show(fragmentManager, "VersionDialog")
                        }
                        else
                        {
                            if (!silent)
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.toast_update_uptodate),
                                    Toast.LENGTH_LONG
                                ).show()
                        }
                    }

                    override fun onFailed(error: IOError) {
                        if (!silent)
                            Toast.makeText(
                                context,
                                resources.getString(R.string.toast_update_check_failed),
                                Toast.LENGTH_LONG
                            ).show()
                    }
                }
            )
        }
    }

    override fun onClickPositiveButton(floor: String, username: String) {
        val prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        prefEditor.putString("number_floors", floor)
        prefEditor.putString("username", username)
        prefEditor.apply()
    }

    override fun onClickNegativeButton() {
        // Do nothing
    }
}