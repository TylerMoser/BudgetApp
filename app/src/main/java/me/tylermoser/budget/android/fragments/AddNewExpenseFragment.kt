package me.tylermoser.budget.android.fragments

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.fragment_add_new_expense.*

import me.tylermoser.budget.R
import me.tylermoser.budget.android.activities.BudgetActivity
import me.tylermoser.budget.android.models.Expense
import me.tylermoser.budget.android.utils.hideKeyboard
import me.tylermoser.budget.android.utils.onSubmit
import java.text.SimpleDateFormat
import java.util.*

/**
 * The [Fragment] that allows the user to add new expenses to the currently opened budget. This
 * [Fragment] is the first [Fragment] shown in the [android.support.design.widget.TabLayout] for
 * active budgets.
 *
 * @author Tyler Moser
 *
 * @see ActiveBudgetFragment
 */
class AddNewExpenseFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private val appendExpense by lazy {resources.getInteger(R.integer.authentication_request_code_append_expense)}
    private val parentActivity by lazy {activity as BudgetActivity}
    private val parentBudgetFragment by lazy {parentFragment as ActiveBudgetFragment}

    /**
     * Populates the UI when this [Fragment] is created
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_add_new_expense, container, false)
        return v
    }

    /**
     * Sets up the fragment interaction interface when the [Fragment] is attached
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    /**
     * When the [Activity] containing this [Fragment] is created, set the date
     * [android.widget.EditText] to the current date, and set the handler for when the user presses
     * enter after entering an amount to try and post the new expense to Google's servers.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        expenseDateEditText.hint = SimpleDateFormat("M/d/yyyy", Locale.US).format(Date())

        expenseAmountEditText.onSubmit {
            if (expenseDateEditText.text.toString().trim().isEmpty()) {
                expenseDateEditText.setText(SimpleDateFormat("M/d/yyyy", Locale.US).format(Date()))
            }
            if (!expenseDescriptionEditText.text.toString().trim().isEmpty()
                    && !expenseAmountEditText.text.toString().trim().isEmpty()) {
                if (parentActivity.authenticator.authenticate(appendExpense)) {
                    submitNewExpense()
                }
            }
        }
    }

    /**
     * Null the fragment interaction interface element when this [Fragment] is removed
     */
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * Executes the query to add a new expense to a budget and updates the UI accordingly
     */
    fun submitNewExpense() {
        hideKeyboard(activity as Activity)
        submitNewExpenseButton.text = "In Progress"
        val expenseAmountSent = expenseAmountEditText.text.toString().toInt()

        parentActivity.api.appendNewExpense(
                parentActivity.spreadsheetID,
                parentBudgetFragment.sheetID,
                parentBudgetFragment.sheetName,
                Expense(getDateStringFromEditText(),
                        expenseDescriptionEditText.text.toString(),
                        expenseAmountEditText.text.toString().toInt()))
        {
            parentActivity.leftoverTextView.text =
                    (parentActivity.leftoverTextView.text.toString().toInt()
                            - expenseAmountSent).toString()

            expenseDateEditText.setText("")
            expenseDateEditText.hint = SimpleDateFormat("M/d/yyyy", Locale.US).format(Date())
            expenseDescriptionEditText.setText("");
            expenseAmountEditText.setText("")

            // Resets the contents of the Activity to reflect the new changes, as well as any
            // changes that others have made to the sheet since the activity was loaded
            //      Resolving issue found when updating app for start of 2020:
            // When a new expense is added and the refresh kicks off afterwards, even though the
            // update request has completed the refresh returns the not-updated sheet unless a
            // slight delay is added before making the request to get the sheet. Instead of adding
            // this delay, the change is being made to update the cache when the expense is added
            // and update the UI using the cache.
            //      This has the disadvantage of not pulling in changes made by other users, but
            // that is not a real issue at this point in time.
            //parentBudgetFragment.refresh()
            parentBudgetFragment.loadSheetFromCache()

            submitNewExpenseButton?.text = "Completed"
            Handler().postDelayed({
                submitNewExpenseButton?.text = "Submit"
            }, 2000)
        }
    }

    /**
     * Change the submit expense button's text to "Submit" again after the attempt to post a new
     * expense fails to convey to the user that they can try again.
     */
    fun resetSubmitAfterFailure() {
        submitNewExpenseButton.text = "Submit"
    }

    /**
     * If the user has entered a date into the date [EditText], use that for getting the expense's
     * date. Otherwise, attempt to use the hint (set to be the current date)
     */
    private fun getDateStringFromEditText(): String {
        return if (expenseDateEditText.text.toString().trim() == "") {
            expenseDateEditText.hint.toString()
        } else {
            expenseDateEditText.text.toString()
        }
    }

    /**
     * An interface provided by Android for communicating between a [Fragment] and an [Activity]
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Creates a new instance of this [Fragment]
         */
        @JvmStatic
        fun newInstance() = AddNewExpenseFragment()
    }

}
