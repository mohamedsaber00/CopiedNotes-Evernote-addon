package pro.copytoevernote.material

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class StartOnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val myServiceIntent = Intent(context,MyService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(myServiceIntent)
        } else {
            context.startService(myServiceIntent)
        }


    }
}
