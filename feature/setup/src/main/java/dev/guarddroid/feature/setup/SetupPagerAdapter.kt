package dev.guarddroid.feature.setup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SetupPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 8

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> WelcomeFragment()
        1 -> DeviceAnalysisFragment()
        2 -> PermissionsFragment()
        3 -> MasterCodeFragment()
        4 -> AppConfigFragment()
        5 -> ScheduleFragment()
        6 -> SystemRulesFragment()
        7 -> SummaryFragment()
        else -> WelcomeFragment()
    }
}
