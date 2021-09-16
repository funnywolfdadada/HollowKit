package com.funnywolf.hollowkit.rxstate

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.atomic.AtomicReference

/**
 * 带生命周期感知的观察者，能够在生命周期结束时自动断开对上游的观察
 * TODO: 实现激活和未激活状态的管理
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/9/17
 */
fun <T: Any> Observable<T>.subscribeWith(
    owner: LifecycleOwner, consumer: (t: T)->Unit
): LiveObserver<T> {
    val observer = object: LiveObserver<T>(owner) {
        override fun onNext(t: T) {
            consumer(t)
        }
    }
    subscribe(observer)
    return observer
}

abstract class LiveObserver<T: Any>(
    private val owner: LifecycleOwner
): Observer<T>, LifecycleEventObserver, Disposable {
    private val disposable = AtomicReference<Disposable?>()

    @CallSuper
    override fun onSubscribe(d: Disposable) {
        dispose()
        if (owner.isDestroyed) {
            d.dispose()
        } else {
            disposable.set(d)
            owner.lifecycle.addObserver(this)
        }
    }

    @CallSuper
    override fun onError(e: Throwable) {
        dispose()
    }

    @CallSuper
    override fun onComplete() {
        dispose()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (source != owner) {
            return
        }
        if (event == Lifecycle.Event.ON_DESTROY) {
            dispose()
        }
    }

    override fun dispose() {
        disposable.getAndSet(null)?.dispose()
        owner.lifecycle.removeObserver(this)
    }

    override fun isDisposed(): Boolean = disposable.get()?.isDisposed != false
}

val LifecycleOwner.isDestroyed
    get() = lifecycle.currentState < Lifecycle.State.INITIALIZED
