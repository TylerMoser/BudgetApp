package me.tylermoser.budget.android.services

import com.google.api.services.sheets.v4.model.*
import com.google.gson.Gson
import me.tylermoser.budget.R
import me.tylermoser.budget.android.models.SheetEntry

/**
 * This class assists in creating an update metadata request
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
class UpdateMetadataRequestCreator(
        private val spreadsheetID: String,
        private val sheetEntries: List<SheetEntry>
) {
    private val gson = Gson()

    /**
     * Creates the update metadata request
     */
    fun create(): BatchUpdateSpreadsheetRequest {
        val sheetEntriesToSaveJson = gson.toJson(sheetEntries)
        val updateMetadataLocation = DeveloperMetadataLocation().apply {
            spreadsheet = true
        }
        val updateMetadata = DeveloperMetadata().apply {
            location = updateMetadataLocation
            metadataId = R.integer.sheet_info_developer_metadata_key
            metadataKey = spreadsheetID
            metadataValue = sheetEntriesToSaveJson
            visibility = "DOCUMENT"
        }
        val metadataLookup = DeveloperMetadataLookup().apply {
            metadataLocation = updateMetadataLocation
            locationMatchingStrategy = "EXACT_LOCATION"
            metadataId = R.integer.sheet_info_developer_metadata_key
            metadataKey = spreadsheetID
            visibility = "DOCUMENT"
        }
        val dataFilter = DataFilter().apply {
            developerMetadataLookup = metadataLookup
        }
        val updateMetadataRequest = UpdateDeveloperMetadataRequest().apply {
            developerMetadata = updateMetadata
            dataFilters = listOf(dataFilter)
            fields = "*"
        }
        val updateRequest = Request().apply {
            updateDeveloperMetadata = updateMetadataRequest
        }
        val updateMetadataRequestList = listOf(updateRequest)
        return BatchUpdateSpreadsheetRequest().apply {
            requests = updateMetadataRequestList
        }
    }
}
