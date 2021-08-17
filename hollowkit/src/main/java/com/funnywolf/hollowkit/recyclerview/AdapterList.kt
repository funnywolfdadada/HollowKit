package com.funnywolf.hollowkit.recyclerview

import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

/**
 * 数据变更同步通知 [RecyclerView.Adapter] 的列表
 */
open class AdapterList<T>: AbstractList<T>() {
    protected open val rawList: MutableList<T> = ArrayList()

    /**
     * [RecyclerView.Adapter] 引用，用于 notify
     */
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

    /**
     * 取消 [adapterRef] 绑定
     */
    fun unbind() {
        adapterRef?.clear()
        adapterRef = null
    }

    /**
     * 添加 [data] 并 [RecyclerView.Adapter.notifyItemInserted]，null 时不作处理
     */
    fun add(data: T?): Boolean {
        return addAt(rawList.size, data)
    }

    /**
     * 在 [index] 处添加 [data] 并 [RecyclerView.Adapter.notifyItemInserted]，null 时不作处理
     */
    fun addAt(index: Int, data: T?): Boolean {
        if (data != null && safeAddIndex(index)) {
            rawList.add(index, data)
            adapterRef?.get()?.notifyItemInserted(index)
            return true
        }
        return false
    }

    /**
     * 添加 [c] 中所有元素并 [RecyclerView.Adapter.notifyItemRangeInserted]，null 时不作处理
     */
    fun addAll(c: Collection<T>?): Boolean {
        return addAllAt(rawList.size, c)
    }

    /**
     * 在 [index] 处添加 [c] 中所有元素并 [RecyclerView.Adapter.notifyItemRangeInserted]，null 时不作处理
     */
    fun addAllAt(index: Int, c: Collection<T>?): Boolean {
        if (!c.isNullOrEmpty() && safeAddIndex(index)) {
            rawList.addAll(index, c)
            adapterRef?.get()?.notifyItemRangeInserted(index, c.size)
            return true
        }
        return false
    }

    /**
     * 移除 [data] 并 [RecyclerView.Adapter.notifyItemRemoved]，null 时不作处理
     */
    fun remove(data: T?): Boolean {
        return removeAt(rawList.indexOf(data ?: return false))
    }

    /**
     * 移除 [index] 处的数据并 [RecyclerView.Adapter.notifyItemRemoved]
     */
    fun removeAt(index: Int): Boolean {
        if (safeIndex(index)) {
            rawList.removeAt(index)
            adapterRef?.get()?.notifyItemRemoved(index)
            return true
        }
        return false
    }

    /**
     * 移除所有的 [c] 中数据并 [RecyclerView.Adapter.notifyDataSetChanged]
     */
    fun removeAll(c: Collection<T>?): Boolean {
        if (!c.isNullOrEmpty() && rawList.removeAll(c)) {
            adapterRef?.get()?.notifyDataSetChanged()
            return true
        }
        return false
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
    fun update(data: T?): Boolean {
        return updateAt(rawList.indexOf(data ?: return false))
    }

    /**
     * 更新 [index] 处的数据并 [RecyclerView.Adapter.notifyItemChanged]
     * 如果 [data] 不为 null，则将 [index] 处的数据设置成 [data]
     */
    fun updateAt(index: Int, data: T? = null): Boolean {
        if (safeIndex(index)) {
            if (data != null) {
                rawList[index] = data
            }
            adapterRef?.get()?.notifyItemChanged(index)
            return true
        }
        return false
    }

    /**
     * 执行 [func] 然后更新 [index] 处的数据并 [RecyclerView.Adapter.notifyItemChanged]
     */
    fun updateAtBy(index: Int, func: ((T) -> Unit)): Boolean {
        if (safeIndex(index)) {
            func.invoke(rawList[index])
            adapterRef?.get()?.notifyItemChanged(index)
            return true
        }
        return false
    }

    /**
     * 如果 [func] 不为 null 则执行 [func]，然后 [RecyclerView.Adapter.notifyDataSetChanged]
     */
    fun updateAll(func: ((MutableList<T>) -> Unit)? = null) {
        func?.invoke(rawList)
        adapterRef?.get()?.notifyDataSetChanged()
    }

    /**
     * 将 [from] 处的数据移动到下标 [to]
     */
    fun move(from: Int, to: Int) {
        rawList.add(if (from < to) { to - 1 } else { to }, rawList.removeAt(from))
        adapterRef?.get()?.notifyItemMoved(from, to)
    }

    private fun safeIndex(index: Int) = index >= 0 && index < rawList.size

    private fun safeAddIndex(index: Int) = index >= 0 && index <= rawList.size

}
