package com.funnywolf.hollowkit.provider

import android.os.Looper
import android.view.View
import com.funnywolf.hollowkit.R

/**
 * 通过 View 树进行构建和访问的 Provider
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/1/17
 */
/**
 * 以回调 [listener] 的方式监听 [V] 类型 Provider 的数据
 */
inline fun <reified V: Any> View.valueOf(
        updateOnAdded: Boolean = true,
        noinline listener: (V?)->Unit
): ListenerInView<V> {
    return ListenerInView(this, V::class.java, updateOnAdded, listener)
}

/**
 * 查找 Provider，并获取 [V]类型的数据
 * 注：需要在 View.isAttachedToWindow == true 时使用
 */
inline fun <reified V: Any> View.valueOf(): V? {
    return lookForProvider(V::class.java)?.value
}

/**
 * 在 View 树中向上查找 Provider
 */
fun <V> View.lookForProvider(clazz: Class<V>, includeSelf: Boolean = true): ListenableValue<V>? {
    if (!isAttachedToWindow) {
        return null
    }
    var v: View? = if (includeSelf) {
        this
    } else {
        parent as? View
    }
    while (v != null) {
        val provider = v.provider(clazz)
        if (provider != null) {
            return provider
        }
        v = v.parent as? View
    }
    return null
}

/**
 * 尝试获取当前 View 的 Provider
 */
fun <V> View.provider(clazz: Class<V>): ListenableValue<V>? {
    return provider()?.get(clazz)
}

fun View.provider(): Provider? {
    return getTag(R.id.provider_in_view_id) as? Provider
}

/**
 * 设置当前 View 的 Provider，null 表示清除
 */
fun View.setProvider(provider: Provider?) {
    setTag(R.id.provider_in_view_id, provider)
}

/**
 * 获取当前 View 的 Provider，没有时新建一个
 */
fun View.requestProvider(): Provider {
    return provider() ?: Provider().also {
        setProvider(it)
    }
}

/**
 * View 中的 BasicListener 包装类，在 onViewAttachedToWindow 时自动查找 Provider 并监听，
 * 并在 onViewDetachedFromWindow 从 Provider 移除监听，提供手动关闭的方法 [dispose]
 */
class ListenerInView<V>(
        private val attachedView: View,
        private val clazz: Class<V>,
        updateOnAdded: Boolean,
        listener: (V?) -> Unit
): BasicListener<V>(updateOnAdded, listener) {
    private val listener: View.OnAttachStateChangeListener

    init {
        this.listener = object: View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                attachedView.lookForProvider(clazz)?.add(this@ListenerInView)
            }

            override fun onViewDetachedFromWindow(v: View?) {
                attachedValue()?.remove(this@ListenerInView)
            }
        }
        attachedView.addOnAttachStateChangeListener(this.listener)
        if (attachedView.isAttachedToWindow) {
            this.listener.onViewAttachedToWindow(attachedView)
        }
    }

    override fun update(v: V?) {
        if (Looper.getMainLooper().isCurrentThread) {
            super.update(v)
        } else {
            attachedView.post { update(v) }
        }
    }

    override fun dispose() {
        super.dispose()
        attachedView.removeOnAttachStateChangeListener(listener)
    }

}
