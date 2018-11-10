package me.tylermoser.budget.android.fragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.fragment_archived_budget.*
import kotlinx.android.synthetic.main.fragment_view_expenses.*
import me.tylermoser.budget.R
import me.tylermoser.budget.android.models.BudgetSheet
import me.tylermoser.budget.android.models.SheetEntry

/**
 * The parent class of all [NavDrawerFragment]s that display a single budget/Google Sheet
 *
 * @author Tyler Moser
 */
abstract class BudgetFragment: NavDrawerFragment() {

    lateinit var sheetID: String
    lateinit var sheetName: String

    abstract var viewExpensesFragment: ViewExpensesFragment

    /**
     * Sets the [BudgetFragment] parameters when the [android.support.v4.app.Fragment] is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sheetID = it.getString(context?.resources?.getString(R.string.bundle_parameter_key_sheet_ID))
                    ?: throw RuntimeException("Bundle parameter sheet ID not found")
            sheetName = it.getString(context?.resources?.getString(R.string.bundle_parameter_key_sheet_name))
                    ?: throw RuntimeException("Bundle parameter sheet name not found")
        }
    }

    /**
     * Populate the UI when this [android.support.v4.app.Fragment] is created
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val toReturn =  super.onCreateView(inflater, container, savedInstanceState)
        return toReturn
    }

    /**
     * Sets up the [android.support.design.widget.TabLayout] and loads the sheet information from
     * the app cache once the [android.app.Activity] containing this
     * [android.support.v4.app.Fragment] is created
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupTabbedLayout()
        loadSheetFromCache()
    }

    /**
     * When this [android.support.v4.app.Fragment] resumes, query Google's servers and update the UI
     * with the gathered data
     */
    override fun onResume() {
        super.onResume()
        loadSheetFromServer()
    }

    /**
     * Populates this [android.support.v4.app.Fragment]'s UI with the data stored in the cache
     */
    private fun loadSheetFromCache() {
        val cachedSheet = budgetActivity.cache.getBudgetSheet(sheetID)
        if (cachedSheet != null) {
            populateSheetUI(cachedSheet)
        }
    }

    /**
     * Populates the UI with the data in a [BudgetSheet]
     *
     * @param sheet The [BudgetSheet] from which to get the data to populate the UI with
     */
    private fun populateSheetUI(sheet: BudgetSheet) {
        if (budgetActivity.currentFragment == this) {
            budgetActivity.slash.setText("/")
            budgetActivity.leftoverTextView.text = sheet.leftover.toString()
            budgetActivity.toSpendTextView.text = sheet.toSpend.toString()

            /*
            Set cachedSheet so that viewExpensesFragment loads the cached sheet into the recyclerView
            if it has not yet been inflated. In the case that it has been inflated, call
            setupRecyclerView so that the recycler view is updated.
             */
            viewExpensesFragment.cachedSheet = sheet
            viewExpensesFragment.setupRecyclerView(sheet)
        }
    }

    /**
     * Sets up the [android.support.design.widget.TabLayout] for this [BudgetFragment] to contain
     * the required [android.support.v4.app.Fragment]s
     */
    abstract fun setupTabbedLayout()

    /**
     * Populates the UI with data obtained from Google's servers
     */
    private fun loadSheetFromServer() {
        if (budgetActivity.authenticator.authenticate(budgetActivity.doesSheetExist)) {
            loadSheetFromServerAuthenticated()
        }
    }

    fun loadSheetFromServerAuthenticated() {
        budgetActivity.api.doesSheetExist(budgetActivity.spreadsheetID, sheetID) {
            if (it != null) {
                sheetName = it // If the sheet was renamed, take the new name
                if (budgetActivity.authenticator.authenticate(budgetActivity.getSheet)) {
                    refresh()
                }
            } else {
                Snackbar.make(budgetActivity.viewPager, "This sheet has been deleted",
                        Snackbar.LENGTH_SHORT).show()
                budgetActivity.stopRefreshing()
                val openOnLoadNameIDPair = budgetActivity.cache.getOpenOnLoad()
                if (openOnLoadNameIDPair.sheetName == sheetName && openOnLoadNameIDPair.sheetId == sheetID) {
                    budgetActivity.cache.clearOpenOnLoad()
                }
                tryToLoadAnotherBudget()
                budgetActivity.populateNavigationDrawerFromServer()
            }
        }
    }

    /**
     * Refreshes the data displayed on the current [BudgetFragment]
     *
     * @param activatedWithGesture true if the user is refreshing the UI by pulling down on the
     * [android.support.v7.widget.RecyclerView]
     */
    fun refresh(activatedWithGesture: Boolean = false) {
        budgetActivity.refreshButton.startRotation()
        if (!activatedWithGesture) swipeRefreshLayout?.isRefreshing = true

        budgetActivity.api.getSheet(budgetActivity.spreadsheetID, sheetName, sheetID) { sheet: BudgetSheet ->
            populateSheetUI(sheet)
            swipeRefreshLayout?.isRefreshing = false
            budgetActivity.stopRefreshing()
        }
    }

    /**
     * A helper method to update the [SheetEntry] represented by this [BudgetFragment] to be either
     * archived or unarchived when it is contained in a list.
     *
     * @param sheetEntries The list containing a [SheetEntry] represented by this [BudgetFragment]
     * @param newStatus The new archival status for the [SheetEntry] represented by this
     *                  [BudgetFragment]. True for archived, false for unarchived.
     */
    protected fun changeThisSheetArchiveStatus(sheetEntries: MutableList<SheetEntry>, newStatus: Boolean) {
        for (entry in sheetEntries) if (entry.sheetId == sheetID) entry.archived = newStatus
    }

    /**
     * Gets the spreadsheet metadata and looks for another active budget to open. If one is found,
     * it is displayed. Otherwise, the Activity reflects that there are no active budgets to open.
     *
     * Called when this sheet is set to open on load, but has been deleted.
     */
    private fun tryToLoadAnotherBudget() {
        budgetActivity.api.getSpreadsheetMetadata(budgetActivity.spreadsheetID) {
            if (!budgetActivity.navDrawerHelper.openSheetFromMetadata(it, sheetID)) {
                budgetActivity.loadUIWhenOpenOnLoadCacheIsInvalid()
            }
            budgetActivity.supportFragmentManager.beginTransaction().remove(this).commit()
        }
    }

}
