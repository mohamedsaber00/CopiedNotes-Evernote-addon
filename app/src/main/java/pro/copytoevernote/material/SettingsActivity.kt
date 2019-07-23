package pro.copytoevernote.material

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.evernote.client.android.EvernoteSession
import java.util.*
import io.mattcarroll.hover.overlay.OverlayPermission
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.Uri

import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsClient.getPackageName
import androidx.core.content.edit

import androidx.preference.ListPreference
import com.android.billingclient.api.*

import com.evernote.edam.type.Notebook
import com.evernote.client.android.asyncclient.EvernoteCallback
import com.evernote.client.android.login.EvernoteLoginFragment
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.fragment_sign_in.view.*


class SettingsActivity : AppCompatActivity() {

    private val REQUEST_CODE_HOVER_PERMISSION = 1000

    private var mPermissionsRequested = false

    private val EVERNOTE_SERVICE = EvernoteSession.EvernoteService.PRODUCTION
    lateinit var mEvernoteSession: EvernoteSession
    lateinit var signinDialog: AlertDialog

    val CONSUMER_KEY = "mohamedsaber09-2230"
    val CONSUMER_SECRET = "8866090b487c8bc6"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

        MobileAds.initialize(this, getString(R.string.ADMOB_APP_ID));

        mEvernoteSession = EvernoteSession.Builder(this)
            .setEvernoteService(EVERNOTE_SERVICE).setSupportAppLinkedNotebooks(true)
            .build(CONSUMER_KEY, CONSUMER_SECRET).asSingleton()

        if (!EvernoteSession.getInstance().isLoggedIn) {


            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.you_need_to_signin)
            builder.setPositiveButton(R.string.sign_in, DialogInterface.OnClickListener { dialog, which ->
                mEvernoteSession.authenticate(this)
            })
            builder.setCancelable(false)

            val signinDialog = builder.create().show()

