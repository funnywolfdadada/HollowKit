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
    /**
     * 浮层的内容视图
     */
    contentView: View,
    /**
     * 初始位置，最低高度 [POSITION_MIN]、中间高度 [POSITION_MID] 或最大高度 [POSITION_MAX]
     */
    private val initPosition: Int,
    /**
     * 内容视图的最低显示高度
     */
    private val minHeight: Int,
    /**
     * 内容视图中间停留的显示高度，默认等于最低高度
     */
    private val midHeight: Int = minHeight
): NestedScrollBehavior {
    override val scrollAxis: Int = ViewCompat.SCROLL_AXIS_VERTICAL

    /**
     * 用于控制滚动范围
     */
    override val prevView: View? = Space(contentView.context).also {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        lp.topMargin = minHeight
        it.layoutParams = lp
    }

    override val midView: View = contentView
    override val nextView: View? = null

    /**
     * 中间高度 [POSITION_MID] 时 scrollY 的值
     */
    private var midScroll = 0
    private var firstLayout = true

    override fun afterLayout(v: BehavioralScrollView) {
        // 计算中间高度时的 scrollY
        midScroll = v.minScroll + midHeight - minHeight
        // 第一次 layout 滚动到初始位置
        if (firstLayout) {
            firstLayout = false
            v.scrollTo(
                v.scrollX,
                when (initPosition) {
                    POSITION_MIN -> v.minScroll
                    POSITION_MAX -> v.maxScroll
                    else -> midScroll
                }
            )
        }
    }

    override fun handleDispatchTouchEvent(
        v: BehavioralScrollView,
        e: MotionEvent
    ): Boolean? {
        if ((e.action == MotionEvent.ACTION_CANCEL || e.action == MotionEvent.ACTION_UP)
            && v.scrollY != 0) {
            // 在 up 或 cancel 时，根据当前滚动位置和上次滚动的方向，决定动画的目标位置
            v.smoothScrollTo(
                if (v.scrollY > midScroll) {
                    if (v.lastScrollDir > 0) {
                        v.maxScroll
                    } else {
                        midScroll
                    }
                } else {
                    if (v.lastScrollDir > 0) {
                        midScroll
                    } else {
                        v.minScroll
                    }
                }
            )
            return true
        }
        return super.handleDispatchTouchEvent(v, e)
    }

    override fun handleTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? {
        // down 事件触点在 prevView 上时不做处理
        return if (e.action == MotionEvent.ACTION_DOWN && prevView?.isUnder(e.rawX, e.rawY) == true) {
            false
        } else {
            null
        }
    }

    override fun handleNestedPreScrollFirst(
        v: BehavioralScrollView,
        scroll: Int,
        @ViewCompat.NestedScrollType type: Int
    ): Boolean? {
        // 只要 contentView 没有完全展开，就在子 View 滚动前处理
        return if (v.scrollY != 0) {
            true
        } else {
            null
        }
    }

    override fun handleNestedScrollFirst(
        v: BehavioralScrollView,
        scroll: Int,
        type: Int
    ): Boolean? {
        return true
    }

    override fun handleScrollSelf(
        v: BehavioralScrollView,
        scroll: Int,
        @ViewCompat.NestedScrollType type: Int
    ): Boolean? {
        // 只允许 touch 类型用于自身的滚动
        return if (type == ViewCompat.TYPE_NON_TOUCH) {
            true
        } else {
            null
        }
    }

    companion object {
        const val POSITION_MIN = 1
        const val POSITION_MID = 2
        const val POSITION_MAX = 3
    }
}