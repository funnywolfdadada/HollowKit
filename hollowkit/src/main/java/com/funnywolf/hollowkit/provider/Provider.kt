package com.funnywolf.hollowkit.provider

import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


/**
 * Provider 是一个提供依赖注入和监听的工具
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/1/16
 */
open class Provider {

    private var values = ConcurrentHashMap<Class<*>, ListenableValue<*>>(2)

    fun <V> put(clazz: Class<V>, v: ListenableValue<V>?) {
        if (v == null) {
            values.remove(clazz)
        } else {
            values[clazz] = v
        }
    }

    fun <V> get(clazz: Class<V>): ListenableValue<V>? {
        return values[clazz] as? ListenableValue<V>
    }

    fun isSupport(clazz: Class<*>): Boolean {
        return values[clazz] != null
    }

    fun <V> provide(clazz: Class<V>, v: V?) {
        get(clazz)?.value = v
    }

    fun <V> valueOf(clazz: Class<V>): V? {
        return get(clazz)?.value
    }

    fun <V> valueOf(clazz:Class<V>, listener: ValueListener<V>): Boolean {
        return get(clazz)?.add(listener) ?: false
    }

    fun <V> remove(clazz:Class<V>, listener: ValueListener<V>): Boolean {
        return get(clazz)?.remove(listener) ?: false
    }

    fun dispose() {
        values.values.forEach {
            it.clear()
        }
        values.clear()
    }

}

/**
 * 缓存了 [value] 的可观察者
 */
interface ListenableValue<V> {
    var value: V?
    fun add(listener: ValueListener<V>): Boolean
    fun remove(listener: ValueListener<V>): Boolean
    fun clear()
}

/**
 * [ListenableValue] 的观察者
 */
interface ValueListener<V> {
    fun update(v: V?)
    fun onAdded(listenable: ListenableValue<V>)
    fun onRemoved(listenable: ListenableValue<V>)
    fun dispose()
}

open class LiveValue<V>: ListenableValue<V> {

    private val listeners = CopyOnWriteArrayList<ValueListener<V>>()

    override var value: V? = null
        set(value) {
            field = value
            onUpdate(value)
        }

    protected open fun onUpdate(v: V?) {
        listeners.forEach { it.update(v) }
    }

    override fun add(listener: ValueListener<V>): Boolean {
        if (listeners.addIfAbsent(listener)) {
            listener.onAdded(this)
            return true
        }
        return false
    }

    override fun remove(listener: ValueListener<V>): Boolean {
        if (listeners.remove(listener)) {
            listener.onRemoved(this)
            return true
        }
        return false
    }

    override fun clear() {
        ArrayList(listeners).also { l ->
            listeners.clear()
            l.forEach { it.onRemoved(this) }
        }
    }

}

open class BasicListener<V>(
        private val updateOnAdded: Boolean = true,
        private val listener: (V?)->Unit
): ValueListener<V> {
    private var lRef: WeakReference<ListenableValue<V>>? = null

    fun attachedValue(): ListenableValue<V>? = lRef?.get()

    override fun update(v: V?) {
        listener(v)
    }

    override fun onAdded(listenable: ListenableValue<V>) {
        lRef?.get()?.remove(this)
        lRef = WeakReference(listenable)
        if (updateOnAdded) {
            update(listenable.value)
        }
    }

    override fun onRemoved(listenable: ListenableValue<V>) {
        lRef = null
    }

    override fun dispose() {
        lRef?.get()?.remove(this)
        lRef = null
    }

}
