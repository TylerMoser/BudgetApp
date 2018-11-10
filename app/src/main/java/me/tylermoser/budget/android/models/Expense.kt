package me.tylermoser.budget.android.models

import com.google.api.services.sheets.v4.model.ValueRange

/**
 * A single expense on a budget
 *
 * @author Tyler Moser
 */
data class Expense(var date: String, var expense: String, var amount: Int) {

    /**
     * Converts the [Expense] to Google's object for use in the Google Sheets API's
     */
    fun toValueRange(): ValueRange {
        val values = mutableListOf(mutableListOf(
                this.date as Any,
                this.expense as Any,
                this.amount.toString() as Any
        ))
        return ValueRange().setValues(values)
    }

}
