package pro.copytoevernote.material

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.Image
import android.os.Build
import android.os.Bundle

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.evernote.client.android.login.EvernoteLoginFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment1_intro.*
import kotlinx.android.synthetic.main.fragment2_intro.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.w3c.dom.Text
import pro.copytoevernote.material.Intro.IntroFragment1
import pro.copytoevernote.material.Intro.IntroFragment2
import pro.copytoevernote.material.Intro.IntroPageAdapter
import java.util.ArrayList
import android.view.WindowManager
import android.widget.Toast


class MainActivity : AppCompatActivity(), EvernoteLoginFragment.ResultCallback {

    lateinit var sharedPreferences: SharedPreferences


    lateinit var pager: ViewPager
    lateinit var adap: IntroPageAdapter

    var notificationImageView: ImageView? = null
    var notificationTextView: TextView? = null
    var bubbleImageView: ImageView? = null
    var bubbleTextView: TextView? = null

    var firstScreenShoteImageView: ImageView? = null
    var howItWorkTextView: TextView? = null
    var copyAnythingTextView: TextView? = null

    var signInButton: MaterialButton? = null
    var logoImageView: ImageView? = null
    var justOneMoreTextView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkIfFirstTime()
        setContentView(R.layout.activity_main)
        pager = findViewById<View>(R.id.intro_pager) as ViewPager

        adap = IntroPageAdapter(supportFragmentManager)
        adap.addFrag(IntroFragment1.newInstance("", ""))
        adap.addFrag(IntroFragment2.newInstance("", ""))
        adap.addFrag(SignInFragment.newInstance("", ""))

        pager.adapter = adap
        pager.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        pager.offscreenPageLimit = 4


        val colors = arrayOf(
            ContextCompat.getColor(this, R.color.frag1),
            ContextCompat.getColor(this, R.color.frag2),
            ContextCompat.getColor(this, R.color.frag3)
        )
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                if (position < adap.count - 1 && position < colors.size - 1) {
                    val window = getWindow()


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // clear FLAG_TRANSLUCENT_STATUS flag:
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                        // finally change the color
                        window.statusBarColor = ArgbEvaluator().evaluate(
                            positionOffset,
                            colors[position],
                            colors[position + 1]
                        ) as Int
                    }


                    pager.setBackgroundColor(
                        ArgbEvaluator().evaluate(
                            positionOffset,
                            colors[position],
                            colors[position + 1]
                        ) as Int
                    )

                } else {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


                        window.statusBarColor = colors[colors.size - 1]
                    }

                    pager.setBackgroundColor(colors[colors.size - 1])

                }

            }


            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        pager.setPageTransformer(true) { view, position ->

            var mPosition = position

            val pageWidth = view.width
            val pageHeight = view.height
            val ratio = pageWidth.toFloat() / pageHeight


            notificationImageView = view.findViewById<View>(R.id.notification_intro_imageView) as ImageView?
            notificationTextView = view.findViewById(R.id.notification_intro_textView) as TextView?
            bubbleImageView = view.findViewById<View>(R.id.bubble_intro_imageView) as ImageView?
            bubbleTextView = view.findViewById<View>(R.id.bubble_intro_textView) as TextView?

            firstScreenShoteImageView = view.findViewById<View>(R.id.first_screenshot_imageview) as ImageView?
            howItWorkTextView = view.findViewById(R.id.how_it_work_textview) as TextView?
            copyAnythingTextView = view.findViewById<View>(R.id.copy_anything_textView) as TextView?


            signInButton = view.findViewById(R.id.sign_in_button) as MaterialButton?
            logoImageView = view.findViewById(R.id.logo_imageView) as ImageView?
            justOneMoreTextView = view.findViewById(R.id.just_one_more_textview) as TextView?


            if (position in 0.0..1.0) {


                notificationImageView?.setTranslationY(-(pageHeight * (1 - mPosition) / 1.3f * ratio))
                notificationTextView?.setTranslationY(-(pageHeight * (1 - mPosition) / 2.2f * ratio))

                bubbleImageView?.setTranslationY(pageHeight * (1 - mPosition) / 8 * ratio)
                bubbleTextView?.setTranslationY(pageHeight * (1 - mPosition) / 3 * ratio)



                logoImageView?.translationX = -pageWidth * (1 - position) / 5.5f
                logoImageView?.translationY = pageWidth * (1 - position) / 5

                justOneMoreTextView?.translationX = -pageWidth * (1 - position) / 5f

                signInButton?.translationX = -pageWidth * (1 - position) / 2.9f
                signInButton?.translationY = -pageWidth * (1 - position) / 2f


            } else if (position in -1.0..-0.0) {


                var negativePosition = -position
                notificationImageView?.setTranslationY(-(pageHeight * (1 - negativePosition) / 1.3f * ratio))
                notificationTextView?.setTranslationY(-(pageHeight * (1 - negativePosition) / 2.2f * ratio))

                bubbleImageView?.setTranslationY(pageHeight * (1 - negativePosition) / 8 * ratio)
                bubbleTextView?.setTranslationY(pageHeight * (1 - negativePosition) / 3 * ratio)





                firstScreenShoteImageView?.translationY = pageWidth * position / 2
                howItWorkTextView?.translationY = pageWidth * position / 3
                copyAnythingTextView?.translationY = pageWidth * position / 4

                firstScreenShoteImageView?.translationX = pageWidth * position / 2
                howItWorkTextView?.translationX = pageWidth * position / 3
                copyAnythingTextView?.translationX = pageWidth * position / 4


            }
        }


    }

    fun checkIfFirstTime() {
        sharedPreferences = getSharedPreferences("main", Context.MODE_PRIVATE)

        if (!sharedPreferences.getBoolean("isFirstTime", true)) {
            Log.d("SigninActivity", "Already Signed in")
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onLoginFinished(successful: Boolean) {

        if (successful) {
            sharedPreferences.edit {
                putBoolean("isFirstTime", false)
            }

            Log.d("SigninActivity", "Signed in succesfully")
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)

        } else {
            Toast.makeText(this,"Login failed",Toast.LENGTH_LONG).show()

        }
    }


    inner class pagerAdap(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        internal var fragments = ArrayList<Fragment>()

        internal fun addFrag(fragment: Fragment) {
            fragments.add(fragment)
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }

    companion object {

        private val TAG = "TAG"
    }


}
