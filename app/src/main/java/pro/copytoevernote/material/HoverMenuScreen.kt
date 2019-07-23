package pro.copytoevernote.material

import android.content.Context
import io.mattcarroll.hover.Content
import android.view.Gravity
import android.view.View
import android.widget.TextView



class HoverMenuScreen(context: Context, s: String) : Content {


    private  var mContext: Context
    private  var mPageTitle: String
    private  var mWholeScreen: View

    init {
        mContext = context.getApplicationContext()
        mPageTitle = s
        mWholeScreen = createScreenView()
    }

    private fun createScreenView(): View {
        val wholeScreen = TextView(mContext)
        wholeScreen.text = "Screen: $mPageTitle"
        wholeScreen.gravity = Gravity.CENTER
        return wholeScreen
    }

    // Make sure that this method returns the SAME View.  It should NOT create a new View each time
    // that it is invoked.
    override fun getView(): View {
        return mWholeScreen
    }

    override fun isFullscreen(): Boolean {
        return true
    }

    override fun onShown() {
        // No-op.
    }

    override fun onHidden() {
        // No-op.
    }
}