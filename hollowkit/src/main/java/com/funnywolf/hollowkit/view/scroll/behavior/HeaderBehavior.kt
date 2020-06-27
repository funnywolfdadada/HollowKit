package com.funnywolf.hollowkit.view.scroll.behavior

import android.view.MotionEvent
import android.view.View
import android.widget.Space
import androidx.core.view.ViewCompat

/**
 * 带滑动头部的 behavior
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/23
 */
class HeaderBehavior(
    contentView: View,
    headerView: View
): NestedScrollBehavior {

    override val scrollVertical: Boolean = true
    override val prevView: View? = Space(headerView.context)
    override val prevScrollTarget: NestedScrollTarget? = null
    override val midView: View = headerView
    override val midScrollTarget: NestedScrollTarget? = null
    override val nextView: View? = contentView
    override val nextScrollTarget: NestedScrollTarget? = null

    private val onScrollChanged: (BehavioralScrollView)->Unit = {
        val height = headerView.height.toFloat()
        val scale = if (it.scrollY < 0) {
            1F - it.scrollY / height
        } else {
            1F
        }
        headerView.scaleX = scale
        headerView.scaleY = scale
        headerView.pivotY = height
    }

    override fun afterLayout(v: BehavioralScrollView) {
        super.afterLayout(v)
        v.onScrollChangedListener = onScrollChanged
    }

    override fun handleDispatchTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? {
        if (e.action == MotionEvent.ACTION_UP && v.scrollY < 0) {
            v.smoothScrollTo(0)
            return true
        }
        return super.handleDispatchTouchEvent(v, e)
    }

    override fun scrollSelfFirst(v: BehavioralScrollView, scroll: Int, type: Int): Boolean {
        return scroll > 0
    }

    override fun handleScrollSelf(v: BehavioralScrollView, scroll: Int, type: Int): Boolean {
        if (type == ViewCompat.TYPE_NON_TOUCH && v.state == NestedScrollState.FLING) {
            return v.scrollY <= 0 && scroll < 0
        }
        return false
    }

}