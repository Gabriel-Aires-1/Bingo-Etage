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
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [StatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class StatFragment : Fragment() {

    private var _binding: FragmentStatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: ViewPagerFragmentAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var averagePerMonthFragment : AveragePerMonthFragment
    private lateinit var floorPieChartFragment : FloorPieChartFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStatBinding.inflate(inflater, container, false)

        viewPager = binding.viewPagerStat
        viewPagerAdapter =
            ViewPagerFragmentAdapter(childFragmentManager, lifecycle)
        averagePerMonthFragment = viewPagerAdapter.getFragment(0) as AveragePerMonthFragment
        floorPieChartFragment = viewPagerAdapter.getFragment(1) as FloorPieChartFragment

        viewPager.adapter = viewPagerAdapter

        tabLayout = binding.tabLayoutStat

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
        private val fragmentArray = arrayOf<Fragment>( //Initialize fragments views
            AveragePerMonthFragment.newInstance(),
            FloorPieChartFragment.newInstance(),
        )

        override fun getItemCount(): Int
        {
            return fragmentArray.size
        }

        override fun createFragment(position: Int): Fragment
        {
            return fragmentArray[position]
        }

        fun getFragment(position: Int): Fragment
        {
            return fragmentArray[position]
        }
    }
}