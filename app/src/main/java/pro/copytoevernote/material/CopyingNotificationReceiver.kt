package pro.copytoevernote.material

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.evernote.client.android.EvernoteSession
import com.evernote.client.android.EvernoteUtil
import com.evernote.client.android.asyncclient.EvernoteCallback
import com.evernote.edam.type.Note
import java.text.SimpleDateFormat
import java.util.*

class CopyingNotificationReceiver : BroadcastReceiver() {

    val TAG = "CopyBroadCast"

    override fun onReceive(context: Context, intent: Intent) {

        saveToNote(context,intent.getStringExtra("copied_text"))
        initMainNotification(context)
    }


    fun initMainNotification(context: Context) {
        val intent = Intent(context, SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)


        val builder = NotificationCompat.Builder(context,"copytoevernote")
        builder.setContentTitle("Waiting for copied text")
        builder.setSubText("Service is running")
        builder.setContentIntent(pendingIntent)
        builder.setSmallIcon(R.drawable.ic_logo_200)

        val notification = builder.build()

        with(NotificationManagerCompat.from(context)) {
            notify(1, notification)
        }

    }



    fun saveToNote(context: Context,copiedText : String){
        if (!EvernoteSession.getInstance().isLoggedIn) {
            //You need to log in
            Log.d(TAG, "User not logged in")

            return
        }


        val objDate = Date()
        val objSDF = SimpleDateFormat("EEE-dd-MM-yyyy HH:mm")
        val dateString = objSDF.format(objDate)


        val noteStoreClient = EvernoteSession.getInstance().evernoteClientFactory.noteStoreClient
        Log.d(TAG, "Start Saving notes")

        val note = Note()
        note.setTitle("Copied note on"  +  "$dateString")
        note.setContent(EvernoteUtil.NOTE_PREFIX + copiedText + EvernoteUtil.NOTE_SUFFIX);

        noteStoreClient.createNoteAsync(note, object : EvernoteCallback<Note> {
            override fun onSuccess(result: Note) {
                Toast.makeText(context, context.getString(R.string.note_created_success), Toast.LENGTH_LONG).show()
                Log.d(TAG, "Note Saved")

            }

            override fun onException(exception: Exception) {
                Toast.makeText(context, context.getString(R.string.note_created_failed), Toast.LENGTH_LONG).show()

                Log.e(TAG, "Error creating note", exception)
            }
        })

    }
}
