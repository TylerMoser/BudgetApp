package me.tylermoser.budget.android.services

import android.app.Activity
import android.support.design.widget.Snackbar
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.SheetsScopes
import kotlinx.android.synthetic.main.content_home.*
import me.tylermoser.budget.android.activities.BudgetActivity
import java.util.*

/**
 * A helper class to assist in authentication with Google's servers for using Google's API's
 *
 * @author Tyler Moser
 */
class Authenticator(private val activity: Activity) {

    private val signInOptions : GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
                    .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
                    .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                    .requestEmail()
                    .build()
    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(activity, signInOptions)
    }
    val googleAccountCredential: GoogleAccountCredential by lazy {
        GoogleAccountCredential
                .usingOAuth2(activity, Collections.singleton(SheetsScopes.SPREADSHEETS))
                .setBackOff(ExponentialBackOff())
    }

    /**
     * Gets a [View] to display a [Snackbar] on if one exists
     */
    private val view: View by lazy {
        when (activity) {
            is BudgetActivity -> {activity.activeBudgetConstraintLayout}
            else -> {throw RuntimeException("activity type not supported by authenticator")}
        }
    }

    /**
     * Authenticates the user with Google's servers.
     *
     * @param authenticationRequestCode A code matching up with a value in
     * [BudgetActivity.onActivityResult] for what logic to execute after the user has finished
     * authenticating. This code is intended to allow the app to ask the user for authentication
     * when they first need it, and then continue with the action that needed the authentication.
     */
    fun authenticate(authenticationRequestCode: Int): Boolean {
        if (!NetworkConnectionManager().isDeviceConnectedToNetwork(activity)) {
            Snackbar.make(view,
                        "Cannot authenticate user. Device must be connected to a network."
                        , Snackbar.LENGTH_SHORT)
                    .show()
            return false
        }

        setUserToLastSignedIn()
        if (googleAccountCredential.selectedAccount == null) {
            activity.startActivityForResult(googleSignInClient.signInIntent, authenticationRequestCode)
            return false
        } else {
            return true
        }
    }

    /**
     * Sets the current user to the one who signed in last
     */
    fun setUserToLastSignedIn() {
        googleAccountCredential.selectedAccount = GoogleSignIn.getLastSignedInAccount(lazyOf(activity).value)?.account
    }

    /**
     * De-authenticate the user with Google's servers
     */
    fun signOut() {
        googleSignInClient.signOut()
        googleSignInClient.revokeAccess()
    }

}
