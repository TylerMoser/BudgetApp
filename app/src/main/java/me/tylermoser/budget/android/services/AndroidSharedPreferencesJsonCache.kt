package me.tylermoser.budget.android.services

import android.content.Context
import com.google.gson.Gson
import me.tylermoser.budget.R
import me.tylermoser.budget.android.models.BudgetSheet
import me.tylermoser.budget.android.models.NameIDPair
import me.tylermoser.budget.android.models.SheetEntry

/**
 * This class uses Android's shared preferences mapping feature to implement a cache. Primitive
 * variables are stored using the shared preferences methods of their type, and objects are stored
 * as Strings in JSON representation.
 *
 * This is probably not the fastest cache, and it is attempting to use a feature primarily intended
 * for saving settings to store data, but it was the easiest cache to get working during development
 * and it has proven fast enough for all current use cases
 *
 * @author Tyler Moser
 *
 * @param context Use application context to have this cache "in-context" for the entire app
 */
class AndroidSharedPreferencesJsonCache(context: Context) : Cache {

    private val sharedPreferences =
        context.getSharedPreferences(context.resources.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
    private val openOnLoadKey = context.resources.getString(R.string.open_on_load_key)
    private val spreadsheetKey = context.resources.getString(R.string.spreadsheet_key)
    private val gson = Gson()

    /*
    init {
        //putSpreadsheetID("1JkL290-LpDrrNNRug-bmA4_-kVYkdjbwPGXx85_frh4")
        //clearCache()
    }
    */

    private val emptyPairString = gson.toJson(NameIDPair("", ""))

    override fun clearCache() {
        sharedPreferences.edit().clear().apply()
    }

    // =============================================================================================

    override fun putBudgetSheet(sheetID: String, budgetSheet: BudgetSheet) {
        val budgetSheetString = gson.toJson(budgetSheet)
        sharedPreferences.edit().putString(sheetID, budgetSheetString).apply()
    }

    override fun getBudgetSheet(sheetID: String): BudgetSheet? {
        return if (sharedPreferences.contains(sheetID)) {
            val budgetSheetString = sharedPreferences.getString(sheetID, "")
            gson.fromJson(budgetSheetString, BudgetSheet::class.java)
        } else {
            null;
        }
    }

    // =============================================================================================

    override fun putNavigationDrawerSheetEntriesList(spreadsheetID: String, sheetEntries: List<SheetEntry>) {
        val sheetEntriesString = gson.toJson(sheetEntries)
        sharedPreferences.edit().putString(spreadsheetID, sheetEntriesString).apply()
    }

    override fun getNavigationDrawerSheetEntriesList(spreadsheetID: String): MutableList<SheetEntry>? {
        return if (sharedPreferences.contains(spreadsheetID)) {
            val sheetEntriesString = sharedPreferences.getString(spreadsheetID, "")
            val sheetEntriesArray
                    = gson.fromJson(sheetEntriesString, Array<SheetEntry>::class.java)
            sheetEntriesArray.toMutableList()
        } else {
            null
        }
    }

    // =============================================================================================

    override fun putSpreadsheetID(spreadsheetID: String) {
        sharedPreferences.edit().putString(spreadsheetKey, spreadsheetID).apply()
    }

    override fun getSpreadsheetID(): String {
        return sharedPreferences.getString(spreadsheetKey, "") as String
    }

    override fun isSpreadsheetIDValid(): Boolean {
        return getSpreadsheetID() != ""
    }

    // =============================================================================================

    override fun clearOpenOnLoad() {
        putOpenOnLoad("", "")
    }

    override fun putOpenOnLoad(sheetID: String, sheetName: String) {
        val pair = NameIDPair(sheetName, sheetID)
        sharedPreferences.edit().putString(openOnLoadKey, gson.toJson(pair)).apply()
    }

    override fun getOpenOnLoad(): NameIDPair {
        var pairString = sharedPreferences.getString(openOnLoadKey, "")
        if (pairString == "") pairString = emptyPairString
        return gson.fromJson(pairString, NameIDPair::class.java)
    }

    override fun isOpenOnLoadValid(): Boolean {
        return !(getOpenOnLoad().sheetId == "" || getOpenOnLoad().sheetName == "")
    }

}
