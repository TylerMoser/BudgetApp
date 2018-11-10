package me.tylermoser.budget.android.models

/**
 * A helper class meant to store the name and ID of a sheet
 *
 * @author Tyler Moser
 */
data class NameIDPair(
        var sheetName: String,
        var sheetId: String
) {

    /**
     * A textual representation of the data in this class
     */
    override fun toString(): String {
        return "sheetName= $sheetName, sheetID= $sheetId"
    }

}
