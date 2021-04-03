package com.funnywolf.hollowkit.monitor

import android.view.Choreographer
import androidx.annotation.MainThread

/**
 * 统计 Vsync 数量
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/4/3
 */
object VsyncTick: Choreographer.FrameCallback {

    var enable: Boolean = false
        @MainThread
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                addFrameCallback()
            } else {
                removeFrameCallback()
            }
        }

    var count: Long = 0
        private set

    override fun doFrame(frameTimeNanos: Long) {
        count++
        if (enable) {
            addFrameCallback()
        }
    }

    private fun addFrameCallback() {
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun removeFrameCallback() {
        Choreographer.getInstance().removeFrameCallback(this)
    }

}