            return
        }


    }

    override fun onResume() {
        super.onResume()

        // On Android M and above we need to ask the user for permission to display the Hover
        // menu within the "alert window" layer.  Use OverlayPermission to check for the permission
        // and to request it.
        if (!mPermissionsRequested && !OverlayPermission.hasRuntimePermissionToDrawOverlay(this)) {
            val myIntent = OverlayPermission.createIntentToRequestOverlayPermission(this)
            startActivityForResult(myIntent, REQUEST_CODE_HOVER_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (REQUEST_CODE_HOVER_PERMISSION == requestCode) {
            mPermissionsRequested = true
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

        if (EvernoteSession.REQUEST_CODE_LOGIN == requestCode)
            if (resultCode == Activity.RESULT_OK) {
                signinDialog.dismiss()

            } else {
                // handle failure
            }

    }


    class SettingsFragment : PreferenceFragmentCompat(), PurchasesUpdatedListener {

        val TAG = "Setting fragment"

        private lateinit var billingClient: BillingClient

        var mainSwitchSharedPreferences: SwitchPreferenceCompat? = null
        var notebooksListPreference: ListPreference? = null
        var confirmMethodListPreference: ListPreference? = null
        var buyPreference: Preference? = null
        var feedbackPreference: Preference? = null
        var ratePreference: Preference? = null


        lateinit var sharedPreferences: SharedPreferences

        lateinit var skuDetails: SkuDetails


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            sharedPreferences = activity!!.getSharedPreferences("main", Context.MODE_PRIVATE)

            mainSwitchSharedPreferences = findPreference<Preference>("service_is_on") as SwitchPreferenceCompat?
            notebooksListPreference = findPreference<Preference>("choose_notebook") as ListPreference
            confirmMethodListPreference = findPreference<Preference>("confirm_method") as ListPreference
            buyPreference = findPreference<Preference>("buy_full") as Preference


            ratePreference = findPreference<Preference>("rate") as Preference
            feedbackPreference = findPreference<Preference>("feedback") as Preference

            feedbackPreference!!.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {
                    val emailIntent = Intent(
                        Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "mohamedsaber09@@gmail.com", null
                        )
                    );
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Copy to Evernote");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));

                    return true
                }
            }

            ratePreference!!.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {
                    val appPackageName = context!!.getPackageName()
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                            )
                        )
                    }
                    return true
                }
            }







            mainSwitchSharedPreferences!!.isChecked = isServiceRunning(MyService::class.java)
            setInAppBilling()
            getNotebookList()
            ifFullVersion(sharedPreferences.getBoolean("full_version", false))





            mainSwitchSharedPreferences!!.onPreferenceChangeListener =
                object : Preference.OnPreferenceChangeListener {
                    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                        turnServiceChanged(newValue as Boolean)

                        return true
                    }
                }

            notebooksListPreference!!.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {
                    getNotebookList()
                    return true

                }
            }



            buyPreference!!.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {

                    if (skuDetails != null) {
                        val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build()
                        val responseCode = billingClient.launchBillingFlow(activity, flowParams)
                    }

                    return true

                }
            }


        }

        fun turnServiceChanged(isON: Boolean) {
            Log.d(TAG, "service statue changed")
            val myServiceIntent = Intent(activity, MyService::class.java)


            if (isON) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context!!.startForegroundService(myServiceIntent)
                } else {
                    context!!.startService(myServiceIntent)
                }

            } else context!!.stopService(myServiceIntent)

        }


        fun getNotebookList() {
            if (!EvernoteSession.getInstance().isLoggedIn) {
                return
            }

            val noteStoreClient = EvernoteSession.getInstance().evernoteClientFactory.noteStoreClient
            noteStoreClient.listNotebooksAsync(object : EvernoteCallback<List<Notebook>> {
                override fun onSuccess(result: List<Notebook>) {
                    val namesList = ArrayList<CharSequence>(result.size)
                    val idList = ArrayList<CharSequence>(result.size)

                    for (notebook in result) {
                        namesList.add(notebook.name)
                        idList.add(notebook.guid)
                    }
                    val nameListasCS = namesList.toArray(arrayOfNulls<CharSequence>(namesList.size))
                    val idListasCS = idList.toArray(arrayOfNulls<CharSequence>(idList.size))

                    notebooksListPreference!!.entries = nameListasCS
                    notebooksListPreference!!.entryValues = idListasCS

                }

                override fun onException(exception: Exception) {
                    Log.e(TAG, "Error retrieving notebooks", exception)
                }
            })
        }

        private fun setInAppBilling() {

            billingClient = BillingClient.newBuilder(context!!).enablePendingPurchases().setListener(this).build()
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    Log.d(TAG, "Billing setup failed")

                }

                override fun onBillingSetupFinished(billingResult: BillingResult?) {
                    if (billingResult!!.responseCode == BillingClient.BillingResponseCode.OK) {

                        val params = SkuDetailsParams.newBuilder()
                        params.setSkusList(listOf("full_version","full_version")).setType(BillingClient.SkuType.INAPP)
                        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                            skuDetails = skuDetailsList.get(0)

                        }
                    }
                }

            })

        }


        override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {

            if (billingResult!!.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                sharedPreferences.edit {
                    putBoolean("full_version", true)
                }
                ifFullVersion(true)

            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                Toast.makeText(context, "User cancelled", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(context, "Error, Purchase didn't compelete", Toast.LENGTH_SHORT)
            }
        }


        fun ifFullVersion(isFull: Boolean) {
            if (sharedPreferences.getBoolean("full_version", false)) {

                buyPreference!!.isEnabled = false
                confirmMethodListPreference!!.isSelectable = true
                confirmMethodListPreference!!.summary = "%s"
            } else {

                buyPreference!!.isEnabled = true
                confirmMethodListPreference!!.isSelectable = false
                confirmMethodListPreference!!.summary = getString(R.string.you_need_to_buy)

            }

        }

        private fun isServiceRunning(serviceClass: Class<*>): Boolean {
            val manager = activity!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            for (service in manager!!.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }

    }


}