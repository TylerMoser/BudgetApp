package me.tylermoser.budget.android.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.expense_list_entry.view.*
import me.tylermoser.budget.R
import me.tylermoser.budget.android.models.BudgetSheet
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * An adapter used by the [RecyclerView] that shows a scrollable list of the expenses on a budget
 *
 * @author Tyler Moser
 */
class ViewExpensesRecyclerViewAdapter(var sheet: BudgetSheet) :
        RecyclerView.Adapter<ViewExpensesRecyclerViewAdapter.InternalViewHolder>() {

    private val dateFromExpense = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private val dayOfWeekFromDate = SimpleDateFormat("EEEE", Locale.getDefault())
    private val dateStringFromDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    /**
     * An internal helper class for populating the UI for each expense in the [RecyclerView]
     */
    class InternalViewHolder(override val containerView: View)
            : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val dayOfWeekTextView: TextView = containerView.dayOfWeekExpenseIncurredTextView
        val expenseDateTextView: TextView = containerView.expenseDateTextView
        val expenseDescriptionTextView: TextView = containerView.expenseDescriptionTextView
        val expenseAmountTextView: TextView = containerView.expenseAmountTextView
    }

    /**
     * Populates the UI for each expense in the [RecyclerView]
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InternalViewHolder {
        val entryView = LayoutInflater.from(parent.context).inflate(R.layout.expense_list_entry, parent, false)
        return InternalViewHolder(entryView)
    }

    /**
     * Callback method for setting the data to be displayed in the UI for each expense in the
     * [RecyclerView]
     */
    override fun onBindViewHolder(holder: InternalViewHolder, position: Int) {
        try {
            val date = dateFromExpense.parse(sheet.expenses[position].date)
            holder.dayOfWeekTextView.text = dayOfWeekFromDate.format(date)
            holder.expenseDateTextView.text = dateStringFromDate.format(date)
        } catch (_: ParseException) {
            holder.dayOfWeekTextView.text = ""
            holder.expenseDateTextView.text = ""
        }

        holder.expenseDescriptionTextView.text = sheet.expenses[position].expense
        holder.expenseAmountTextView.text = sheet.expenses[position].amount.toString()
    }

    /**
     * Gets the number of expenses displayed in the [RecyclerView]
     */
    override fun getItemCount() = sheet.expenses.size

}
