package me.tylermoser.budget.android.models

import com.google.api.services.sheets.v4.model.ValueRange

/**
 * A representation of the data associated with a single sheet in a Google spreadsheet and therefore
 * a single budget
 *
 * @author Tyler Moser
 */
data class BudgetSheet(
        val toSpend: Int,
        val runningLeftover: Int,
        val expenses: MutableList<Expense>
) {
    val spendable: Int
        get() = toSpend + runningLeftover
    val spent: Int
        get() = expenses.sumBy { it.amount }
    val leftover: Int
        get() = spendable - spent

    /**
     * Adds a single expense to the list of expenses on this sheet
     */
    fun addExpense(expense: Expense) = expenses.add(expense)

    /**
     * Creates the initial sheet data to be sent to Google when the sheet is created
     */
    fun createEmptySheet(): ValueRange {
        return ValueRange().setValues(listOf(
                listOf("To Spend:", "Running Leftover:", "Spendable:", "Spent:", "Leftover:"),
                listOf(toSpend.toString(), runningLeftover.toString(), "=A2+B2", "=SUM(C5:C)", "=C2-D2"),
                listOf(),
                listOf("Date:", "Expense:", "Amount:")
        ))
    }

    /**
     * Prints a textual representation of the data on this sheet
     */
    override fun toString(): String {
        var s: String = "toSpend= $toSpend, runningLeftover= $runningLeftover, spendable= $spendable, spent= $spent, leftover= $leftover, expenses= ["
        for (expense in expenses)
            s += "{$expense}, "
        s += "]"
        return s
    }
}
