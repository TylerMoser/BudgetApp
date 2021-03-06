package me.tylermoser.budget.android.services

import kotlinx.coroutines.*

/**
 * A helper class to improve the code readability of executing API requests and then
 * executing a callback.
 *
 * Currently, this is used to execute code from [GoogleSheetsApiV4] asynchronously, and then to
 * execute a callback with the ability to modify the UI of the [android.app.Activity] or
 * [android.support.v4.app.Fragment] that made the API call.
 *
 * @author Tyler Moser
 */
class RequestExecutor() {

    /**
     * Executes a lambda asynchronously and then executes a lambda
     *
     * @param doOnUIThreadAfterExecution The callback
     * @param doOnAsyncThread The code to be executed asynchronously
     */
    inline fun execute(
            crossinline doOnUIThreadAfterExecution: () -> Unit,
            crossinline doOnAsyncThread: () -> Unit
    ) {
        ensureDeviceConnectedToNetwork()
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                doOnAsyncThread()
            }
            doOnUIThreadAfterExecution()
        }
    }

    /**
     * Executes a lambda asynchronously and then executes a lambda acting on the result of the
     * asynchronous lambda
     *
     * @param doOnUIThreadAfterExecution The callback
     * @param doOnAsyncThread The code to be executed asynchronously
     */
    inline fun <reified T> execute(
            crossinline doOnUIThreadAfterExecution: (T) -> Unit,
            crossinline doOnAsyncThread: () -> T
    ) {
        ensureDeviceConnectedToNetwork()
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.Default) {
                doOnAsyncThread()
            }
            doOnUIThreadAfterExecution(result)
        }
    }

    /**
     * Throws a [NoNetworkConnectionException] if the device is not connected to a network
     */
    fun ensureDeviceConnectedToNetwork() {
        //if (!NetworkConnectionManager().isDeviceConnectedToNetwork(activity))
        //    throw NoNetworkConnectionException()
    }
}
