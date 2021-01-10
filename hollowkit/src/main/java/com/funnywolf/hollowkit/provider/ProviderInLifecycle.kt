package com.funnywolf.hollowkit.provider

import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.*

/**
 * @author https://github.com/funnywolfdadada
 * @since 2021/1/23
 */
open class ListenerInLifecycleBound<V>(
        protected val lifecycle: Lifecycle,
        updateOnAdded: Boolean,
        listener: (V?) -> Unit
): BasicListener<V>(updateOnAdded, listener), LifecycleEventObserver {

    private val mh by lazy { Handler(Looper.getMainLooper()) }

    init {
        if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
            attachToLifecycle()
        }
    }

    @CallSuper
    protected fun attachToLifecycle() {
        lifecycle.addObserver(this)
    }

    override fun update(v: V?) {
        if (Looper.getMainLooper().isCurrentThread) {
            super.update(v)
        } else {
            mh.post { update(v) }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            dispose()
        }
    }

    override fun dispose() {
        super.dispose()
        lifecycle.removeObserver(this)
    }

}

class ListenerInLifecycleCache<V>(
        lifecycle: Lifecycle,
        updateOnAdded: Boolean,
        listener: (V?) -> Unit
): ListenerInLifecycleBound<V>(lifecycle, updateOnAdded, listener) {

    private val cache = LinkedList<V?>()

    override fun update(v: V?) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) ) {
            super.update(v)
        } else {
            cache.add(v)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && cache.isNotEmpty()) {
            cache.forEach { super.update(it) }
            cache.clear()
        }
    }

    override fun onRemoved(listenable: ListenableValue<V>) {
        super.onRemoved(listenable)
        cache.clear()
    }

    override fun dispose() {
        super.dispose()
        cache.clear()
    }

}
