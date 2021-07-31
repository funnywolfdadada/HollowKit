package com.funnywolf.hollowkit.view.scroll.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.ViewCompat

/**
 * 弹性布局
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/28
 */
class JellyLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BehavioralScrollView(context, attrs, defStyleAttr) {

    /**
     * 滚动阻尼，参数为当前的滚动量，返回值未阻尼系数
     */
    var resistance: ((JellyLayout, Int)->Float)? = null

    /**
     * 手指抬起时的回调
     */
    var onTouchRelease: ((JellyLayout)->Unit)? = null

    override fun handleDispatchTouchEvent(e: MotionEvent): Boolean? {
        if ((e.action == MotionEvent.ACTION_CANCEL || e.action == MotionEvent.ACTION_UP) && !inStablePosition()) {
            onTouchRelease?.invoke(this) ?: smoothScrollTo(0)
            return true
        }
        return super.handleDispatchTouchEvent(e)
    }

    override fun handleNestedPreScrollFirst(scroll: Int, type: Int): Boolean? {
        return if (inStablePosition() && isScrollChildTotalShowing()) {
            null
        } else {
            false
        }
    }

    override fun handleNestedScrollFirst(scroll: Int, type: Int): Boolean? {
        return true
    }

    override fun handleScrollSelf(scroll: Int, type: Int): Boolean? {
        return when (type) {
            ViewCompat.TYPE_NON_TOUCH -> false
            ViewCompat.TYPE_TOUCH -> {
                val s = (scroll * (resistance?.invoke(this, scroll) ?: 1F)).toInt()
                when (nestedScrollAxes) {
                    ViewCompat.SCROLL_AXIS_VERTICAL -> scrollBy(0, s)
                    ViewCompat.SCROLL_AXIS_HORIZONTAL -> scrollBy(s, 0)
                }
                true
            }
            else -> null
        }
    }

}
