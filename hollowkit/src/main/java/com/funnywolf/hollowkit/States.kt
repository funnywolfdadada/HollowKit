package com.funnywolf.hollowkit

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

/**
 * 「状态」的集合，支持读写和观察
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/18
 */
class States {
    private val states = ConcurrentHashMap<String, State>()
    private val listeners = ConcurrentHashMap<StateListener, Any>()

    fun get(type: String?): State? = type?.let { states[it] }

    fun set(state: State?): States {
        state ?: return this
        states[state.type] = state
        listeners.keys.forEach {
            it.notify(state)
        }
        return this
    }

    fun addListener(listener: StateListener): States {
        listeners[listener] = this
        return this
    }

    fun removeListener(listener: StateListener): States {
        listeners.remove(listener)
        return this
    }

}

interface StateListener {

    /**
     * 状态改变的回调
     */
    fun onStateChanged(state: State)

    /**
     * [onStateChanged] 发生异常时的回调
     */
    fun onStateChangedError(error: Throwable?) = Unit

}

fun StateListener.notify(state: State) = try {
    onStateChanged(state)
} catch (e: Exception) {
    onStateChangedError(e)
}

private val mainHandler = Handler(Looper.getMainLooper())

data class LifecycleStateListener(
    val listener: StateListener
): StateListener, LifecycleEventObserver {

    private var lifecycle: Lifecycle? = null
    private var minLifecycleState: Lifecycle.State = Lifecycle.State.CREATED
    private var states: States? = null

    private val stateList = ArrayList<State>()

    fun addTo(states: States, lifecycle: Lifecycle? = null, minLifecycleState: Lifecycle.State = Lifecycle.State.CREATED) {
        clear()
        this.minLifecycleState = minLifecycleState
        this.lifecycle = lifecycle
        lifecycle?.addObserver(this)
        this.states = states
        states.addListener(this)
    }

    fun clear() {
        states?.removeListener(this)
        states = null
        lifecycle?.removeObserver(this)
        lifecycle = null
        stateList.clear()
    }

    override fun onStateChanged(state: State) {
        if (Thread.currentThread() != Looper.getMainLooper().thread) {
            mainHandler.post { onStateChanged(state) }
            return
        }
        stateList.add(state)
        dispatchAll()
    }

    override fun onStateChangedError(error: Throwable?) {
        super.onStateChangedError(error)
        listener.onStateChangedError(error)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        dispatchAll()
    }

    private fun dispatchAll() {
        val current = lifecycle?.currentState
        when {
            current == Lifecycle.State.DESTROYED -> clear()
            current == null || current >= minLifecycleState -> {
                stateList.forEach { listener.notify(it) }
                stateList.clear()
            }
        }
    }

}

/**
 * 所有数据（文本、列表、等）和行为（刷新、点击、删除等）都抽象为「状态」
 * 「状态」用 [type] 区分类型，[data] 承载元数据，可能处于就绪 [READY]、加载中 [LOADING] 或出错 [ERROR]
 * 出错时可以携带错误信息 [error]
 */
data class State (
    val type: String,
    var data: Any? = null,
    var status: Int = READY,
    var error: Throwable? = null
) {

    companion object {

        const val READY = 0
        const val LOADING = 1
        const val ERROR = 2

        @JvmStatic
        fun ready(type: String, data: Any? = null): State {
            return State(type, data, READY)
        }

        @JvmStatic
        fun loading(type: String, data: Any? = null): State {
            return State(type, data, LOADING)
        }

        @JvmStatic
        fun error(type: String, throwable: Throwable? = null, data: Any? = null): State {
            return State(type, data, ERROR, throwable)
        }

    }
}
