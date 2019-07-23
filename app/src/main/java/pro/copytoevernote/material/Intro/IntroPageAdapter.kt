package pro.copytoevernote.material.Intro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import java.util.ArrayList

class IntroPageAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(
    fragmentManager) {

    var fragments = ArrayList<Fragment>()

    fun addFrag(fragment: Fragment) {
        fragments.add(fragment)
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}