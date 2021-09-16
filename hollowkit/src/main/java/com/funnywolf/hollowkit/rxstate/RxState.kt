package com.funnywolf.hollowkit.rxstate

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
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
        val od = ObserverDisposable(observer)
        od.onSubscribe(od)
        if (!od.isDisposed) {
            add(od)
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
    ): AtomicBoolean(false), Observer<V>, Disposable {

        override fun isDisposed(): Boolean = get()

        override fun dispose() {
            if (compareAndSet(false, true)) {
                remove(this)
            }
        }

        override fun onSubscribe(d: Disposable) {
            downstream.onSubscribe(d)
        }

        override fun onNext(v: V) {
            downstream.onNext(v)
        }

        override fun onError(e: Throwable) {
            downstream.onError(e)
        }

        override fun onComplete() {
            downstream.onComplete()
        }
    }
}

class RxMutableEvent<V: Any>(
    private val onEmit: ((V)->Unit)? = null
): RxEvent<V>() {

    public override fun emit(v: V) {
        super.emit(v)
        onEmit?.invoke(v)
    }

}

abstract class RxState<V: Any>: RxEvent<V>() {
    /**
     * 当前状态的值
     */
    abstract val value: V

    override fun add(observer: ObserverDisposable) {
        super.add(observer)
        // 状态需要在观察者添加时，发送当前最新的值
        observer.onNext(value)
    }
}

class RxMutableState<V: Any>(
    initValue: V,
    private val onChanged: ((V)->Unit)? = null
) : RxState<V>() {

    override var value: V = initValue
        set(value) {
            field = value
            emit(value)
            onChanged?.invoke(value)
        }

}

