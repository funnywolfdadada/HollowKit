package com.funnywolf.hollowkit.view.scroll.behavior

import android.view.MotionEvent
import android.view.View
import android.widget.Space
import androidx.core.view.ViewCompat

/**
 * 带折叠滑动头部的 behavior
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/23
 */
class CollapsingHeaderBehavior(
    val contentView: View,
    val headerView: View,
    val enableOverScroll: Boolean = true
): NestedScrollBehavior {

    override val scrollVertical: Boolean = true
    override val prevView: View? = Space(headerView.context)
    override val prevScrollTarget: NestedScrollTarget? = null
    override val midView: View = headerView
    override val midScrollTarget: NestedScrollTarget? = null
    override val nextView: View? = contentView
    override val nextScrollTarget: NestedScrollTarget? = null

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
        if (type == ViewCompat.TYPE_TOUCH && v.scrollY < 0) {
            if (enableOverScroll) {
                val height = headerView.height
                val s = if (height > 0) {
                    scroll * (v.scrollY + height) / height
                } else {
                    0
                }
                v.scrollBy(0, s)
            }
            return true
        }
        return false
    }

}