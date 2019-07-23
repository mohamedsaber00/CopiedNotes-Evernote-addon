package pro.copytoevernote.material


import android.app.Activity
import android.app.LauncherActivity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.evernote.client.android.EvernoteSession
import com.evernote.client.android.login.EvernoteLoginFragment
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_in.view.*

import pro.copytoevernote.material.Intro.IntroFragment1


/**
 * A simple [Fragment] subclass.
 */
class SignInFragment : Fragment(), EvernoteLoginFragment.ResultCallback {


    val CONSUMER_KEY = "mohamedsaber09-2230"
    val CONSUMER_SECRET = "8866090b487c8bc6"


    private val EVERNOTE_SERVICE = EvernoteSession.EvernoteService.PRODUCTION

    lateinit var mEvernoteSession : EvernoteSession

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)


         mEvernoteSession = EvernoteSession.Builder(activity)
            .setEvernoteService(EVERNOTE_SERVICE).setSupportAppLinkedNotebooks(true)
            .build(CONSUMER_KEY, CONSUMER_SECRET).asSingleton()

        view.sign_in_button.setOnClickListener {
            mEvernoteSession.authenticate(activity);
        }


        return view

    }

    override fun onLoginFinished(successful: Boolean) {

    }



    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignInFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

}
