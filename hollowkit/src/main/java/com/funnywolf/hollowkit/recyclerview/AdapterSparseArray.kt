package com.funnywolf.hollowkit.recyclerview

import android.util.SparseArray
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

/**
 * 数据变更同步通知观察者的稀疏数组
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/11/3
 */
interface AdapterSparseArray<T> {

    /**
     * 绑定观察者
     */
    fun bind(adapter: RecyclerView.Adapter<*>)

    /**
     * 解绑观察者
     */
    fun unbind()

    /**
     * 数量
     */
    fun size(): Int

    /**
     * [index] 下标处的建
     */
    fun keyAt(index: Int): Int

    /**
     * [index] 下标处的值
     */
    fun valueAt(index: Int): T?

    /**
     * [key] 对应的值
     */
    fun get(key: Int): T?

}

interface MutableAdapterSparseArray<T>: AdapterSparseArray<T> {

    /**
     * 设置 [key] 对应的值为 [value]
     */
    fun put(key: Int, value: T?)

    /**
     * 设置下标 [index] 的值为 [value]
     */
    fun setValueAt(index: Int, value: T?)

    /**
     * 移除 [key] 对应的值
     */
    fun remove(key: Int)

    /**
     * 移除下标 [index] 处的值
     */
    fun removeAt(index: Int)

}

open class AdapterSparseArrayImpl<T>: MutableAdapterSparseArray<T> {

    private val array = SparseArray<T>()
    private var adapterRef: WeakReference<RecyclerView.Adapter<*>>? = null

    override fun bind(adapter: RecyclerView.Adapter<*>) {
        adapterRef = WeakReference(adapter)
    }

    override fun unbind() {
        adapterRef = null
    }

    override fun size(): Int = array.size()

    override fun keyAt(index: Int): Int = array.keyAt(index)

    override fun valueAt(index: Int): T? = array.valueAt(index)

    override fun get(key: Int): T? = array.get(key)

    override fun put(key: Int, value: T?) {
        array.put(key, value)
        adapterRef?.get()?.notifyItemChanged(key, value)
    }

    override fun setValueAt(index: Int, value: T?) {
        array.setValueAt(index, value)
        adapterRef?.get()?.notifyItemChanged(array.keyAt(index), value)
    }

    override fun remove(key: Int) {
        array.remove(key)
        adapterRef?.get()?.notifyItemChanged(key, null)
    }

    override fun removeAt(index: Int) {
        array.removeAt(index)
        adapterRef?.get()?.notifyItemChanged(array.keyAt(index), null)
    }

}