package me.tylermoser.budget.android.fragments

import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.content_home.*
import me.tylermoser.budget.R
import me.tylermoser.budget.android.adapters.ActiveBudgetPagerAdapter
import me.tylermoser.budget.android.models.SheetEntry

/**
 * A [BudgetFragment] that displays a Google Sheet that is currently archived.
 *
 * Archived sheets do not allow the user to add any new expenses, and have a shorter App Bar so that
 * the user can view more of the expenses on the budget at once.
 *
 * @author Tyler Moser
 */
class ArchivedBudgetFragment : BudgetFragment() {

    lateinit var archivedBudgetSettingsFragment: ArchivedBudgetSettingsFragment
    override lateinit var viewExpensesFragment: ViewExpensesFragment

    /**
     * Unarchives this sheet by sending a request to update this sheet's metadata
     */
    fun unarchiveSheet() {
        budgetActivity.api.getSpreadsheetMetadata(budgetActivity.spreadsheetID) {
            unarchiveThisSheetInSheetEntriesList(it)
            budgetActivity.api.updateSpreadsheetMetadata(budgetActivity.spreadsheetID, it)
            budgetActivity.populateNavigationDrawer(it)

            val newFragment = ActiveBudgetFragment.newInstance(budgetActivity, sheetID, sheetName)
            budgetActivity.navDrawerHelper.changeMainFragment(newFragment)
        }
    }

    /**
     * Changes this sheet's archival status in a list of [SheetEntry]s
     */
    private fun unarchiveThisSheetInSheetEntriesList(sheetEntries: MutableList<SheetEntry>) {
        changeThisSheetArchiveStatus(sheetEntries, newStatus = false)
    }

    /**
     * Prepares the [android.support.design.widget.TabLayout] used on the UI to display the three
     * [android.support.v4.app.Fragment]s for viewing existing expenses and adjusting the budget's
     * settings.
     *
     * @see ViewExpensesFragment
     * @see ArchivedBudgetSettingsFragment
     */
    override fun setupTabbedLayout() {
        viewExpensesFragment = ViewExpensesFragment.newInstance()
        archivedBudgetSettingsFragment = ArchivedBudgetSettingsFragment.newInstance()
        viewPager.adapter = ActiveBudgetPagerAdapter(
                arrayOf(viewExpensesFragment, archivedBudgetSettingsFragment),
                childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_view_list_white_24dp)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_settings_white_24dp)
    }

    /**
     * Makes changes to the App Bar for the desired archived budget UI. Most notably, this decreases
     * the height of the app bar.
     */
    override fun setAppBar() {
        budgetActivity.appBar.editForArchivedBudget(this)
    }

    companion object {
        /**
         * Creates a new instance of this [android.support.v4.app.Fragment] and sets the required \
         * parameters.
         *
         * @param sheetID The ID of the sheet to open in this [ArchivedBudgetFragment]
         * @param sheetName The name of the sheet to open in this [ArchivedBudgetFragment]
         */
        @JvmStatic
        fun newInstance(context: Context, sheetID: String, sheetName: String) =
                ArchivedBudgetFragment().apply {
                    arguments = Bundle().apply {
                        putString(context.resources.getString(R.string.bundle_parameter_key_sheet_ID), sheetID)
                        putString(context.resources.getString(R.string.bundle_parameter_key_sheet_name), sheetName)
                    }
                }
    }

}
