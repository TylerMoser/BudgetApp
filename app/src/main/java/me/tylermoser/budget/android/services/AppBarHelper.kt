package me.tylermoser.budget.android.services

import android.support.v4.app.Fragment
import android.view.View
import kotlinx.android.synthetic.main.app_bar_home.*
import me.tylermoser.budget.android.activities.BudgetActivity
import me.tylermoser.budget.android.utils.setHeightDP

/**
 * A helper class for adjusting the App Bar to meet the desired appearance for different
 * open [Fragment]s
 *
 * It was determined that it is infeasible to have the different App Bar layouts in xml and inflate
 * each one when needed, and the changes being made to the app bar are simple enough to allow for
 * changes to be made to the UI instead of re-creating it.
 *
 * @author Tyler Moser
 */
class AppBarHelper(private val budgetActivity: BudgetActivity) {

    /**
     * Set the App Bar to match the desired appearance for a
     * [me.tylermoser.budget.android.fragments.AppSettingsFragment]
     */
    fun editForSettings(fragment: Fragment) {
        with(budgetActivity) {
            toolbar.layoutParams.setHeightDP(54, fragment)
            leftoverTextView.setText("")
            toSpendTextView.setText("")
            slash.setText("")
            refreshButton.visibility = View.INVISIBLE
            refreshButton.isEnabled = false
            appSettingsTextView.setText("App Settings")
        }
    }

    /**
     * A parent method that sets the App Bar to match the desired appearance for all descendants of
     * [me.tylermoser.budget.android.fragments.BudgetFragment]
     */
    private fun editForBudget() {
        with(budgetActivity) {
            refreshButton.visibility = View.VISIBLE
            leftoverTextView.textSize = 40f;
            refreshButton.isEnabled = true
            appSettingsTextView.setText("")
        }
    }

    /**
     * Set the App Bar to match the desired appearance for a
     * [me.tylermoser.budget.android.fragments.ActiveBudgetFragment]
     */
    fun editForActiveBudget(fragment: Fragment) {
        editForBudget()
        with (budgetActivity) {
            toolbar.layoutParams.setHeightDP(126, fragment)
        }
    }

    /**
     * Set the App Bar to match the desired appearance for a
     * [me.tylermoser.budget.android.fragments.ArchivedBudgetFragment]
     */
    fun editForArchivedBudget(fragment: Fragment) {
        editForBudget()
        with(budgetActivity) {
            toolbar.layoutParams.setHeightDP(86, fragment)
        }
    }

}
