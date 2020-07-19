package com.funnywolf.hollowkit.view.scroll.behavior

import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat


/**
 * 联动滚动
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/7/12
 */
class LinkedScrollBehavior(
    topContent: View,
    bottomContent: View,
    private val topScrollTarget: (()->View?)? = null,
    private val bottomScrollTarget: (()->View?)? = null
): NestedScrollBehavior {

    override val scrollAxis: Int = ViewCompat.SCROLL_AXIS_VERTICAL
    override val prevView: View? = null
    override val midView: View = topContent
    override val nextView: View? = bottomContent

    private fun isChildTotalShowing(v: BehavioralScrollView): Boolean {
        val c = v.nestedScrollChild
        return c == null || (c.y - v.scrollY >= 0 && c.y + c.height - v.scrollY <= v.height)
    }

    override fun handleDispatchTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? {
        // down 时需要把之前可能的 fling 停掉，这里通过分发一个 down 事件解决
        if (e.action == MotionEvent.ACTION_DOWN) {
            midView.dispatchTouchEvent(e)
            nextView?.dispatchTouchEvent(e)
        }
        return super.handleDispatchTouchEvent(v, e)
    }

    override fun handleNestedPreScrollFirst(
        v: BehavioralScrollView,
        scroll: Int,
        type: Int
    ): Boolean? {
        return if (isChildTotalShowing(v)) {
            null
        } else {
            false
        }
    }

    override fun handleNestedScrollFirst(
        v: BehavioralScrollView,
        scroll: Int,
        type: Int
    ): Boolean? {
        return true
    }

    override fun handleScrollSelf(v: BehavioralScrollView, scroll: Int, type: Int): Boolean? {
        if (type != ViewCompat.TYPE_NON_TOUCH || v.canScrollVertically(scroll)) {
            return null
        }
        val target = if (scroll < 0) {
            topScrollTarget?.invoke()
        } else {
            bottomScrollTarget?.invoke()
        }
        return if (target != null && target.canScrollVertically(scroll)) {
            target.scrollBy(0, scroll)
            true
        } else {
            false
        }
    }
}