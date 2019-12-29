package me.tylermoser.budget.android.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.DialogFragment
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.fragment_archived_budget.*
import kotlinx.android.synthetic.main.fragment_view_expenses.*
import me.tylermoser.budget.R
import me.tylermoser.budget.android.fragments.*
import me.tylermoser.budget.android.models.SheetEntry
import me.tylermoser.budget.android.services.*
import androidx.appcompat.widget.Toolbar

/**
 * The main Activity for the budget app. This app has a single main activity with a navigation
 * drawer and app bar. The navigation drawer is used to load individual fragments into this
 * activity. Each of these fragments may contain other fragments and modify the app bar.
 *
 * This Activity contains instances of services, utilities, and helper classes used by the other
 * fragments in this app. The app only allows the user to modify a single spreadsheet at a time,
 * with the individual sheets of that spreadsheet listed in the navigation drawer. The ID of that
 * spreadsheet is stored in this class.
 *
 * @author Tyler Moser
 */
class BudgetActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        NavDrawerFragment.OnFragmentInteractionListener,
        AddNewExpenseFragment.OnFragmentInteractionListener,
        ViewExpensesFragment.OnFragmentInteractionListener,
        ActiveBudgetSettingsFragment.OnFragmentInteractionListener,
        ArchivedBudgetSettingsFragment.OnFragmentInteractionListener
{
    // Codes used to resume desired execution after authentication completes
    val noAction by lazy {resources.getInteger(R.integer.authentication_request_code_no_action)}
    val getSheet by lazy {resources.getInteger(R.integer.authentication_request_code_get_sheet)}
    val appendExpense by lazy {resources.getInteger(R.integer.authentication_request_code_append_expense)}
    val getNavDrawerData by lazy {resources.getInteger(R.integer.authentication_request_code_get_navigation_drawer_data)}
    val updateNavDrawerData by lazy {resources.getInteger(R.integer.authentication_request_code_update_navigation_drawer_data)}
    val archiveThisSheet by lazy {resources.getInteger(R.integer.authentication_request_code_archive_this_sheet)}
    val refresh by lazy {resources.getInteger(R.integer.authentication_request_code_refresh)}
    val unarchiveThisSheet by lazy {resources.getInteger(R.integer.authentication_request_code_unarchive_this_sheet)}
    val changeSpreadsheet by lazy {resources.getInteger(R.integer.authentication_request_code_change_spreadsheet)}
    val addNewBudget by lazy {resources.getInteger(R.integer.authentication_request_code_add_new_budget)}
    val doesSheetExist by lazy {resources.getInteger(R.integer.authentication_request_code_does_sheet_exist)}

    // Used for retaining state on screen rotation
    private val savedFragmentTypeKey by lazy {resources.getString(R.string.bundle_parameter_key_saved_fragment_type)}
    private val savedSheetNameKey by lazy {resources.getString(R.string.bundle_parameter_key_saved_sheet_name)}
    private val savedSheetIDKey by lazy {resources.getString(R.string.bundle_parameter_key_saved_sheet_ID)}

    val authenticator: Authenticator = Authenticator(this)
    val cache by lazy {AndroidSharedPreferencesJsonCache(applicationContext)}
    val api: GoogleSheetsApi by lazy {GoogleSheetsApiV4(authenticator, cache)}
    val navDrawerHelper by lazy {NavigationDrawerHelper(this, cache)}
    val appBar by lazy {AppBarHelper(this)}

    /*
     References to the fragment currently in the navigation drawer view, cast to different types
     for easy use. When using these cast fragment, the developer must be sure that the currently
     active fragment is in fact the type that the member variable being accessed represents. These
     are unsafe casts.
     */
    private val thisActivity: BudgetActivity = this
    lateinit var currentFragment: NavDrawerFragment
    val budgetFragment get() = currentFragment as BudgetFragment
    private val viewExpensesFragment get() = budgetFragment.viewExpensesFragment
    private val activeBudgetFragment get() = currentFragment as ActiveBudgetFragment
    private val addNewExpenseFragment get() = activeBudgetFragment.addNewExpenseFragment
    private val activeBudgetSettingsFragment get() = activeBudgetFragment.activeBudgetSettingsFragment
    private val archivedBudgetFragment get() = currentFragment as ArchivedBudgetFragment
    private val archivedBudgetSettingsFragment get() = archivedBudgetFragment.archivedBudgetSettingsFragment

    private val changeSpreadsheetDialogTag = "change spreadsheet"
    private val addNewBudgetDialogTag = "add new budget"

    // The spreadsheet currently being modified by this app
    lateinit var spreadsheetID: String

    /**
     * Gets the currently active spreadsheet ID from the cache, loads the correct sheet into the UI,
     * and populates the navigation drawer from the cache when the [Activity] is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        spreadsheetID = cache.getSpreadsheetID()

        if (cache.isOpenOnLoadValid() && savedInstanceState == null && cache.isSpreadsheetIDValid()) {
            // Load from cache when app opens
            val (openOnLoadSheetName, openOnLoadSheetID) = cache.getOpenOnLoad()
            navDrawerHelper.changeMainFragment(ActiveBudgetFragment
                    .newInstance(this, openOnLoadSheetID, openOnLoadSheetName))
        } else if (savedInstanceState != null && cache.isSpreadsheetIDValid()) {
            // load from saved state after screen rotation
            loadFragmentFromSavedInstanceState(savedInstanceState)
        } else if (cache.isSpreadsheetIDValid()) {
            // If the app is being opened and the open on load cache is invalid
            loadUIWhenOpenOnLoadCacheIsInvalid()
        } else {
            // If there is no cached spreadsheetID
            loadUIWhenSpreadsheetIDIsInvalid()
        }

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        if (cache.isSpreadsheetIDValid()) {
            populateNavigationDrawerFromCache()
        }
    }

    /**
     * When the [Activity] is resumed, query the server and populate the navigation drawer based on
     * the data.
     */
    override fun onResume() {
        super.onResume()

        if (cache.isSpreadsheetIDValid()) {
            populateNavigationDrawerFromServer()
        } else {
            showChangeSpreadsheetDialog()
        }
    }

    /**
     * Used to restore state on screen rotation
     */
    override fun onSaveInstanceState(outState: Bundle) {
        if (isCurrentFragmentInitialized()) {
            when (currentFragment) {
                is AppSettingsFragment -> {
                    outState.putString(savedFragmentTypeKey, AppSettingsFragment::class.java.name)
                }
                is ActiveBudgetFragment -> {
                    outState.putString(savedFragmentTypeKey, ActiveBudgetFragment::class.java.name)
                    outState.putString(savedSheetIDKey, activeBudgetFragment.sheetID)
                    outState.putString(savedSheetNameKey, activeBudgetFragment.sheetName)
                }
                is ArchivedBudgetFragment -> {
                    outState.putString(savedFragmentTypeKey, ArchivedBudgetFragment::class.java.name)
                    outState.putString(savedSheetIDKey, archivedBudgetFragment.sheetID)
                    outState.putString(savedSheetNameKey, archivedBudgetFragment.sheetName)
                }
            }
        }

        super.onSaveInstanceState(outState)
    }

    /**
     * This app does not have a separate authentication screen. When an action is needed that
     * requires access, the user is prompted with a dialog activity provided by Google's API's. This
     * method is called after the user is authenticated. I use this to resume the action requested
     * by the user after they have finished authenticating.
     *
     * @param requestCode Used to identify which call prompted the authentication and then continue
     * the desired logic to be completed after authentication
     * @param resultCode Should return [Activity.RESULT_OK] when the user finishes authentication
     * @param data Unused at this point
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            authenticator.setUserToLastSignedIn()
            when (requestCode) {
                noAction -> {}
                getSheet -> budgetFragment.refresh()
                doesSheetExist -> budgetFragment.loadSheetFromServerAuthenticated()
                appendExpense -> addNewExpenseFragment.submitNewExpense()
                getNavDrawerData -> getNavigationDrawerDataFromServer()
                refresh -> {
                    populateNavigationDrawerFromServer()
                    budgetFragment.refresh()
                }
                archiveThisSheet ->
                    Snackbar.make(activeBudgetConstraintLayout,
                            "Unable to archive this sheet",
                            Snackbar.LENGTH_SHORT).show()
                unarchiveThisSheet ->
                    Snackbar.make(archivedBudgetConstraintLayout,
                            "Unable to unarchive this sheet",
                            Snackbar.LENGTH_SHORT).show()
                changeSpreadsheet -> showChangeSpreadsheetDialog()
                addNewBudget -> showAddNewBudgetDialog()
                else -> throw RuntimeException("invalid code in onActivityResult")
            }
        } else {
            /*
                On first authentication, this returns "result cancelled" this is incorrect,
                but everything seems to work.  Ignore this message for the time being when the
                app is first installed.
             */
            Snackbar.make(activeBudgetConstraintLayout,
                    "Unable to authenticate user",
                    Snackbar.LENGTH_SHORT).show()
        }
    }

    /*
     * Individual sheets are assigned listeners to load them into the navigation drawer view when the
     * navigation drawer is populated. The non-sheet actions available in the navigation drawer are
     * assigned on-click listeners here.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawer_new_budget -> showAddNewBudgetDialog()
            R.id.drawer_settings ->
                navDrawerHelper.changeMainFragment(AppSettingsFragment.newInstance())
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Closes the navigation drawer when the user presses the back button
     */
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * An implementation of an interface required by each child fragment for
     * [android.support.v4.app.Fragment] and [Activity] communication
     */
    override fun onFragmentInteraction(uri: Uri) {}

    /**
     * Used to determine whether the current fragment loaded into the navigation drawer view has
     * been initialized from outside of this activity.
     */
    fun isCurrentFragmentInitialized() = ::currentFragment.isInitialized

    /**
     * Called by [AddNewExpenseFragment] when a user aims to add a new expense to the currently
     * active budget sheet.
     *
     * @param v The button as a [android.view.View] that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onSubmitClick(v: android.view.View) {
        try {
            if (authenticator.authenticate(appendExpense))
                addNewExpenseFragment.submitNewExpense()
        } catch (numberFormatException: NumberFormatException) {
            Snackbar.make(v,"Non-Numeric Expense Amount Entered", Snackbar.LENGTH_SHORT)
                    .show()
            addNewExpenseFragment.resetSubmitAfterFailure()
        } catch (exception: Exception) {
            Snackbar.make(v, "Unexpected Error Submitting a new Expense", Snackbar.LENGTH_SHORT)
                    .show()
            addNewExpenseFragment.resetSubmitAfterFailure()
        }
    }

    /**
     * Called by [ActiveBudgetSettingsFragment] when a user wants the budget sheet they are
     * currently viewing to open every time that the user opens the app.
     *
     * @param v The button as a [android.view.View] that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onOpenOnLoadClick(v: android.view.View) {
        cache.putOpenOnLoad(activeBudgetFragment.sheetID, activeBudgetFragment.sheetName)

        api.getSpreadsheetMetadata(spreadsheetID) {
            populateNavigationDrawer(it)
        }

        Snackbar.make(v,
                "This sheet will now open every time the app launches",
                Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Called by [ActiveBudgetSettingsFragment] when the user wants to archive the budget that they
     * are currently viewing.
     *
     * @param v The button as a [android.view.View] that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onArchiveClick(v: android.view.View) {
        if (authenticator.authenticate(archiveThisSheet)) {
            activeBudgetFragment.archiveSheet()
        }
    }

    /**
     * Called by [ArchivedBudgetSettingsFragment] when the user wants to unarchive the budget that
     * they are currently viewing.
     *
     * @param v The button as a [android.view.View] that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onUnarchiveClick(v: android.view.View) {
        if (authenticator.authenticate(unarchiveThisSheet)) {
            archivedBudgetFragment.unarchiveSheet()
        }
    }

    /**
     * This opens the sheet currently being viewed in the Google Sheets app.
     *
     * @param v The button as a [android.view.View] that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onOpenBudgetInGoogleSheetsClick(v: android.view.View) {
        val linkText = "https://docs.google.com/spreadsheets/d/$spreadsheetID/edit#gid=${budgetFragment.sheetID}"
        val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(linkText))
        startActivity(linkIntent)
    }

    /**
     * This method is called when the user clicks the refresh button located on the app bar.
     *
     * @param v The button as a [android.view.View] that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onRefreshButtonClick(v: android.view.View) {
        if (authenticator.authenticate(refresh)) {
            /*
            In the future, refresh should be reworked.
            In some cases, refresh as used in the code should just reload the sheet from the server,
            while the true refresh logic should also reload the navigation drawer. A manager to
            manage the speed and number of requests should be implemented at this point. In
            addition, for this true refresh logic, the requests should be sent and processed
            concurrently. The refresh UI indicator icons/wheels should not stop spinning until all
            responses have been received and processed.
             */
            populateNavigationDrawerFromServer()
            budgetFragment.refresh()
        }
    }

    /**
     * Called by [AppSettingsFragment] when the user wants to switch to a different spreadsheet to
     * use.
     *
     * @param v The button as a [android.view.View] that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onChangeSpreadsheetClick(v: android.view.View) {
        showChangeSpreadsheetDialog()
    }

    /**
     * Signs out the user. Follows the Google official guidelines by deleting all locally stored
     * data before logging the user out.
     *
     * @param v The button as a [android.view.View] that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onSignOutClick(v: android.view.View) {
        cache.clearCache()
        authenticator.signOut()
        finishAndRemoveTask() // closes the app programmatically
    }

    /**
     * A helper method used to load the main fragment after rotating the screen
     *
     * @param savedInstanceState The [Bundle] containing the data that was saved when the screen
     * rotated
     */
    private fun loadFragmentFromSavedInstanceState(savedInstanceState: Bundle) {
        val savedFragmentType = savedInstanceState.getString(savedFragmentTypeKey)
        val savedSheetName = savedInstanceState.getString(savedSheetNameKey, "")
        val savedSheetID = savedInstanceState.getString(savedSheetIDKey, "")

        if (savedFragmentType == AppSettingsFragment::class.java.name) {
            navDrawerHelper.changeMainFragment(AppSettingsFragment.newInstance())
        } else if (savedFragmentType == ActiveBudgetFragment::class.java.name){
            navDrawerHelper.changeMainFragment(
                    ActiveBudgetFragment.newInstance(this, savedSheetID, savedSheetName))
        } else if (savedFragmentType == ArchivedBudgetFragment::class.java.name) {
            navDrawerHelper.changeMainFragment(
                    ArchivedBudgetFragment.newInstance(this, savedSheetID, savedSheetName))
        } else {
            throw RuntimeException("savedInstanceState fragment type is impossible: $savedFragmentType")
        }
    }

    /**
     * Sets up the activity UI elements when there are no active sheets to display when the app is
     * opened.
     */
    fun loadUIWhenOpenOnLoadCacheIsInvalid() {
        loadUIWhenInvalidData()
        leftoverTextView.textSize = 20f
        leftoverTextView.setText("No Active Sheets")
    }

    /**
     * Sets up the activity UI when there is no spreadsheet set to be opened by the app.
     */
    private fun loadUIWhenSpreadsheetIDIsInvalid() {
        loadUIWhenInvalidData()
        leftoverTextView.textSize = 16f;
        leftoverTextView.setText("No Spreadsheet Selected")
    }

    /**
     * A helper method intended to extract common code from [loadUIWhenOpenOnLoadCacheIsInvalid]
     * and [loadUIWhenSpreadsheetIDIsInvalid]
     */
    private fun loadUIWhenInvalidData() {
        slash.setText("")
        toSpendTextView.setText("")
        refreshButton.isEnabled = false
    }

    /**
     * Populates the navigation drawer from the local cache.
     */
    private fun populateNavigationDrawerFromCache() {
        val cachedEntries = cache.getNavigationDrawerSheetEntriesList(spreadsheetID)
        if (cachedEntries != null) {
            populateNavigationDrawer(cachedEntries)
        }
    }

    /**
     * Executes the queries and associated logic to populate the navigation drawer based on data
     * from Google's servers.
     */
    fun populateNavigationDrawerFromServer() {
        if (authenticator.authenticate(getNavDrawerData)) {
            getNavigationDrawerDataFromServer()
        }
    }

    /**
     * A helper method used by [populateNavigationDrawerFromServer] to execute the required logic
     * after the user has been authenticated.
     */
    private fun getNavigationDrawerDataFromServer() {
        api.getSpreadsheetMetadata(spreadsheetID) {
            if (!it.isEmpty()) {
                populateNavigationDrawer(it)
            }

            if (authenticator.authenticate(noAction)) {
                updateNavigationDrawerData(it)
            }
        }
    }

    /**
     * Updates the navigation drawer to include sheets added in the Google Sheets webapp after the
     * user has loaded the navigation drawer. This updates the spreadsheet metadata to include all
     * sheets in the spreadsheet.
     *
     * @param existingMetadata The metadata as it existed before this call
     */
    private fun updateNavigationDrawerData(existingMetadata: List<SheetEntry>) {
        api.getSpreadsheetProperties(spreadsheetID) {
            val newMetadata = navDrawerHelper.mergeSheetEntryLists(existingMetadata, it)
            if (existingMetadata != newMetadata) {
                navDrawerHelper.openSheetIfNoneSpecified(it)
                api.updateSpreadsheetMetadata(spreadsheetID, newMetadata)
                cache.putNavigationDrawerSheetEntriesList(spreadsheetID, newMetadata)
                populateNavigationDrawer(newMetadata)
            }
        }
    }

    /**
     * Loads the navigation drawer UI with the queried data.
     *
     * @param sheetEntries The list of [SheetEntry] metadata
     */
    fun populateNavigationDrawer(sheetEntries: MutableList<SheetEntry>) {
        val navMenu = nav_view.menu
        navMenu.clear()
        nav_view.inflateMenu(R.menu.activity_home_drawer)
        val activeSubMenu = navMenu.addSubMenu("Active Budgets")
        val archivedSubMenu = navMenu.addSubMenu("Archived Budgets")

        navDrawerHelper.openSheetIfNoneSpecified(sheetEntries)
        navDrawerHelper.putOpenOnLoadSheetAtTopOfActiveSheetsSubMenu(activeSubMenu, sheetEntries)
        navDrawerHelper.populateNavigationDrawerSubmenus(activeSubMenu, archivedSubMenu, sheetEntries)
    }

    /**
     * Stops all UI cues of refreshing.
     * To be called when a new fragment is being loaded into the Navigation Drawer layout.
     */
    fun stopRefreshing() {
        swipeRefreshLayout?.isRefreshing = false
        refreshButton?.stopRotation()
    }

    /**
     * Displays the change spreadsheet dialog, allowing the user to switch which Google
     * spreadsheet is being edited by the app.
     */
    private fun showChangeSpreadsheetDialog() {
        if (!changeSpreadsheetDialogAlreadyShowing()) {
            if (authenticator.authenticate(changeSpreadsheet)) {
                ChangeSpreadsheetDialogFragment.newInstance()
                        .show(supportFragmentManager, changeSpreadsheetDialogTag)
            }
        }
    }

    /**
     * Returns true if the change spreadsheet dialog is already being displayed to the user.
     * Otherwise, returns false.
     */
    private fun changeSpreadsheetDialogAlreadyShowing(): Boolean {
        val changeSpreadsheetDialogFragment = supportFragmentManager.findFragmentByTag(changeSpreadsheetDialogTag) as? DialogFragment
        return (changeSpreadsheetDialogFragment?.dialog?.isShowing ?: false
                && !changeSpreadsheetDialogFragment!!.isRemoving)
    }

    /**
     * Executed when the user changes the spreadsheet that is active in the app.
     *
     * @param newSpreadsheetID The spreadsheet ID of the new spreadsheet to be used by the app
     */
    fun changeSpreadsheet(newSpreadsheetID: String) {
        cache.clearCache()
        cache.putSpreadsheetID(newSpreadsheetID)
        spreadsheetID = newSpreadsheetID
        populateNavigationDrawerFromServer()
    }

    /**
     * Displays the "Add new budget" [android.app.Dialog], allowing the user to add a new sheet to
     * the Google spreadsheet currently being used by the app.
     */
    private fun showAddNewBudgetDialog() {
        if (authenticator.authenticate(addNewBudget)) {
            AddNewBudgetDialogFragment.newInstance()
                    .show(supportFragmentManager, addNewBudgetDialogTag)
        }
    }

}
