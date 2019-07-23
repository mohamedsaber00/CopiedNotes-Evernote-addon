package pro.copytoevernote.material

import io.mattcarroll.hover.HoverMenu

import android.widget.ImageView.ScaleType
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.annotation.Nullable
import io.mattcarroll.hover.Content
import java.util.*


class SingleSectionHoverMenu(applicationContext: Context) : HoverMenu() {

    private  var mContext: Context
    private  var mSection: Section

     init {


        mContext = applicationContext

        mSection = Section(
            SectionId("1"),
            createTabView(),
            createScreen()
        )
    }


    private fun createTabView(): View {
        val imageView = ImageView(mContext)
        imageView.setImageResource(R.drawable.logo_rounded)
        imageView.setScaleType(ScaleType.CENTER_INSIDE)
        return imageView
    }

    private fun createScreen(): Content {
        return  HoverMenuScreen(mContext,"Screen 1")
    }

    override fun getId(): String {
        return "singlesectionmenu"
    }

    override fun getSectionCount(): Int {
        return 1
    }

    @Nullable
    override fun getSection(index: Int): Section? {
        return if (0 == index) {
            mSection
        } else {
            null
        }
    }

    @Nullable
    override fun getSection(sectionId: SectionId): Section? {
        return if (sectionId == mSection.id) {
            mSection
        } else {
            null
        }
    }

    override fun getSections(): List<Section> {
        return Collections.singletonList(mSection)
    }

}