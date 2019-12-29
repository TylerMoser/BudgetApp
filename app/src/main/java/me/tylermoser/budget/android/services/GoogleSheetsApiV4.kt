package me.tylermoser.budget.android.services

import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import com.google.gson.Gson
import me.tylermoser.budget.R
import me.tylermoser.budget.android.models.BudgetSheet
import me.tylermoser.budget.android.models.Expense
import me.tylermoser.budget.android.models.SheetEntry

/**
 * An implementation of the Google Sheets API for use with Google's Sheets API Version 4
 * This implementation also heavily relies on [RequestExecutor]
 *
 * @author Tyler Moser
 */
class GoogleSheetsApiV4(
        private val authenticator: Authenticator,
        private val cache: Cache? = null
) : GoogleSheetsApi {
    private val executor = RequestExecutor()
    private val httpTransport = AndroidHttp.newCompatibleTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val gson = Gson()
    private val service by lazy {
        Sheets.Builder(httpTransport, jsonFactory, authenticator.googleAccountCredential)
                .setApplicationName("PersonalFinanceApp").build()
    }

    private var developerMetadataExists: Boolean? = null

    // =============================================================================================

    override fun getSheet(
            spreadsheetID: String,
            sheetName: String,
            sheetID: String,
            doAfterRequest: (BudgetSheet) -> Unit
    ) {
        executor.execute(doAfterRequest) {
            val result = service
                    .spreadsheets()
                    .values()
                    .get(spreadsheetID, "'$sheetName'!A:E")
                    .execute()
                    .toBudgetSheet()
            cache?.putBudgetSheet(sheetID, result)
            result
        }
    }

    override fun doesSheetExist(
            spreadsheetID: String,
            sheetID: String,
            doAfterRequest: (String?) -> Unit
    ) {
        executor.execute(doAfterRequest) {
            val result = service.spreadsheets().get(spreadsheetID).execute()
            val listOfSheetsOnSpreadsheet = mutableListOf<SheetEntry>()
            for ((i, sheet) in result.sheets.withIndex()) {
                listOfSheetsOnSpreadsheet.add(SheetEntry(sheet.properties.title, sheet.properties.sheetId.toString()))
            }
            var returnValue: String? = null
            for (entry in listOfSheetsOnSpreadsheet) {
                if (entry.sheetId == sheetID) {
                    returnValue = entry.sheetName
                }
            }
            returnValue
        }
    }

    override fun addSheet(
            spreadsheetID: String,
            sheetName: String,
            budgetSheet: BudgetSheet,
            doAfterRequest: (String) -> Unit
    ) {
        executor.execute(doAfterRequest) {
            val newSheetID = addNewSheetToSpreadsheet(sheetName, spreadsheetID)
            cache?.putBudgetSheet(newSheetID, budgetSheet)
            populateNewSheet(sheetName, budgetSheet, spreadsheetID)
            newSheetID
        }
    }

    // =============================================================================================

    override fun appendNewExpense(
            spreadsheetID: String,
            sheetID: String,
            sheetName: String,
            expense: Expense,
            doAfterRequest: () -> Unit
    ) {
        executor.execute(doAfterRequest) {
            service.spreadsheets().values()
                    .append(spreadsheetID, "'$sheetName'!A:E", expense.toValueRange())
                    .setValueInputOption("USER_ENTERED")
                    .execute()

            // Update cache for this sheet to include the newly appended expense
            cache?.getBudgetSheet(sheetID)?.let {
                it.expenses.add(expense)
                cache.putBudgetSheet(sheetID, it)
            }
        }
    }

    // =============================================================================================

    override fun getSpreadsheetMetadata(
            spreadsheetID: String,
            doAfterRequest: (MutableList<SheetEntry>) -> Unit
    ) {
        executor.execute(doAfterRequest) {
            try {
                val result = service.spreadsheets()
                        .developerMetadata()
                        .get(spreadsheetID, R.integer.sheet_info_developer_metadata_key)
                        .execute()
                        .metadataValue
                        .toString()
                developerMetadataExists = true
                val sheetEntries = gson.fromJson(result, Array<SheetEntry>::class.java).toMutableList()
                cache?.putNavigationDrawerSheetEntriesList(spreadsheetID, sheetEntries)
                sheetEntries
            } catch (googleJsonResponseException: GoogleJsonResponseException) {
                // There is no developer metadata to return, therefore mark as such and return
                // empty
                developerMetadataExists = false
                mutableListOf<SheetEntry>()
            }
        }
    }

    override fun updateSpreadsheetMetadata(spreadsheetID: String, sheetEntries: List<SheetEntry>) {
        if (developerMetadataExists == null) getSpreadsheetMetadata(spreadsheetID) {}

        val request = if (developerMetadataExists as Boolean) {
            UpdateMetadataRequestCreator(spreadsheetID, sheetEntries).create()
        } else {
            CreateMetadataRequestCreator(spreadsheetID, sheetEntries).create()
        }

        executor.execute({
            getSpreadsheetMetadata(spreadsheetID) {}
        }) {
            service.spreadsheets()
                    .batchUpdate(spreadsheetID, request)
                    .execute()
            cache?.putNavigationDrawerSheetEntriesList(spreadsheetID, sheetEntries)
        }
    }

    // =============================================================================================

    override fun getSpreadsheetProperties(
            spreadsheetID: String,
            doAfterRequest: (List<SheetEntry>) -> Unit
    ) {
        executor.execute(doAfterRequest) {
            val result = service.spreadsheets().get(spreadsheetID).execute()
            val listOfSheetsOnSpreadsheet = mutableListOf<SheetEntry>()
            for ((i, sheet) in result.sheets.withIndex()) {
                listOfSheetsOnSpreadsheet.add(SheetEntry(sheet.properties.title, sheet.properties.sheetId.toString()))
            }
            listOfSheetsOnSpreadsheet
        }
    }

    // =============================================================================================

    override fun validateSpreadsheetID(
            spreadsheetID: String,
            doAfterRequest: (Boolean) -> Unit
    ) {
        executor.execute(doAfterRequest) {
            try {
                service.spreadsheets().get(spreadsheetID).execute()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /*
     ************************************* HELPER METHODS ******************************************
     */

    /**
     * Sends a request to add a new sheet to an existing spreadsheet
     *
     * @param sheetName The name of the new sheet to add
     * @param spreadsheetID The ID of the spreadsheet to add the new sheet to
     * @return The sheet ID of the newly created sheet
     */
    private fun addNewSheetToSpreadsheet(sheetName: String, spreadsheetID: String): String {
        val addSheetRequest = AddSheetRequest().apply {
            properties = SheetProperties().apply {
                set("title", sheetName)
            }
        }

        val batchUpdateRequest = BatchUpdateSpreadsheetRequest().apply {
            requests = listOf(Request().apply{addSheet = addSheetRequest})
        }

        val result = service.spreadsheets().batchUpdate(spreadsheetID, batchUpdateRequest).execute()
        return result.replies[0].addSheet.properties.sheetId.toString()
    }

    /**
     * Sends a request to insert the correct starting data for an empty budget sheet. This adds the
     * first 4 rows of the sheet.
     *
     * @param sheetName The name of the sheet being set up
     * @param budgetSheet A [BudgetSheet] object containing information about the new sheet
     * @param spreadsheetID The ID of the spreadsheet containing the sheet that is being set up
     */
    private fun populateNewSheet(
            sheetName: String,
            budgetSheet: BudgetSheet,
            spreadsheetID: String
    ) {
        service.spreadsheets().values()
                .append(spreadsheetID, "'$sheetName'!A:E", budgetSheet.createEmptySheet())
                .setValueInputOption("USER_ENTERED")
                .execute()
    }

    /**
     * Takes the Google sheet data structure, [ValueRange], and converts it to the app sheet data
     * structure, [BudgetSheet].
     */
    private fun ValueRange.toBudgetSheet(): BudgetSheet {
        val values = this.getValues()
        val toSpend = values[1][0]?.toString()?.toInt() ?: -1
        val runningLeftover = values[1][1]?.toString()?.toInt() ?: -1
        val expenses = mutableListOf<Expense>()
        for (i in 4 until values.size) {
            val expenseDate = values[i][0]?.toString() ?: ""
            val expenseName = values[i][1]?.toString() ?: ""
            val expenseAmount = values[i][2]?.toString()?.toInt() ?: -1
            expenses.add(Expense(expenseDate, expenseName, expenseAmount))
        }
        return BudgetSheet(toSpend, runningLeftover, expenses)
    }

}
