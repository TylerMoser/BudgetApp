package me.tylermoser.budget.android.fragments

import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.content_home.*
import me.tylermoser.budget.R
import me.tylermoser.budget.android.adapters.ActiveBudgetPagerAdapter
import me.tylermoser.budget.android.models.NameIDPair
import me.tylermoser.budget.android.models.SheetEntry
import com.google.android.material.tabs.TabLayout

/**
 * A [BudgetFragment] that displays a Google Sheet that is currently not archived.
 *
 * Unarchived sheets allow the user to add new expenses, and have a larger app bar meant to more
 * prominently display the remaining funds in the budget.
 *
 * @author Tyler Moser
 */
class ActiveBudgetFragment : BudgetFragment() {

    lateinit var addNewExpenseFragment: AddNewExpenseFragment
    lateinit var activeBudgetSettingsFragment: ActiveBudgetSettingsFragment
    override lateinit var viewExpensesFragment: ViewExpensesFragment

    /**
     * Archives this sheet by sending a request to update this sheet's metadata
     */
    fun archiveSheet() {
        budgetActivity.api.getSpreadsheetMetadata(budgetActivity.spreadsheetID) {
            if (budgetActivity.cache.getOpenOnLoad() == NameIDPair(sheetName, sheetID))
                budgetActivity.cache.putOpenOnLoad("", "")

            archiveThisSheetInSheetEntriesList(it)
            budgetActivity.api.updateSpreadsheetMetadata(budgetActivity.spreadsheetID, it)
            budgetActivity.populateNavigationDrawer(it)

            val newFragment = ArchivedBudgetFragment.newInstance(budgetActivity, sheetID, sheetName)
            budgetActivity.navDrawerHelper.changeMainFragment(newFragment)
        }
    }

    /**
     * Changes this sheet's archival status in a list of [SheetEntry]s
     */
    private fun archiveThisSheetInSheetEntriesList(sheetEntries: MutableList<SheetEntry>) {
        changeThisSheetArchiveStatus(sheetEntries, newStatus = true)
    }

    /**
     * Prepares the [ 	android.support.design.widget.TabLayout] used on the UI to display the three
     * [android.support.v4.app.Fragment]s for adding new expenses to the budget, viewing existing
     * expenses, and adjusting the budget's settings.
     *
     * @see AddNewExpenseFragment
     * @see ViewExpensesFragment
     * @see ActiveBudgetSettingsFragment
     */
    override fun setupTabbedLayout() {
        addNewExpenseFragment = AddNewExpenseFragment.newInstance()
        viewExpensesFragment = ViewExpensesFragment.newInstance()
        activeBudgetSettingsFragment = ActiveBudgetSettingsFragment.newInstance()
        viewPager.adapter = ActiveBudgetPagerAdapter(
                arrayOf(addNewExpenseFragment, viewExpensesFragment, activeBudgetSettingsFragment),
                childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_add_white_24dp)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_view_list_white_24dp)
        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_settings_white_24dp)
    }

    /**
     * Makes changes to the App Bar for the desired active budget UI. Most notably, this increases
     * the height of the app bar.
     */
    override fun setAppBar() {
        budgetActivity.appBar.editForActiveBudget(this)
    }

    /**
     * Creates a new instance of this [android.support.v4.app.Fragment] and sets the required
     * parameters.
     */
    companion object {
        @JvmStatic
        fun newInstance(context: Context, sheetID: String, sheetName: String) =
                ActiveBudgetFragment().apply {
                    arguments = Bundle().apply {
                        putString(context.resources.getString(R.string.bundle_parameter_key_sheet_ID), sheetID)
                        putString(context.resources.getString(R.string.bundle_parameter_key_sheet_name), sheetName)
                    }
                }
    }

}
