package com.funnywolf.hollowkit.recyclerview

import androidx.recyclerview.widget.RecyclerView


/**
 * 用于监听 RecyclerView 是否到达边界
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/2/16
 */
class BoundaryCallback(
    private val rv: RecyclerView
): RecyclerView.OnScrollListener(), Runnable {

    init {
        rv.addOnScrollListener(this)
    }

    /**
     * 往开始位置滚动，在距开始位置 [thresholdCount] 以内时，回调 [onReachStart]
     * 往结束位置滚动，在距结束位置 [thresholdCount] 以内时，回调 [onReachEnd]
     */
    var thresholdCount = 5
    private var onReachStart: (()->Unit)? = null
    private var onReachEnd: (()->Unit)? = null

    private var pending = false
    private var lastDirection = 0

    /**
     * 到达列表开始的回调
     */
    fun onReachStart(callback: (()->Unit)?): BoundaryCallback {
        onReachStart = callback
        return this
    }

    /**
     * 到达列表最后的回调
     */
    fun onReachEnd(callback: (()->Unit)?): BoundaryCallback {
        onReachEnd = callback
        return this
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        lastDirection = 0
        tryInvoke()
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        lastDirection = when {
            dy != 0 -> dy
            dx != 0 -> dx
            else -> return
        }
        tryInvoke()
    }

    private fun tryInvoke() {
        if (!pending) {
            pending = true
            rv.postDelayed(this, 100)
        }
    }

    override fun run() {
        pending = false
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
        when {
            lastDirection < 0 -> if (minPosition < thresholdCount) {
                onReachStart?.invoke()
            }
            lastDirection > 0 -> if (maxPosition > size - thresholdCount) {
                onReachEnd?.invoke()
            }
            lastDirection == 0 -> if (maxPosition > size - thresholdCount) {
                onReachEnd?.invoke()
            } else if (minPosition < thresholdCount) {
                onReachStart?.invoke()
            }
        }
    }

}
