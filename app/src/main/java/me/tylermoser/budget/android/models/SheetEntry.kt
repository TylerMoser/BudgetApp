package me.tylermoser.budget.android.models

/**
 * Represents the metadata associated with a sheet
 *
 * @author Tyler Moser
 */
data class SheetEntry(
    var sheetName: String,
    var sheetId: String,
    var archived: Boolean = false
) {

    /**
     * A textual representation of the data in this class
     */
    override fun toString(): String {
        return "sheetName= $sheetName, sheetID= $sheetId, archived= $archived"
    }

}
