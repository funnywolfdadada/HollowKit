package com.funnywolf.hollowkit.view.scroll.behavior

import android.content.Context
import android.util.AttributeSet
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
) : BehavioralScrollView(context, attrs, defStyleAttr) {

    var topScrollTarget: (()-> View?)? = null
    var bottomScrollTarget: (()-> View?)? = null

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        // 为了能够将滚动传递下去，需要把 fling 拦截下来
        fling(velocityY)
        return true
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
        return if (type == ViewCompat.TYPE_TOUCH) {
            handleDrag(scroll)
        } else {
            handleFling(scroll)
        }
    }

    private fun handleDrag(scroll: Int): Boolean? {
        scrollBy(0, if (scrollY > 0) { scroll } else { scroll / 2 })
        return true
    }

    private fun handleFling(scroll: Int): Boolean? {
        if (isScrollChildTotalShowing() && nestedScrollTarget?.canScrollVertically(scroll) == true) {
            nestedScrollTarget?.scrollBy(0, scroll)
            return true
        }
        // 自己可以滚动时，默认处理
        if (canScrollVertically(scroll)) {
            return null
        }
        // 自己无法滚动时根据方向确定滚动传递的目标
        val target = if (scroll < 0) {
            topScrollTarget?.invoke()
        } else {
            bottomScrollTarget?.invoke()
        }
        if (target != null && target.canScrollVertically(scroll)) {
            target.scrollBy(0, scroll)
            return true
        }
        return false
    }

}
