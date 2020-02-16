package com.funnywolf.hollowkit

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING

/**
 * Utility for invoke [loadMore] when [RecyclerView] reach the bottom.
 *
 * @author funnywolf
 * @since 2020/2/16
 */
class RecyclerViewLoadMore(
    val threstholdPixel: Int,
    val loadMore: ()->Unit
): RecyclerView.OnScrollListener() {
    private val interval = 100L
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
        lastTime = System.currentTimeMillis()
        val rest = (v.computeVerticalScrollRange()
                - v.computeVerticalScrollExtent()- v.computeVerticalScrollOffset())
        if (rest in 1..threstholdPixel || !v.canScrollVertically(1)) {
            loadMore.invoke()
        }
    }

}
