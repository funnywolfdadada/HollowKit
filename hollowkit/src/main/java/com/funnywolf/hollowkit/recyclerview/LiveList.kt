package com.funnywolf.hollowkit.recyclerview


import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

/**
 * Updates of the list will notify [RecyclerView.Adapter]
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/2/16
 */

interface LiveListSource<T> {
    /**
     * Raw list.
     */
    fun get(): List<T>

    /**
     * Bind/Unbind adapter for notify.
     */
    fun bind(adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>)
    fun unbind()

    /**
     * Appends [data] to the end of list.
     * Will not execute when [data] is null.
     */
    fun add(data: T?)

    /**
     * Inserts [data] at the specified position [index] in list.
     * Will not execute when [data] is null.
     */
    fun addAt(index: Int, data: T?)

    /**
     * Appends all the elements in [c] to the end of list, in the order that they are returned by the specified collection's Iterator.
     */
    fun addAll(c: Collection<T>)

    /**
     * Inserts all of the elements in [c] into the list, starting at the specified position [index].
     */
    fun addAllAt(index: Int, c: Collection<T>)

    /**
     * Removes the first occurrence of [data] from the list, if it is present.
     */
    fun remove(data: T?)

    /**
     * Removes the element at the specified position [index] in the list.
     */
    fun removeAt(index: Int)

    /**
     * Removes from the list all of its elements that are contained in [c].
     */
    fun removeAll(c: Collection<T>)

    /**
     * Update [data] if it is in the list.
     */
    fun update(data: T?)

    /**
     * Update data in the specified position [index], and replace it if [data] is not null.
     */
    fun updateAt(index: Int, data: T? = null)

    /**
     * Update data in the specified position [index] after invoke [func].
     */
    fun updateAtBy(index: Int, func: ((T)->Unit)? = null)

    /**
     * Update the whole list after invoke [func].
     */
    fun updateAll(func: ((MutableList<T>)->Unit)? = null)

    /**
     * Remove all the elements from this list.
     */
    fun clear()

    /**
     * After removing all the elements from the list, add [data] into it.
     */
    fun clearAdd(data: T?)

    /**
     * After removing all the elements from the list, Add all the elements in [c] into it.
     */
    fun clearAddAll(c: Collection<T>)
}

class LiveList<T>: LiveListSource<T> {

    private val rawList: MutableList<T> = ArrayList()
    private var adapterRef: WeakReference<RecyclerView.Adapter<out RecyclerView.ViewHolder>>? = null

    override fun get(): List<T> = rawList

    override fun bind(adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>) {
        adapterRef = WeakReference(adapter)
    }

    override fun unbind() {
        adapterRef?.clear()
        adapterRef = null
    }

    override fun add(data: T?) {
        addAt(rawList.size, data)
    }

    override fun addAt(index: Int, data: T?) {
        if (data != null && safeAddIndex(index)) {
            rawList.add(index, data)
            adapterRef?.get()?.notifyItemInserted(index)
        }
    }

    override fun addAll(c: Collection<T>) {
        addAllAt(rawList.size, c)
    }

    override fun addAllAt(index: Int, c: Collection<T>) {
        if (safeAddIndex(index) && c.isNotEmpty()) {
            rawList.addAll(index, c)
            adapterRef?.get()?.notifyItemRangeInserted(index, c.size)
        }
    }

    override fun remove(data: T?) {
        removeAt(rawList.indexOf(data ?: return))
    }

    override fun removeAt(index: Int) {
        if (safeIndex(index)) {
            rawList.removeAt(index)
            adapterRef?.get()?.notifyItemRemoved(index)
        }
    }

    override fun removeAll(c: Collection<T>) {
        if (c.isNotEmpty()) {
            rawList.removeAll(c)
            adapterRef?.get()?.notifyDataSetChanged()
        }
    }

    override fun update(data: T?) {
        updateAt(rawList.indexOf(data ?: return))
    }

    override fun updateAt(index: Int, data: T?) {
        if (safeIndex(index)) {
            if (data != null) {
                rawList[index] = data
            }
            adapterRef?.get()?.notifyItemChanged(index)
        }
    }

    override fun updateAtBy(index: Int, func: ((T) -> Unit)?) {
        if (safeIndex(index)) {
            func?.invoke(rawList[index])
            adapterRef?.get()?.notifyItemChanged(index)
        }
    }

    override fun updateAll(func: ((MutableList<T>) -> Unit)?) {
        func?.invoke(rawList)
        adapterRef?.get()?.notifyDataSetChanged()
    }

    override fun clear() {
        rawList.clear()
        adapterRef?.get()?.notifyDataSetChanged()
    }

    override fun clearAdd(data: T?) {
        data ?: return
        rawList.clear()
        rawList.add(data)
        adapterRef?.get()?.notifyDataSetChanged()
    }

    override fun clearAddAll(c: Collection<T>) {
        rawList.clear()
        rawList.addAll(c)
        adapterRef?.get()?.notifyDataSetChanged()
    }

    private fun safeIndex(index: Int) = index >= 0 && index < rawList.size

    private fun safeAddIndex(index: Int) = index >= 0 && index <= rawList.size
}
