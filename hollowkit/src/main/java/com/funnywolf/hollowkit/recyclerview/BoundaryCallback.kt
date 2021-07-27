package com.funnywolf.hollowkit.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 * 用于监听 RecyclerView 是否到达边界
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/2/16
 */
class BoundaryCallback(
    private val rv: RecyclerView
): RecyclerView.OnScrollListener(), View.OnLayoutChangeListener, Runnable {

    init {
        rv.addOnScrollListener(this)
        rv.addOnLayoutChangeListener(this)
    }

    /**
     * 往开始位置滚动，在距开始位置 [thresholdCount] 以内时，回调 [onReachStart]
     * 往结束位置滚动，在距结束位置 [thresholdCount] 以内时，回调 [onReachEnd]
     */
    var thresholdCount = 5
    private var onReachStart: (()->Unit)? = null
    private var onReachEnd: (()->Unit)? = null

    fun onReachStart(callback: (()->Unit)?): BoundaryCallback {
        onReachStart = callback
        return this
    }

    fun onReachEnd(callback: (()->Unit)?): BoundaryCallback {
        onReachEnd = callback
        return this
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        rv.post(this)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        rv.post(this)
    }

    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        rv.post(this)
    }

    override fun run() {
        val size = rv.adapter?.itemCount ?: return
        var minPosition = Int.MAX_VALUE
        var maxPosition = Int.MIN_VALUE
        for (i in 0 until rv.childCount) {
            val p = rv.getChildAdapterPosition(rv.getChildAt(i))
            if (p < minPosition) {
                minPosition = p
            }
            if (p > maxPosition) {
                maxPosition = p
            }
        }
        if (maxPosition > size - thresholdCount) {
            onReachEnd?.invoke()
        } else if (minPosition < thresholdCount) {
            onReachStart?.invoke()
        }
    }

}
