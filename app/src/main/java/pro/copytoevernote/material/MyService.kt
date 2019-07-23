package pro.copytoevernote.material

import android.app.Service
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.evernote.client.android.EvernoteSession
import com.evernote.client.android.EvernoteUtil
import com.evernote.client.android.asyncclient.EvernoteCallback
import com.evernote.edam.type.Note
import java.text.SimpleDateFormat
import java.util.*

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.view.WindowManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import io.mattcarroll.hover.window.WindowViewController
import io.mattcarroll.hover.HoverView


class MyService : Service(), ClipboardManager.OnPrimaryClipChangedListener {


    lateinit var clipboardManager: ClipboardManager
    val TAG = "CopyingSevrice"

    lateinit var mInterstitialAd: InterstitialAd

    lateinit var sharedPreferences: SharedPreferences

    lateinit var mainSharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "copying service created")

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(this)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        mainSharedPreferences = getSharedPreferences("main", Context.MODE_PRIVATE)

        mInterstitialAd = InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7071185772281204/5354971795")


        initChannels(this)
        initMainNotification()


    }

    fun initMainNotification() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


        val builder = NotificationCompat.Builder(this, "copytoevernote")
        builder.setContentTitle("Waiting for copied text")
        builder.setSubText("Service is running")
        builder.setContentIntent(pendingIntent)
        builder.setSmallIcon(R.drawable.ic_logo_200)

        val notification = builder.build()
        startForeground(1, notification)
    }


    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "copytoevernote",
            "Copy to Evernote",
            NotificationManager.IMPORTANCE_NONE
        )
        channel.description = "Channel description"
        notificationManager.createNotificationChannel(channel)


    }


    override fun onPrimaryClipChanged() {


        if (clipboardManager.hasPrimaryClip()) {
            //maybe text or image

            if (clipboardManager.primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
                clipboardManager.primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
            ) {
                Log.d(TAG, "copied data is text")

                //its text
                //but maybe plan text or URI
                val item = clipboardManager.primaryClip.getItemAt(0)
                var copiedText = item.text

                if (copiedText != null) {
                    // copied Data is the Text
                    checkCopyMethod(copiedText.toString())
                } else {

                    val copiedURI = item.uri

                    if (copiedURI != null) {
                        // resolve copied text from uri
                        copiedText = copiedURI.toString()
                        checkCopyMethod(copiedText)

                    }
                }

            }

        }

    }

    fun checkCopyMethod(copiedText: String) {
        when (sharedPreferences.getString("confirm_method", "none")) {
            "none" -> saveToNote(copiedText)
            "notification" -> notificationMethod(copiedText)
            "pop_up_bubble" -> hoverMethod(copiedText)

        }
    }


    fun notificationMethod(copiedText: String) {

        val snoozeIntent = Intent(this, CopyingNotificationReceiver::class.java).apply {
            putExtra("copied_text", copiedText)
        }
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, snoozeIntent, 0)


        val builder = NotificationCompat.Builder(this, "copytoevernote")
        builder.setContentTitle("Text Copied")
        builder.setContentText("Tap to save in Evernote")
        builder.setSubText("Service is running")
        builder.setContentIntent(pendingIntent)
        builder.setSmallIcon(R.drawable.ic_logo_200)

        val notification = builder.build()
        startForeground(1, notification)

    }

    fun hoverMethod(copiedText: String) {
        val hoverView = HoverView.createForWindow(
            this,
            WindowViewController(
                getSystemService(Context.WINDOW_SERVICE) as WindowManager
            )
        )

        val hoverMenu = SingleSectionHoverMenu(this)

        hoverView.setMenu(hoverMenu)
        hoverView.addToWindow()
        hoverView.collapse()

        Handler().postDelayed({
            hoverView.release()
            hoverView.removeFromWindow()
        }, 5000)



        hoverView.addOnExpandAndCollapseListener(object : HoverView.Listener {
            override fun onCollapsing() {

            }

            override fun onExpanding() {
                saveToNote(copiedText)
                hoverView.removeFromWindow()
            }

            override fun onClosing() {
            }

            override fun onClosed() {
            }

            override fun onExpanded() {
            }

            override fun onCollapsed() {
            }


        })

    }

    fun saveToNote(copiedText: String) {

        if (!EvernoteSession.getInstance().isLoggedIn) {
            //You need to log in
            Log.d(TAG, "User not logged in")

            return
        }

        //check first if the full version
        if (!mainSharedPreferences.getBoolean("full_version", false)) mInterstitialAd.loadAd(AdRequest.Builder().build())


        val objDate = Date()
        val objSDF = SimpleDateFormat("EEE-dd-MM-yyyy HH:mm")
        val dateString = objSDF.format(objDate)


        val noteStoreClient = EvernoteSession.getInstance().evernoteClientFactory.noteStoreClient

        Log.d(TAG, "Start Saving notes")

        val note = Note()

        note.title = "Copied note on" + "$dateString"
        note.content = EvernoteUtil.NOTE_PREFIX + copiedText + EvernoteUtil.NOTE_SUFFIX;
        note.notebookGuid = sharedPreferences.getString("choose_notebook", null)

        noteStoreClient.createNoteAsync(note, object : EvernoteCallback<Note> {
            override fun onSuccess(result: Note) {
                Toast.makeText(applicationContext, getString(R.string.note_created_success), Toast.LENGTH_LONG).show()
                Log.d(TAG, "Note Saved")

                mInterstitialAd.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        mInterstitialAd.show()

                    }

                    override fun onAdClosed() {
                        // Load the next interstitial.
                        mInterstitialAd.loadAd(AdRequest.Builder().build())
                    }
                }

            }

            override fun onException(exception: Exception) {
                Toast.makeText(applicationContext, getString(R.string.note_created_failed), Toast.LENGTH_LONG).show()

                Log.e(TAG, "Error creating note", exception)
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        if (clipboardManager != null) {
            clipboardManager.removePrimaryClipChangedListener(this)
        }
    }


    override fun onBind(intent: Intent): IBinder? {

        return null
    }
}
