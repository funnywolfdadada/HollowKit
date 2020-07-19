package com.funnywolf.hollowkit.view.scroll.behavior

import android.view.MotionEvent
import android.view.View

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/7/19
 */
class CommonBehavior(
    override val scrollAxis: Int,
    override val prevView: View?,
    override val midView: View,
    override val nextView: View?
) : NestedScrollBehavior {

    var afterLayout: ((BehavioralScrollView)->Unit)? = null

    var handleDispatchTouchEvent: ((BehavioralScrollView, MotionEvent)->Boolean)? = null

    var handleTouchEvent: ((BehavioralScrollView, MotionEvent)->Boolean)? = null

    var handleNestedPreScrollFirst: NestedScrollHandler? = null

    var handleNestedScrollFirst: NestedScrollHandler? = null

    var handleScrollSelf: NestedScrollHandler? = null

    override fun afterLayout(v: BehavioralScrollView) {
        afterLayout?.invoke(v)
    }

    override fun handleDispatchTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? {
        return handleDispatchTouchEvent?.invoke(v, e)
    }

    override fun handleTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? {
        return handleTouchEvent?.invoke(v, e)
    }

    override fun handleNestedPreScrollFirst(
        v: BehavioralScrollView,
        scroll: Int,
        type: Int
    ): Boolean? {
        return handleNestedPreScrollFirst?.invoke(v, scroll, type)
    }

    override fun handleNestedScrollFirst(
        v: BehavioralScrollView,
        scroll: Int,
        type: Int
    ): Boolean? {
        return handleNestedScrollFirst?.invoke(v, scroll, type)
    }

    override fun handleScrollSelf(v: BehavioralScrollView, scroll: Int, type: Int): Boolean? {
        return handleScrollSelf?.invoke(v, scroll, type)
    }

}

/**
 * 参数分别是 BehavioralScrollView，滚动量和滚动类型
 */
typealias NestedScrollHandler = (BehavioralScrollView, Int, Int)->Boolean?

inline fun BehavioralScrollView.commonBehavior(
    scrollAxis: Int,
    prevView: View?,
    midView: View,
    nextView: View?,
    init: CommonBehavior.()->Unit
) {
    setupBehavior(CommonBehavior(scrollAxis, prevView, midView, nextView).apply(init))
}
