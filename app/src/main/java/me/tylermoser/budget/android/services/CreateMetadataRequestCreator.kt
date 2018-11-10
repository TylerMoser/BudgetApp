package me.tylermoser.budget.android.services

import com.google.api.services.sheets.v4.model.*
import com.google.gson.Gson
import me.tylermoser.budget.R
import me.tylermoser.budget.android.models.SheetEntry

/**
 * This class assists in creating a create metadata request
 *
 * To make changes to Google Sheets metadata, Google requires that one of two structures be sent.
 * If metadata already exists, the user must send an update metadata request. Otherwise, the user
 * must send a create metadata request.
 *
 * @author Tyler Moser
 *
 * @param spreadsheetID The ID of the spreadsheet to associate this metadata with
 * @param sheetEntries The metadata to associate with the spreadsheet
 */
class CreateMetadataRequestCreator(
        private val spreadsheetID: String,
        private val sheetEntries: List<SheetEntry>
) {
    private val gson = Gson()

    /**
     * Creates the create metadata request
     */
    fun create(): BatchUpdateSpreadsheetRequest {
        val sheetEntriesToSaveJson = gson.toJson(sheetEntries)
        val createMetadataLocation = DeveloperMetadataLocation().apply {
            spreadsheet = true
        }
        val createMetadata = DeveloperMetadata().apply {
            location = createMetadataLocation
            metadataId = R.integer.sheet_info_developer_metadata_key
            metadataKey = spreadsheetID
            metadataValue = sheetEntriesToSaveJson
            visibility = "DOCUMENT"
        }
        val createMetadataRequest = CreateDeveloperMetadataRequest().apply {
            developerMetadata = createMetadata
        }
        val createRequest = Request().apply {
            createDeveloperMetadata = createMetadataRequest
        }
        val createMetadataRequestList = listOf(createRequest)
        return BatchUpdateSpreadsheetRequest().apply {
            requests = createMetadataRequestList
        }
    }

}
