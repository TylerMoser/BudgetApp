package me.tylermoser.budget.android.utils

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Hides the Android on-screen keyboard if it is visible
 *
 * @param activity The [Activity] that the keyboard is being displayed on
 */
fun hideKeyboard(activity: Activity) {
    val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0);
}

/**
 * Sets a listener to be executed when a user presses enter on the keyboard when the current
 * [EditText] is selected.
 *
 * @param func The lambda to be executed when enter is pressed
 */
fun EditText.onSubmit(func: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            func()
        }
        true
    }
}

/**
 * Sets the height of a UI element in DP
 */
fun ViewGroup.LayoutParams.setHeightDP(dp: Int, fragment: Fragment) {
    val density = fragment.context?.resources?.displayMetrics?.density
            ?: throw RuntimeException("Unable to calculate dp")
    height = (dp * density).toInt()
}
