package me.tylermoser.budget.android.services

import me.tylermoser.budget.android.models.BudgetSheet
import me.tylermoser.budget.android.models.Expense
import me.tylermoser.budget.android.models.SheetEntry

/**
 * An interface for implementing the Google Sheets API
 *
 * @author Tyler Moser
 */
interface GoogleSheetsApi {

    /**
     * Gets a Google Sheet from Google's servers and then executes a lambda
     *
     * @param spreadsheetID The ID of the Google Spreadsheet containing the requested sheet
     * @param sheetName The name of the sheet being requested
     * @param sheetID The ID of the sheet being requested
     * @param doAfterRequest A callback lambda to be executed after the sheet has been obtained
     */
    fun getSheet(spreadsheetID: String, sheetName: String, sheetID: String, doAfterRequest: (BudgetSheet) -> Unit)

    /**
     * Determines whether a sheet with the given name exists in the spreadsheet with the provided
     * ID. Executes doAfterRequest with the result of this check.
     */
    fun doesSheetExist(spreadsheetID: String, sheetID: String, doAfterRequest: (String?) -> Unit)

    /**
     * Adds a new Google Sheet to a Google Spreadsheet and executes a lambda
     *
     * @param spreadsheetID The ID of the Google Spreadsheet to add the new sheet to
     * @param sheetName The name of the new sheet to add
     * @param doAfterRequest A callback lambda to be executed after the sheet has been created
     */
    fun addSheet(spreadsheetID: String, sheetName: String, budgetSheet: BudgetSheet, doAfterRequest: (String) -> Unit)

    // =============================================================================================

    /**
     * Adds a new expense to a Google Sheet
     *
     * @param spreadsheetID The ID of the spreadsheet containing the sheet to append this expense to
     * @param sheetName The name of the sheet to append this expense to
     * @param expense The expense to append to the Google Sheet
     * @param doAfterRequest A callback lambda to be executed after this expense has been appended
     */
    fun appendNewExpense(spreadsheetID: String, sheetName: String, expense: Expense, doAfterRequest: () -> Unit)

    // =============================================================================================

    /**
     * Gets the metadata associated with a spreadsheet
     *
     * @param spreadsheetID The ID of the spreadsheet that the metadata is associated with
     * @param doAfterRequest A callback lambda to be executed after the metadata bas been obtained
     */
    fun getSpreadsheetMetadata(spreadsheetID: String, doAfterRequest: (MutableList<SheetEntry>) -> Unit)

    /**
     * Updates the metadata associated with a spreadsheet
     *
     * @param spreadsheetID The ID of the spreadsheet to update the metadata of
     * @param sheetEntries The new metadata to be associated with the spreadsheet
     */
    fun updateSpreadsheetMetadata(spreadsheetID: String, sheetEntries: List<SheetEntry>)

    /**
     * Gets the properties of a Google Spreadsheet. Used in the app to get all of the sheets on a
     * spreadsheet.
     *
     * @param spreadsheetID The spreadsheet to get the properties of
     * @param doAfterRequest A callback lambda to be executed once the properties have been obtained
     */
    fun getSpreadsheetProperties(spreadsheetID: String, doAfterRequest: (List<SheetEntry>) -> Unit)

    // =============================================================================================

    /**
     * Checks whether a spreadsheet ID is valid and the current user has permissions to interact
     * with it.
     *
     * @param spreadsheetID The ID to be validated
     * @param doAfterRequest A callback lambda to be executed with the result of the validation
     */
    fun validateSpreadsheetID(spreadsheetID: String, doAfterRequest: (Boolean) -> Unit)

}
