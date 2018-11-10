package me.tylermoser.budget.android.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add_new_budget_dialog.*
import me.tylermoser.budget.R
import me.tylermoser.budget.android.activities.BudgetActivity
import me.tylermoser.budget.android.models.BudgetSheet
import me.tylermoser.budget.android.utils.hideKeyboard
import me.tylermoser.budget.android.utils.onSubmit

/**
 * A [DialogFragment] for adding a new budget. This adds a new sheet to the spreadsheet that the
 * app is using.
 *
 * @author Tyler Moser
 */
class AddNewBudgetDialogFragment : DialogFragment() {

    private lateinit var thisDialog: AlertDialog

    private val budgetActivity by lazy { activity as BudgetActivity }

    /**
     * Validates the data before sending the request to create a new sheet.
     *
     * As of right now, this only ensures that something is entered for the new budget's name.
     * Both of the amounts are set to zero if they are not populated.
     */
    private val isValidInput: Boolean get() {
        return if (thisDialog.newBudgetNameEditText.text.toString().isEmpty()) {
            thisDialog.infoTextView.visibility = View.VISIBLE
            thisDialog.infoTextView.setText("Enter a name for the new budget")
            false
        } else true
    }

    /**
     * Populates the Dialog UI when it is created
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_new_budget_dialog, container, false)
    }

    /**
     * Creates the [Dialog] with its UI and title.
     * The Positive button defined here is overridden in [onResume]
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(activity?.layoutInflater?.inflate(R.layout.fragment_add_new_budget_dialog, null))
                    .setTitle("Add a New Budget")
                    .setPositiveButton("OK") { _, _ -> }
                    .setNegativeButton("Cancel") { _, _ -> }
            thisDialog = builder.create()
            thisDialog
        } ?: throw IllegalStateException("Cannot create a dialog on a null activity")
    }

    /**
     * Hides the "working" wheel and info text views and sets the positive button logic when the
     * [DialogFragment] is resumed.
     */
    override fun onResume() {
        super.onResume()

        thisDialog.refreshRotatableImageView.visibility = View.GONE // Start the working spinner
        thisDialog.infoTextView.visibility = View.GONE // Show the textual message

        val positiveButton = thisDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        thisDialog.rolloverAmountEditText.onSubmit {
            if (isValidInput) {
                onPositiveButtonClick(positiveButton)
            }
        }
        positiveButton.setOnClickListener {
            if (isValidInput) {
                onPositiveButtonClick(it)
            }
        }
    }

    /**
     * The logic executed when the positive button is clicked.
     *
     * Updates the [Dialog] UI
     * Executes a query to add a new sheet to the spreadsheet
     * Prepares the [BudgetActivity] UI
     */
    private fun onPositiveButtonClick(button: View) {
        button.isEnabled = false
        hideKeyboard(budgetActivity)

        val newBudgetName = thisDialog.newBudgetNameEditText.text.toString()
        val newBudgetAmount = thisDialog.budgetStartingAmountEditText.text.toString().toIntOrNull() ?: 0
        val newBudgetRolloverAmount = thisDialog.rolloverAmountEditText.text.toString().toIntOrNull() ?: 0

        thisDialog.newBudgetNameEditText.isEnabled = false
        thisDialog.budgetStartingAmountEditText.isEnabled = false
        thisDialog.rolloverAmountEditText.isEnabled = false

        thisDialog.refreshRotatableImageView.visibility = View.VISIBLE
        thisDialog.refreshRotatableImageView.startRotation()
        thisDialog.infoTextView.visibility = View.VISIBLE
        thisDialog.infoTextView.setText("Adding the new budget")

        val budgetSheet = BudgetSheet(newBudgetAmount, newBudgetRolloverAmount, mutableListOf())

        budgetActivity.api.addSheet(budgetActivity.spreadsheetID, newBudgetName, budgetSheet) {
            thisDialog.infoTextView.setText("New budget successfully added")

            budgetActivity.populateNavigationDrawerFromServer()
            val activeBudgetFragmentForNewBudget
                    = ActiveBudgetFragment.newInstance(budgetActivity, it, newBudgetName)
            budgetActivity.navDrawerHelper.changeMainFragment(activeBudgetFragmentForNewBudget)

            thisDialog.dismiss()
        }
    }

    companion object {
        /**
         * Creates a new [AddNewBudgetDialogFragment]
         */
        @JvmStatic
        fun newInstance() = AddNewBudgetDialogFragment()
    }
}
