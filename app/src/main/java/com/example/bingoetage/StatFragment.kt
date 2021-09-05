package com.example.bingoetage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.bingoetage.databinding.FragmentStatBinding
import com.example.bingoetage.statistictabs.AveragePerMonthFragment
import com.example.bingoetage.statistictabs.FloorPieChartFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


/**
 * A simple [Fragment] subclass.
 * Use the [StatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class StatFragment : Fragment() {

    private var _binding: FragmentStatBinding? = null
    private val binding get() = _binding!!

    private var _viewPager: ViewPager2? = null
    private val viewPager get() = _viewPager!!
    private var _viewPagerAdapter: ViewPagerFragmentAdapter? = null
    private val viewPagerAdapter get() = _viewPagerAdapter!!
    private var _tabLayout: TabLayout? = null
    private val tabLayout get() = _tabLayout!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStatBinding.inflate(inflater, container, false)

        _viewPager = binding.viewPagerStat
        _viewPagerAdapter =
            ViewPagerFragmentAdapter(childFragmentManager, lifecycle)

        viewPager.adapter = viewPagerAdapter

        _tabLayout = binding.tabLayoutStat

        TabLayoutMediator(tabLayout, viewPager)
        { tab, position ->
            tab.text = when(position)
            {
                0 -> resources.getString(R.string.average_month_tab_title)
                1 -> resources.getString(R.string.floor_pie_chart_tab_title)
                else -> resources.getString(R.string.unknown_tab)
            }
        }.attach()

        // Loading all tabs at once
        viewPager.offscreenPageLimit = 2
        viewPager.isUserInputEnabled = false

        return binding.root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _viewPagerAdapter = null
        viewPager.adapter = null
        _viewPager = null
        _tabLayout = null
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment StatFragment.
         */
        @JvmStatic
        fun newInstance() = StatFragment()
    }


    class ViewPagerFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle)
    {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment
        {
            return when(position)
            {
                0 -> AveragePerMonthFragment.newInstance()
                1 -> FloorPieChartFragment.newInstance()
                else -> Fragment()
            }
        }
    }
}