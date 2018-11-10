package me.tylermoser.budget.android.fragments

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import me.tylermoser.budget.R

/**
 * The [Fragment] shown by [ArchivedBudgetFragment] in a [android.support.design.widget.TabLayout]
 * to allow the user to change the budget's settings
 *
 * @author Tyler Moser
 */
class ArchivedBudgetSettingsFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    /**
     * Populates the UI when the [Fragment] is created.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_archived_budget_settings, container, false)
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
     * Null the fragment interaction interface element when this [Fragment] is removed
     */
    override fun onDetach() {
        super.onDetach()
        listener = null
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
        fun newInstance() = ArchivedBudgetSettingsFragment()
    }
}
