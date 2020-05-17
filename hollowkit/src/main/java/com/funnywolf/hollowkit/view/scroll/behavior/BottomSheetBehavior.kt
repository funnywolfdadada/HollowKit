package com.funnywolf.hollowkit.view.scroll.behavior

import android.view.MotionEvent
import android.view.View
import android.widget.Space
import android.widget.FrameLayout.LayoutParams
import androidx.core.view.ViewCompat
import com.funnywolf.hollowkit.utils.isUnder

/**
 * 底部浮层的 [NestedScrollBehavior]
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/17
 */
class BottomSheetBehavior(
    contentView: View,
    private val initPosition: Int,
    private val minHeight: Int,
    private val midHeight: Int = minHeight
): NestedScrollBehavior {
    override val scrollVertical: Boolean = true
    override val prevView: View? = Space(contentView.context).also {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        lp.topMargin = minHeight
        it.layoutParams = lp
    }

    override val prevScrollTarget: NestedScrollTarget? = null
    override val midView: View = contentView
    override val midScrollTarget: NestedScrollTarget? = null
    override val nextView: View? = null
    override val nextScrollTarget: NestedScrollTarget? = null

    private var midScroll = 0
    private var firstLayout = true

    override fun afterLayout(layout: BehavioralNestedScrollLayout) {
        midScroll = layout.minScroll + midHeight - minHeight
        if (firstLayout) {
            firstLayout = false
            layout.scrollTo(
                layout.scrollX,
                when (initPosition) {
                    POSITION_MIN -> layout.minScroll
                    POSITION_MAX -> layout.maxScroll
                    else -> midScroll
                }
            )
        }
    }

    override fun handleDispatchTouchEvent(
        layout: BehavioralNestedScrollLayout,
        e: MotionEvent
    ): Boolean? {
        if ((e.action == MotionEvent.ACTION_CANCEL || e.action == MotionEvent.ACTION_UP)
            && layout.scrollY != 0
            && layout.lastScrollDir != 0) {
            layout.smoothScrollTo(
                if (layout.scrollY > midScroll) {
                    if (layout.lastScrollDir > 0) {
                        layout.maxScroll
                    } else {
                        midScroll
                    }
                } else {
                    if (layout.lastScrollDir > 0) {
                        midScroll
                    } else {
                        layout.minScroll
                    }
                }
            )
            return true
        }
        return super.handleDispatchTouchEvent(layout, e)
    }

    override fun handleInterceptTouchEvent(
        layout: BehavioralNestedScrollLayout,
        e: MotionEvent
    ): Boolean? {
        return if (prevView?.isUnder(e.rawX, e.rawY) != true) {
            null
        } else {
            false
        }
    }

    override fun handleTouchEvent(layout: BehavioralNestedScrollLayout, e: MotionEvent): Boolean? {
        return if (prevView?.isUnder(e.rawX, e.rawY) != true) {
            null
        } else {
            false
        }
    }

    override fun scrollSelfFirst(
        layout: BehavioralNestedScrollLayout,
        scroll: Int,
        @ViewCompat.NestedScrollType type: Int
    ): Boolean {
        return layout.scrollY != 0
    }

    override fun interceptScrollSelf(
        layout: BehavioralNestedScrollLayout,
        scroll: Int,
        @ViewCompat.NestedScrollType type: Int
    ): Boolean {
        return layout.isFling
    }

    companion object {
        const val POSITION_MIN = 1
        const val POSITION_MID = 2
        const val POSITION_MAX = 3
    }
}