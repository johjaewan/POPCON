package com.ssafy.popcon.ui.popup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ssafy.popcon.dto.Gifticon

class PreviewAdapter(
    fm: FragmentManager,
    val sidePreviewCount: Int,
    val gifticons: List<Gifticon>,
    private val syncToViewPager: ViewPager,
    private val syncWithViewPager: ViewPager
) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    constructor(
        fm: FragmentManager,
        gifticons: List<Gifticon>,
        syncToViewPager: ViewPager,
        syncWithViewPager: ViewPager
    ) : this(
        fm, DEFAULT_SIDE_PREVIEW_COUNT, gifticons, syncToViewPager, syncWithViewPager
    )

    override fun getItem(position: Int): Fragment {
        return if (isDummy(position)) {
            EmptyPreviewFragment()
        } else {
            GifticonPreviewFragment.newInstance(
                gifticons[getRealPosition(position)],
                position,
                object : PreviewListener {
                    override fun onClick(position: Int) {
                        syncToViewPager.setCurrentItem(getRealPosition(position), true)
                        syncWithViewPager.setCurrentItem(getRealPosition(position), true)
                    }

                    override fun onSelect(position: Int) {

                    }
                })
        }
    }

    private fun isDummy(position: Int): Boolean {
        return position < sidePreviewCount || position > gifticons.size - 1 + sidePreviewCount
    }

    fun getRealPosition(position: Int): Int {
        return position - sidePreviewCount
    }

    override fun getCount(): Int {
        return gifticons.size + sidePreviewCount * 2
    }

    override fun getPageWidth(position: Int): Float {
        return 1.0f / elementsPerPage
    }

    private val elementsPerPage: Int
        get() = sidePreviewCount * 2 + 1

    companion object {
        private const val DEFAULT_SIDE_PREVIEW_COUNT = 2
    }

    interface PreviewListener {
        fun onClick(position: Int)
        fun onSelect(position: Int)
    }
}

