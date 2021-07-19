package to.tawk.githubusers.util

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView


class NetworkStatusUtil(
    var context: Context,
    var onNetworkAliveListener: () -> Unit
) : Activity() {
    var br: BroadcastReceiver? = null
    fun build(v: AppCompatTextView) {
        if (br == null) {
            br = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val extras = intent.extras
                    val info = extras!!.getParcelable<Parcelable>("networkInfo") as NetworkInfo?
                    val state = info!!.state
                    Log.d("Network status", "$info $state")
                    if (state == NetworkInfo.State.CONNECTED) {
                        v.visibility = View.GONE
                        onNetworkAliveListener()
                    } else {
                        v.visibility = View.VISIBLE
                    }
                }
            }
            registerNetworkBroadcastReceiver(context)
        }
    }



    private fun registerNetworkBroadcastReceiver(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.registerReceiver(br, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    protected fun unregisterNetworkChanges(context: Context) {
        try {
            context.unregisterReceiver(br)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChanges(context)
    }
}