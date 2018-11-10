package me.tylermoser.budget.android.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import me.tylermoser.budget.R

/**
 * A [NavDrawerFragment] for displaying settings applicable to the entire app
 *
 * @author Tyler Moser
 */
class AppSettingsFragment : NavDrawerFragment() {

    /**
     * Populates the UI and makes the needed changes to the App Bar when this [Fragment] is loaded
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_app_settings, container, false)
        setAppBar()
        return v
    }

    /**
     * Makes the changes to the App Bar for this [NavDrawerFragment]. Most notably, this decreases
     * the height of the App Bar to roughly the standard Android height.
     */
    override fun setAppBar() {
        budgetActivity.appBar.editForSettings(this)
    }

    companion object {
        /**
         * Creates a new [AppSettingsFragment]
         */
        @JvmStatic
        fun newInstance() = AppSettingsFragment().apply {}
    }

}
