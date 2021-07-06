package com.funnywolf.hollowkit.recyclerview

import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

/**
 * 数据变更同步通知 [RecyclerView.Adapter] 的列表
 */
abstract class AbstractAdapterList<T>: AbstractList<T>() {
    protected abstract val rawList: MutableList<T>

    protected var adapterRef: WeakReference<RecyclerView.Adapter<*>>? = null

    override val size: Int
        get() = rawList.size

    override fun get(index: Int): T = rawList[index]

    /**
     * 绑定要通知的 [adapter]
     */
    fun bind(adapter: RecyclerView.Adapter<*>) {
        adapterRef = WeakReference(adapter)
    }

    fun unbind() {
        adapterRef?.clear()
        adapterRef = null
    }

    /**
     * 添加 [data] 并 [RecyclerView.Adapter.notifyItemInserted]，null 时不作处理
     */
    fun add(data: T?) {
        addAt(rawList.size, data)
    }

    /**
     * 在 [index] 处添加 [data] 并 [RecyclerView.Adapter.notifyItemInserted]，null 时不作处理
     */
    fun addAt(index: Int, data: T?) {
        if (data != null && safeAddIndex(index)) {
            rawList.add(index, data)
            adapterRef?.get()?.notifyItemInserted(index)
        }
    }

    /**
     * 添加 [c] 中所有元素并 [RecyclerView.Adapter.notifyItemRangeInserted]，null 时不作处理
     */
    fun addAll(c: Collection<T>?) {
        addAllAt(rawList.size, c)
    }

    /**
     * 在 [index] 处添加 [c] 中所有元素并 [RecyclerView.Adapter.notifyItemRangeInserted]，null 时不作处理
     */
    fun addAllAt(index: Int, c: Collection<T>?) {
        if (!c.isNullOrEmpty() && safeAddIndex(index)) {
            rawList.addAll(index, c)
            adapterRef?.get()?.notifyItemRangeInserted(index, c.size)
        }
    }

    /**
     * 移除 [data] 并 [RecyclerView.Adapter.notifyItemRemoved]，null 时不作处理
     */
    fun remove(data: T?) {
        removeAt(rawList.indexOf(data ?: return))
    }

    /**
     * 移除 [index] 处的数据并 [RecyclerView.Adapter.notifyItemRemoved]
     */
    fun removeAt(index: Int) {
        if (safeIndex(index)) {
            rawList.removeAt(index)
            adapterRef?.get()?.notifyItemRemoved(index)
        }
    }

    /**
     * 移除所有的 [c] 中数据并 [RecyclerView.Adapter.notifyDataSetChanged]
     */
    fun removeAll(c: Collection<T>?) {
        if (!c.isNullOrEmpty() && rawList.removeAll(c)) {
            adapterRef?.get()?.notifyDataSetChanged()
        }
    }

    /**
     * 清除所有数据，并 [RecyclerView.Adapter.notifyDataSetChanged]
     */
    fun clear() {
        rawList.clear()
        adapterRef?.get()?.notifyDataSetChanged()
    }

    /**
     * 更新 [data] 并 [RecyclerView.Adapter.notifyItemChanged]
     */
    fun update(data: T?) {
        updateAt(rawList.indexOf(data ?: return))
    }

    /**
     * 更新 [index] 处的数据并 [RecyclerView.Adapter.notifyItemChanged]
     * 如果 [data] 不为 null，则将 [index] 数据设置成 [data]
     */
    fun updateAt(index: Int, data: T? = null) {
        if (safeIndex(index)) {
            if (data != null) {
                rawList[index] = data
            }
            adapterRef?.get()?.notifyItemChanged(index)
        }
    }

    /**
     * 执行 [func] 然后更新 [index] 处的数据并 [RecyclerView.Adapter.notifyItemChanged]
     */
    fun updateAtBy(index: Int, func: ((T) -> Unit)) {
        if (safeIndex(index)) {
            func.invoke(rawList[index])
            adapterRef?.get()?.notifyItemChanged(index)
        }
    }

    /**
     * 如果 [func] 不为 null 则执行 [func]，然后 [RecyclerView.Adapter.notifyDataSetChanged]
     */
    fun updateAll(func: ((MutableList<T>) -> Unit)? = null) {
        func?.invoke(rawList)
        adapterRef?.get()?.notifyDataSetChanged()
    }

    fun move(from: Int, to: Int) {
        rawList.add(if (from < to) { to - 1 } else { to }, rawList.removeAt(from))
        adapterRef?.get()?.notifyItemMoved(from, to)
    }

    private fun safeIndex(index: Int) = index >= 0 && index < rawList.size

    private fun safeAddIndex(index: Int) = index >= 0 && index <= rawList.size

}

class AdapterList(override val rawList: MutableList<Any> = ArrayList()) : AbstractAdapterList<Any>()
