package com.example.bingoetage

import android.content.Intent
import android.content.SharedPreferences
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
import com.example.bingoetage.viewmodel.BingoViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalStateException
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener
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
            Toast.makeText(
                this,
                resources.getString(R.string.username_empty_reply),
                Toast.LENGTH_LONG
            ).show()
        }
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
            "number_floors" ->
            {
                reloadBingoGrid()
                reloadBingoGridFragment()
            }
        }
    }

    fun reloadBingoGridFragment(){
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
}