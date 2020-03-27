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
    private val thresholdCount: Int,
    private val loadMore: ()->Unit
): RecyclerView.OnScrollListener() {
    var interval = 30L
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

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        // try invoke when scroll down
        if (dy > 0) {
            tryInvoke(recyclerView)
        }
    }

    private fun tryInvoke(v: RecyclerView) {
        if (System.currentTimeMillis() - lastTime < interval) {
            return
        }
        lastTime = System.currentTimeMillis()
        // only invoke loadMore when we have adapter
        val c = v.adapter?.itemCount ?: return
        if (c < thresholdCount
            || v.findViewHolderForLayoutPosition(c - thresholdCount) != null) {
            loadMore.invoke()
        }
    }

}
