package me.tylermoser.budget.android.services

import me.tylermoser.budget.android.models.BudgetSheet
import me.tylermoser.budget.android.models.NameIDPair
import me.tylermoser.budget.android.models.SheetEntry

/**
 * An interface to be used for the app cache
 * UI and returned Google server data is cache to improve app UI fluidity and user experience
 *
 * @author Tyler Moser
 */
interface Cache {

    /**
     * Clears all contents of this cache
     */
    fun clearCache()

    // =============================================================================================

    /**
     * Put a [BudgetSheet] into the cache
     *
     * @param sheetID The ID of the sheet to put in the cache
     * @param budgetSheet The [BudgetSheet] to put in the cache
     */
    fun putBudgetSheet(sheetID: String, budgetSheet: BudgetSheet)

    /**
     * Retrieves the [BudgetSheet] with sheetID from the cache
     */
    fun getBudgetSheet(sheetID: String): BudgetSheet?

    // =============================================================================================

    /**
     * Puts the list of [SheetEntry]s obtained from metadata into the cache. This serves as a
     * metadata cache.
     *
     * @param spreadsheetID The ID of the spreadsheet for which the list of [SheetEntry]s has been
     * created
     * @param sheetEntries The [SheetEntry]s to cache
     */
    fun putNavigationDrawerSheetEntriesList(spreadsheetID: String, sheetEntries: List<SheetEntry>)

    /**
     * Gets the cached metadata sheet entries list from the cache for the spreadsheet with the given
     * ID
     */
    fun getNavigationDrawerSheetEntriesList(spreadsheetID: String): MutableList<SheetEntry>?

    // =============================================================================================

    /**
     * Puts the spreadsheet ID of the spreadsheet that the app is using in the cache.
     */
    fun putSpreadsheetID(spreadsheetID: String)

    /**
     * Gets the spreadsheet ID of the spreadsheet that the app is currently using from the cache.
     * This is used to get the ID of the spreadsheet to load when the app is opened.
     */
    fun getSpreadsheetID(): String

    /**
     * Returns whether or not a valid spreadsheet ID has been saved to the cache.
     */
    fun isSpreadsheetIDValid(): Boolean

    // =============================================================================================

    /**
     * Resets the open on load cache so that it is considered invalid
     */
    fun clearOpenOnLoad()

    /**
     * Caches the data for the Google sheet that's data should be loaded into the UI when the app
     * is opened
     */
    fun putOpenOnLoad(sheetID: String, sheetName: String)

    /**
     * Gets the name and ID of the Google sheet that should be loaded when the app is opened
     */
    fun getOpenOnLoad(): NameIDPair

    /**
     * Returns whether or not a valid Google sheet ID and sheet name have been cached
     */
    fun isOpenOnLoadValid(): Boolean

}
