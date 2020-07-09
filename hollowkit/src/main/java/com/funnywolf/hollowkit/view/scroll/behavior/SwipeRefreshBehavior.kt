package com.funnywolf.hollowkit.view.scroll.behavior

import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.funnywolf.hollowkit.utils.dp
import kotlin.math.abs
import kotlin.math.min

/**
 * 不发生位移的下拉刷新
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/7/9
 */
class SwipeRefreshBehavior(
    contentView: View,
    private var refreshListener: ((SwipeRefreshBehavior)->Unit)? = null
): NestedScrollBehavior {

    private val refreshHeight = 72.dp

    var enable: Boolean = true
        set(value) {
            field = value
            if (!value) {
                isRefreshing = false
            }
        }

    private var animating = false

    var isRefreshing: Boolean = false
        set(value) {
            if (value && value != field) {
                refreshListener?.invoke(this)
            }
            field = value
            val targetTransY = if (value) { refreshHeight.toFloat() } else { 0F }
            if (refreshView.translationY != targetTransY) {
                animating = true
                refreshView.animate().translationY(targetTransY)
                    .withEndAction { animating = false }
                    .start()
            }
            if (value) {
                refreshView.loadingView.alpha = 1F
                refreshView.animator.start()
            } else {
                refreshView.animator.cancel()
            }
        }

    val refreshView = RefreshView(contentView.context).apply {
        elevation = contentView.elevation + 1F
        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, refreshHeight)
        loadingView.layoutParams?.also { lp ->
            lp.height = refreshHeight / 2
            lp.width = refreshHeight / 2
        }
    }

    private var process: Float = 0F
        set(value) {
            if (isRefreshing) {
                return
            }
            field = value
            refreshView.animator.cancel()
            refreshView.loadingView.alpha = process * 3
            refreshView.loadingView.rotation = process * 3 * 360
        }

    override val scrollAxis: Int = ViewCompat.SCROLL_AXIS_VERTICAL
    override val prevView: View? = refreshView
    override val midView: View = contentView
    override val nextView: View? = null

    override fun handleDispatchTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? {
        return when {
            !enable -> null
            // 抬手时，如果头部已经滚出来了，且未刷新，则根据滚出的距离设置刷新状态
            (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_CANCEL)
                    && refreshView.translationY != 0F
                    && !isRefreshing -> {
                isRefreshing = abs(refreshView.translationY) >= refreshHeight
                true
            }
            else -> null
        }
    }

    override fun handleNestedPreScrollFirst(v: BehavioralScrollView, scroll: Int, type: Int): Boolean? {
        val handle = when {
            !enable -> null
            animating -> null
            refreshView.translationY > 0F -> true
            else -> null
        }
        v.log("handleNestedPreScrollFirst $handle, state = ${v.state}, type = $type, isRefreshing = $isRefreshing")
        return handle
    }

    override fun handleNestedScrollFirst(
        v: BehavioralScrollView,
        scroll: Int,
        type: Int
    ): Boolean? {
        return if (animating) {
            null
        } else {
            false
        }
    }

    override fun handleScrollSelf(v: BehavioralScrollView, scroll: Int, type: Int): Boolean? {
        val handle = when {
            !enable -> false
            isRefreshing -> false
            animating -> false
            type == ViewCompat.TYPE_NON_TOUCH -> false
            type == ViewCompat.TYPE_TOUCH -> {
                refreshView.translationY = min(refreshView.translationY - scroll.toFloat(), refreshHeight * 2F)
                process = refreshView.translationY / refreshHeight
                true
            }
            else -> false
        }
        v.log("handleScrollSelf $handle, state = ${v.state}, type = $type, isRefreshing = $isRefreshing")
        return handle
    }

}