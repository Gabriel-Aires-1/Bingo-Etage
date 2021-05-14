package com.example.bingoetagelta

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        // ViewPager
        val viewPager = findViewById<ViewPager2>(R.id.view_pager_main)
        val viewPagerAdapter = ViewPagerFragmentAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = viewPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout_main)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position){
                0 -> resources.getString(R.string.first_tab_name)
                1 -> resources.getString(R.string.second_tab_name)
                else -> resources.getString(R.string.unknown_tab)
            }
            //viewPager.setCurrentItem(tab.position, true)
        }.attach()

        usernameCheck()
    }

    private fun usernameCheck(){
        // Check if username is setup
        if (PreferenceManager.getDefaultSharedPreferences(this)
            .getString("username","") == ""){
            Toast.makeText(
                this,
                resources.getString(R.string.username_empty_reply),
                Toast.LENGTH_LONG
            ).show()
        }
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

    class ViewPagerFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        private val fragmentArray = arrayOf<Fragment>( //Initialize fragments views
            BingoFragment.newInstance(
                null,
                null,
                false
            ),
            CalendarFragment.newInstance(
                Calendar.getInstance().get(Calendar.DAY_OF_YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.YEAR)
            ),
        )


        override fun getItemCount(): Int {
            return fragmentArray.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentArray[position]
        }
    }
}