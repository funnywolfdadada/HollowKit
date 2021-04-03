package com.funnywolf.hollowkit.monitor

import android.os.Looper
import android.util.Printer
import kotlinx.coroutines.*
import java.lang.Exception

/**
 * 主线程的看门狗定时器，主线程的任意回调执行超过设定时间 [threshold] 就会触发 [callback]
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/4/3
 */
object MainThreadWatchDog: Printer {

    var callback: ((stack: Array<StackTraceElement>)->Unit)? = null
    var threshold: Long = 0

    var enable: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
        }

    private val originPrinter: Printer? = try {
        val field = Looper::class.java.getDeclaredField("mLogging")
        field.isAccessible = true
        field.get(Looper.getMainLooper()) as? Printer
    } catch (e: Exception) {
        null
    }

    private var job: Job? = null

    init {
        Looper.getMainLooper().setMessageLogging(this)
    }

    override fun println(x: String?) {
        originPrinter?.println(x)
        x ?: return
        if (enable) {
            if (x.startsWith(">")) {
                onStart()
            } else if (x.startsWith("<")) {
                onEnd()
            }
        }
    }

    private fun onStart() {
        job?.cancel()
        job = GlobalScope.launch {
            delay(threshold)
            if (isActive) {
                callback?.invoke(Looper.getMainLooper().thread.stackTrace)
            }
        }
    }

    private fun onEnd() {
        job?.cancel()
        job = null
    }
}