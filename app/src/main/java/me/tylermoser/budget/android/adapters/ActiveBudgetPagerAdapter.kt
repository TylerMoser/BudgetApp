package me.tylermoser.budget.android.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * An adapter used by the [android.support.v4.view.ViewPager] to display the tabbed fragments for each budget sheet
 *
 * @author Tyler Moser
 */
class ActiveBudgetPagerAdapter(private val fragments: Array<Fragment>, fragmentManager: FragmentManager)
    : FragmentPagerAdapter(fragmentManager) {

    /**
     * Gets the fragment in the tab at index [index]
     */
    override fun getItem(index: Int): Fragment {
        return fragments[index]
    }

    /**
     * Gets the number of tabs in the [android.support.v4.view.ViewPager]
     */
    override fun getCount() = fragments.size

}
