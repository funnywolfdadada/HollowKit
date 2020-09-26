package com.funnywolf.hollowkit.view.scroll.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat

/**
 * 联动滚动布局
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/26
 */
class LinkageScrollLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BehavioralScrollView(context, attrs, defStyleAttr), NestedScrollBehavior {

    var topScrollTarget: (()-> View?)? = null
    var bottomScrollTarget: (()-> View?)? = null

    override var behavior: NestedScrollBehavior? = this

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        prevView = null
        midView = getChildAt(0)
        nextView = getChildAt(1)
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        // 为了能够将滚动传递下去，需要把 fling 拦截下来
        fling(velocityX, velocityY)
        return true
    }

    override fun handleDispatchTouchEvent(e: MotionEvent): Boolean? {
        // down 时需要把之前可能的 fling 停掉，这里通过分发一个 down 事件解决
        if (e.action == MotionEvent.ACTION_DOWN) {
            prevView?.dispatchTouchEvent(e)
            midView?.dispatchTouchEvent(e)
            nextView?.dispatchTouchEvent(e)
        }
        return super.handleDispatchTouchEvent(e)
    }

    override fun handleNestedPreScrollFirst(scroll: Int, type: Int): Boolean? {
        return if (isScrollChildTotalShowing()) {
            null
        } else {
            false
        }
    }

    override fun handleNestedScrollFirst(scroll: Int, type: Int): Boolean? {
        return true
    }

    override fun handleScrollSelf(scroll: Int, type: Int): Boolean? {
        // 拦截了 fling 后，就需要在自己滚动时进行分发
        // fling 的滚动优先分发给触发嵌套滚动的 nestedScrollTarget
        if (type == ViewCompat.TYPE_NON_TOUCH
            && isScrollChildTotalShowing()
            && nestedScrollTarget?.canScrollVertically(scroll) == true) {
            nestedScrollTarget?.scrollBy(0, scroll)
            return true
        }
        // 自己可以滚动时，默认处理
        if (type != ViewCompat.TYPE_NON_TOUCH || canScrollVertically(scroll)) {
            return null
        }
        // 自己无法滚动时根据方向确定滚动传递的目标
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
