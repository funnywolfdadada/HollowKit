package com.funnywolf.hollowkit

import android.os.Looper
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * Utilities for run on main thread or [Lifecycle.Event]
 *
 * @author funnywolf
 * @since 2020/2/16
 */

interface Runner {
    fun cancel()
    fun isCanceled(): Boolean
}

fun runOnMain(runnable: Runnable, delay: Long = 0, oneShot: Boolean = true): Runner? {
    return MainRunner(delay, runnable, oneShot)
}

fun LifecycleOwner.runOn(
        targetEvent: Lifecycle.Event,
        runnable: Runnable,
        oneShot: Boolean = true
): Runner {
    return LifecycleEventRunner(
        this,
        targetEvent,
        runnable,
        oneShot
    )
}

private val mainHandler by lazy { android.os.Handler(Looper.getMainLooper()) }

internal open class BaseRunner(
        private val runnable: Runnable,
        private val oneShot: Boolean = true
): Runner {
    private var canceled = false

    protected fun doRun() {
        runnable.run()
        if (oneShot) {
            cancel()
        }
    }

    @CallSuper
    override fun cancel() {
        canceled = true
    }

    override fun isCanceled(): Boolean {
        return canceled
    }

}

internal class MainRunner(
        private val delay: Long = 0,
        runnable: Runnable,
        oneShot: Boolean = true
): BaseRunner(runnable, oneShot), Runnable {

    init {
        mainHandler.postDelayed(this, delay)
    }

    override fun run() {
        doRun()
        if (!isCanceled()) {
            mainHandler.postDelayed(this, delay)
        }
    }

    override fun cancel() {
        super.cancel()
        mainHandler.removeCallbacks(this)
    }

}

internal class LifecycleEventRunner(
        owner: LifecycleOwner,
        private val targetEvent: Lifecycle.Event,
        runnable: Runnable,
        oneShot: Boolean = true
) : LifecycleObserver, BaseRunner(runnable, oneShot) {
    private var lifecycle: Lifecycle? = null

    init {
        if (owner.lifecycle.currentState != Lifecycle.State.DESTROYED) {
            owner.lifecycle.addObserver(this)
            lifecycle = owner.lifecycle
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onEvent(owner: LifecycleOwner?, event: Lifecycle.Event?) {
        if (targetEvent == event || targetEvent == Lifecycle.Event.ON_ANY) {
            doRun()
        }
        if (Lifecycle.Event.ON_DESTROY == event) {
            cancel()
        }
    }

    override fun cancel() {
        super.cancel()
        lifecycle?.removeObserver(this)
        lifecycle = null
    }

}
