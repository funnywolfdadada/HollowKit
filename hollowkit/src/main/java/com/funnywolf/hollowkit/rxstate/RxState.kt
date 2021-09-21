package com.funnywolf.hollowkit.rxstate

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.SafeObserver
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 基于 RxJava 的热数据流，可观察事件 [RxEvent] 和可观察状态 [RxState]
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/9/17
 */
abstract class RxEvent<V: Any>: Observable<V>() {
    /**
     * 线程安全的观察者集合
     */
    private val observers = CopyOnWriteArrayList<ObserverDisposable>()

    override fun subscribeActual(observer: Observer<in V>) {
        ObserverDisposable(SafeObserver(observer)).also {
            it.onSubscribe()
            if (!it.isDisposed) {
                add(it)
            }
        }
    }

    protected open fun emit(v: V) {
        observers.forEach { it.onNext(v) }
    }

    protected open fun add(observer: ObserverDisposable) {
        observers.add(observer)
    }

    protected open fun remove(observer: ObserverDisposable) {
        observers.remove(observer)
    }

    protected inner class ObserverDisposable(
        private val downstream: Observer<in V>
    ): AtomicBoolean(false), Disposable {

        override fun isDisposed(): Boolean = get()

        override fun dispose() {
            if (compareAndSet(false, true)) {
                remove(this)
            }
        }

        fun onSubscribe() {
            downstream.onSubscribe(this)
        }

        fun onNext(v: V) {
            downstream.onNext(v)
        }

    }
}

class RxMutableEvent<V: Any>(
    private val onEmit: ((V)->Unit)? = null
): RxEvent<V>() {

    public override fun emit(v: V) {
        onEmit?.invoke(v)
        super.emit(v)
    }

}

abstract class RxState<V: Any>: RxEvent<V>() {
    /**
     * 当前状态的值
     */
    abstract val value: V

}

class RxMutableState<V: Any>(
    initValue: V,
    private val onChanged: ((V)->Unit)? = null
) : RxState<V>() {

    override var value: V = initValue
        set(value) {
            field = value
            onChanged?.invoke(value)
            emit(value)
        }

    override fun add(observer: ObserverDisposable) {
        super.add(observer)
        // 状态需要在观察者添加时，发送当前最新的值
        observer.onNext(value)
    }

}

