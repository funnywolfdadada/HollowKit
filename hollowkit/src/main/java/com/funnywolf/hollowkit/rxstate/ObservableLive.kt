package com.funnywolf.hollowkit.rxstate

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.internal.disposables.DisposableHelper
import java.util.concurrent.atomic.AtomicReference

/**
 * 赋予 [Observable] 生命周期感知能力，在 [owner] 生命周期结束时自动解除监听
 * 根据 [minActiveState] 区分生命周期的激活和非激活状态，在激活状态时正常传递事件，
 * 在非激活状态时则缓存事件，直到状态再次激活时，分发缓存的最后一个事件
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/9/17
 */
class ObservableLive<T: Any>(
    private val source: Observable<T>,
    private val owner: LifecycleOwner,
    private val minActiveState: Lifecycle.State
): Observable<T>() {

    override fun subscribeActual(observer: Observer<in T>) {
        source.subscribe(LiveObserver(observer, owner, minActiveState))
    }

    private class LiveObserver<T: Any>(
        private val downstream: Observer<T>,
        private val owner: LifecycleOwner,
        private val minActiveState: Lifecycle.State
    ): Observer<T>, Disposable, LifecycleEventObserver {
        /**
         * 上游的引用
         */
        private val upstreamRef = AtomicReference<Disposable?>()
        /**
         * 未激活时期暂存的事件，目前只暂存一个
         */
        private var pendingRef = AtomicReference<T?>()

        @CallSuper
        override fun onSubscribe(d: Disposable) {
            // 已销毁，直接取消
            if (owner.lifecycle.currentState < Lifecycle.State.INITIALIZED) {
                d.dispose()
                return
            }
            if (DisposableHelper.setOnce(upstreamRef, d)) {
                downstream.onSubscribe(this)
                // 下游在 onSubscribe 后没有 dispose 就开始监听生命周期
                if (!isDisposed) {
                    owner.lifecycle.addObserver(this)
                }
            }
        }

        override fun onNext(t: T) {
            if (isDisposed) {
                return
            }
            // 在激活状态就正常发送事件，未激活时暂存
            if (owner.lifecycle.currentState >= minActiveState) {
                downstream.onNext(t)
            } else {
                pendingRef.set(t)
            }
        }

        @CallSuper
        override fun onError(e: Throwable) {
            if (isDisposed) {
                return
            }
            dispose()
            downstream.onError(e)
        }

        @CallSuper
        override fun onComplete() {
            if (isDisposed) {
                return
            }
            dispose()
            downstream.onComplete()
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (isDisposed) {
                return
            }
            // 销毁时自动 dispose
            if (event == Lifecycle.Event.ON_DESTROY) {
                dispose()
                return
            }
            // 到激活状态后，自动分发暂存的事件
            if (owner.lifecycle.currentState >= minActiveState) {
                pendingRef.getAndSet(null)?.also {
                    onNext(it)
                }
            }
        }

        override fun dispose() {
            DisposableHelper.dispose(upstreamRef)
            // dispose 时移除生命周期监听
            owner.lifecycle.removeObserver(this)
        }

        override fun isDisposed(): Boolean {
            return DisposableHelper.isDisposed(upstreamRef.get())
        }

    }

}

fun <T: Any> Observable<T>.live(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.CREATED
): Observable<T> = ObservableLive(this, owner, minActiveState)

fun <T: Any> Observable<T>.liveSubscribe(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.CREATED,
    onNext: Consumer<T>
) = live(owner, minActiveState).subscribe(onNext)
