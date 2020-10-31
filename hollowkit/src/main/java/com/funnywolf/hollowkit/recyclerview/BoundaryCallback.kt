package com.funnywolf.hollowkit.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.funnywolf.hollowkit.R


/**
 * 用于监听 RecyclerView 是否到达边界
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/2/16
 */
fun RecyclerView.onReachStart(callback: (()->Unit)?) {
    requestBoundaryCallback().onReachStart = callback
}

fun RecyclerView.onReachEnd(callback: (()->Unit)?) {
    requestBoundaryCallback().onReachEnd = callback
}

fun RecyclerView.requestBoundaryCallback(): BoundaryCallback {
    val id = R.id.recycler_view_boundary_callback_id
    return (getTag(id) as? BoundaryCallback)
        ?: BoundaryCallback().also {
            setTag(id, it)
            addOnScrollListener(it)
        }
}

/**
 * 获取 RecyclerView 上 View 中适配器位置（Adapter Position）最小的的位置
 */
fun RecyclerView.minPosition(): Int {
    var p = Int.MAX_VALUE
    for (i in 0 until childCount) {
        val n = getChildAdapterPosition(getChildAt(i))
        if (n in 0 until p) {
            p = n
        }
    }
    return p
}

/**
 * 获取 RecyclerView 上 View 中适配器位置（Adapter Position）最大的的位置
 */
fun RecyclerView.maxPosition(): Int {
    var p = Int.MIN_VALUE
    for (i in 0 until childCount) {
        val n = getChildAdapterPosition(getChildAt(i))
        if (n > p) {
            p = n
        }
    }
    return p
}

class BoundaryCallback: RecyclerView.OnScrollListener() {

    /**
     * 往开始位置滚动，在距开始位置 [thresholdCount] 以内时，回调 [onReachStart]
     * 往结束位置滚动，在距结束位置 [thresholdCount] 以内时，回调 [onReachEnd]
     */
    var thresholdCount = 5
    var onReachStart: (()->Unit)? = null
    var onReachEnd: (()->Unit)? = null

    private var interval = 100L
    private var lastTime = 0L

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
            tryInvoke(recyclerView, null)
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        tryInvoke(recyclerView, dx > 0 || dy > 0)
    }

    private fun tryInvoke(v: RecyclerView, toEnd: Boolean?) {
        if (System.currentTimeMillis() - lastTime < interval || v.adapter == null) {
            return
        }
        lastTime = System.currentTimeMillis()

        val size = v.adapter?.itemCount ?: return
        when (toEnd) {
            true -> if (size - v.maxPosition() < thresholdCount) {
                onReachEnd?.invoke()
            }
            false -> if (v.minPosition() < thresholdCount) {
                onReachStart?.invoke()
            }
            else -> if (size - v.maxPosition() < thresholdCount) {
                onReachEnd?.invoke()
            } else if (v.minPosition() < thresholdCount) {
                onReachStart?.invoke()
            }
        }
    }

}
