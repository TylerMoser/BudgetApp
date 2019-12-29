package me.tylermoser.budget.android.fragments

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_view_expenses.*
import me.tylermoser.budget.R
import me.tylermoser.budget.android.activities.BudgetActivity
import me.tylermoser.budget.android.adapters.ViewExpensesRecyclerViewAdapter
import me.tylermoser.budget.android.models.BudgetSheet

/**
 * The [Fragment] used by [ActiveBudgetFragment] and [ArchivedBudgetFragment] in a
 * [android.support.design.widget.TabLayout] to show the user the
 * [me.tylermoser.budget.android.models.Expense]s currently on the budget.
 *
 * @author Tyler Moser
 */
class ViewExpensesFragment : Fragment() {

    lateinit var cachedSheet: BudgetSheet

    private var listener: OnFragmentInteractionListener? = null
    private val getSheet by lazy {resources.getInteger(R.integer.authentication_request_code_get_sheet)}
    private val parentActivity by lazy { activity as BudgetActivity }
    private val parentBudgetFragment by lazy {parentFragment as BudgetFragment}

    /**
     * Populates the UI when the [Fragment] is created.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_view_expenses, container, false)
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
     * When the [Activity] containing this [Fragment] is created, set the scrollable list and its
     * associated refresh handler.
     *
     * @see android.support.v7.widget.RecyclerView
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (::cachedSheet.isInitialized) setupRecyclerView(cachedSheet)

        swipeRefreshLayout.setOnRefreshListener {
            if (parentActivity.authenticator.authenticate(getSheet)) {
                parentBudgetFragment.refresh()
            } else {
                swipeRefreshLayout?.isRefreshing = false
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
     * Sets up the [android.support.v7.widget.RecyclerView] to show the list of
     * [me.tylermoser.budget.android.models.Expense]s associated with this budget
     *
     * @param sheet The [BudgetSheet] that holds the list of expenses to populate the
     * [android.support.v7.widget.RecyclerView] with
     */
    fun setupRecyclerView(sheet: BudgetSheet) {
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(parentActivity)
        recyclerView?.adapter = ViewExpensesRecyclerViewAdapter(sheet)
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
        @JvmStatic fun newInstance() = ViewExpensesFragment()
    }
}
