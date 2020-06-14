package com.funnywolf.hollowkit.view.scroll.behavior

import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import kotlin.math.abs

/**
 * 弹性滚动
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/13
 */
class JellyBehavior(
    /**
     * 当前的可滚动方向
     */
    override val scrollVertical: Boolean,
    /**
     * 内容视图
     */
    override val midView: View,
    /**
     * 内容的前一个视图，水平方向在左边，垂直方向在上边
     */
    override val prevView: View? = null,
    /**
     * 内容的后一个视图，水平方向在右边，垂直方向在下边
     */
    override val nextView: View? = null
) : NestedScrollBehavior {

    override val prevScrollTarget: NestedScrollTarget? = null
    override val midScrollTarget: NestedScrollTarget? = null
    override val nextScrollTarget: NestedScrollTarget? = null

    private fun selfScrolled(v: BehavioralScrollView) = if (scrollVertical) {
        v.scrollY != 0
    } else {
        v.scrollX != 0
    }

    override fun handleDispatchTouchEvent(
        v: BehavioralScrollView,
        e: MotionEvent
    ): Boolean? {
        if (selfScrolled(v) && (e.action == MotionEvent.ACTION_CANCEL || e.action == MotionEvent.ACTION_UP)) {
            v.smoothScrollTo(0)
            return true
        }
        return super.handleDispatchTouchEvent(v, e)
    }

    override fun shouldTargetScroll(v: BehavioralScrollView, scroll: Int, type: Int): Boolean {
        return !selfScrolled(v)
    }

    override fun handleScrollSelf(v: BehavioralScrollView, scroll: Int, type: Int): Boolean {
        val p = abs(v.currProcess())
        val s = (scroll * (1 - p)).toInt()
        return when {
            v.isFling -> {
                if (abs(s) < 10 || p > 0.1) {
                    v.smoothScrollTo(0)
                } else {
                    v.scrollBy(s, s)
                }
                true
            }
            type == ViewCompat.TYPE_TOUCH -> {
                v.scrollBy(s, s)
                true
            }
            else -> false
        }
    }

}
