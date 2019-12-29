package me.tylermoser.budget.android.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.tylermoser.budget.R

class ReorderActiveBudgetsFragment : NavDrawerFragment() {

    /**
     * Populates the UI and makes the needed changes to the App Bar when this [Fragment] is loaded
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_reorder_active_budgets, container,
                false)
        setAppBar()
        return v
    }

    /**
     * Makes the changes to the App Bar for this [NavDrawerFragment]. Most notably, this decreases
     * the height of the App Bar to roughly the standard Android height.
     */
    override fun setAppBar() {
        budgetActivity.appBar.editForSettings(this, "Reorder Active Budgets")
    }

    companion object {
        /**
         * Creates a new [ReorderActiveBudgetsFragment]
         */
        @JvmStatic
        fun newInstance() = ReorderActiveBudgetsFragment().apply {}
    }

}
