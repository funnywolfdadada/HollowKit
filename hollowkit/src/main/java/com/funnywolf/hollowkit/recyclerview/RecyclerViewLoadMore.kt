package com.funnywolf.hollowkit.recyclerview

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING

/**
 * Utility for invoke [loadMore] when [RecyclerView] reach the bottom.
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/2/16
 */
class RecyclerViewLoadMore(
    private val thresholdPixel: Int,
    private val loadMore: ()->Unit
): RecyclerView.OnScrollListener() {
    var interval = 100L
    private var lastTime = 0L

    fun setup(recyclerView: RecyclerView?) {
        recyclerView?.addOnScrollListener(this)
    }

    fun reset(recyclerView: RecyclerView?) {
        recyclerView?.removeOnScrollListener(this)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (SCROLL_STATE_DRAGGING == newState) {
            tryInvoke(recyclerView)
        }
    }

    private fun tryInvoke(v: RecyclerView) {
        if (System.currentTimeMillis() - lastTime < interval) {
            return
        }
        val rest = (v.computeVerticalScrollRange()
                - v.computeVerticalScrollExtent()
                - v.computeVerticalScrollOffset())
        // rest 等于 0 可能是到底部或者异常情况，需要额外判断
        if (rest in 1..thresholdPixel || !v.canScrollVertically(1)) {
            lastTime = System.currentTimeMillis()
            loadMore.invoke()
        }
    }

}
