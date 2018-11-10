package me.tylermoser.budget.android.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_change_spreadsheet_dialog.*
import me.tylermoser.budget.R
import me.tylermoser.budget.android.activities.BudgetActivity
import me.tylermoser.budget.android.utils.onSubmit

/**
 * A [DialogFragment] for changing the Google Spreadsheet that the App uses.
 *
 * At this point, this dialog only allows the user to type in a Spreadsheet ID, and then validates
 * it before making the required app changes.
 *
 * @author Tyler Moser
 */
class ChangeSpreadsheetDialogFragment : DialogFragment() {

    private lateinit var thisDialog: AlertDialog

    private val budgetActivity by lazy { activity as BudgetActivity }

    /**
     * Populates the Dialog UI when it is created
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_change_spreadsheet_dialog, container)
    }

    /**
     * Creates the [Dialog] with its UI and title.
     * The Positive button defined here is overridden in [onResume]
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(activity?.layoutInflater?.inflate(R.layout.fragment_change_spreadsheet_dialog, null))
                    .setTitle("Change Spreadsheet")
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
        thisDialog.newSpreadsheetIDEditText.onSubmit { onPositiveButtonClick(positiveButton) }
        positiveButton.setOnClickListener { onPositiveButtonClick(it) }
    }

    /**
     * The logic executed when the positive button is clicked. Updates the UI and validates the
     * entered spreadsheet ID before calling back to the [BudgetActivity] to update the app to
     * reflect this new spreadsheet ID
     */
    private fun onPositiveButtonClick(button: View) {
        button.isEnabled = false
        thisDialog.newSpreadsheetIDEditText.isEnabled = false
        thisDialog.refreshRotatableImageView.visibility = View.VISIBLE
        thisDialog.refreshRotatableImageView.startRotation()
        thisDialog.infoTextView.visibility = View.VISIBLE
        thisDialog.infoTextView.setText("Validating the entered spreadsheet ID")

        val enteredSpreadsheetID = thisDialog.newSpreadsheetIDEditText.text.toString()
        budgetActivity.api.validateSpreadsheetID(enteredSpreadsheetID) {
            if (it) {
                thisDialog.infoTextView.setText("Spreadsheet ID successfully validated")
                budgetActivity.changeSpreadsheet(enteredSpreadsheetID)
                thisDialog.dismiss()
            } else {
                thisDialog.refreshRotatableImageView.stopRotation()
                thisDialog.infoTextView.setText("Could not validate the entered spreadsheet ID")
                thisDialog.newSpreadsheetIDEditText.isEnabled = true
                button.isEnabled = true
            }
        }
    }

    companion object {
        /**
         * Creates a new [ChangeSpreadsheetDialogFragment]
         */
        @JvmStatic fun newInstance() = ChangeSpreadsheetDialogFragment()
    }

}
