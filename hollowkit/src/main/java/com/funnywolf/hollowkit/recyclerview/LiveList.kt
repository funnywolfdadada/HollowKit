package com.funnywolf.hollowkit.recyclerview


import android.database.Observable
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList

/**
 * 提供监听数据变化能力的 ArrayList
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/11/02
 */
class LiveList<E>: ArrayList<E>() {

    var listener: Listener? = null

    fun move(from: Int, to: Int) {
        super.add(if (from < to) { to - 1 } else { to }, super.removeAt(from))
        listener?.onMove(from, to)
    }

    override fun set(index: Int, element: E): E {
        val ret = super.set(index, element)
        listener?.onRangeChanged(index, 1)
        return ret
    }

    override fun add(element: E): Boolean {
        return if(super.add(element)) {
            listener?.onRangeInserted(size - 1, 1)
            true
        } else {
            false
        }
    }

    override fun add(index: Int, element: E) {
        super.add(index, element)
        listener?.onRangeInserted(index, 1)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val start = size
        return if (super.addAll(elements)) {
            listener?.onRangeInserted(start, elements.size)
            true
        } else {
            false
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        return if (super.addAll(index, elements)) {
            listener?.onRangeInserted(index, elements.size)
            true
        } else {
            false
        }
    }

    override fun remove(element: E): Boolean {
        val index = indexOf(element)
        return if (index >= 0) {
            removeAt(index)
            true
        } else {
            false
        }
    }

    override fun removeAt(index: Int): E {
        val ret = super.removeAt(index)
        listener?.onRangeRemoved(index, 1)
        return ret
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return if (super.removeAll(elements)) {
            listener?.onChanged()
            true
        } else {
            false
        }
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        return if (super.retainAll(elements)) {
            listener?.onChanged()
            true
        } else {
            false
        }
    }

    override fun clear() {
        super.clear()
        listener?.onChanged()
    }

    interface Listener {

        fun onChanged() {
            // Do nothing
        }

        fun onRangeChanged(start: Int, count: Int) {
            // do nothing
        }

        fun onRangeInserted(start: Int, count: Int) {
            // do nothing
        }

        fun onRangeRemoved(start: Int, count: Int) {
            // do nothing
        }

        fun onMove(from: Int, to: Int) {
            // do nothing
        }

    }

}

class LiveListObserver: Observable<LiveList.Listener>(), LiveList.Listener {
    override fun onChanged() { mObservers.forEach { it.onChanged() } }
    override fun onRangeChanged(start: Int, count: Int) { mObservers.forEach { it.onRangeChanged(start, count) } }
    override fun onRangeInserted(start: Int, count: Int) { mObservers.forEach { it.onRangeInserted(start, count) } }
    override fun onRangeRemoved(start: Int, count: Int) { mObservers.forEach { it.onRangeRemoved(start, count) } }
    override fun onMove(from: Int, to: Int) { mObservers.forEach { it.onMove(from, to) } }
}

class AdapterListener(adapter: RecyclerView.Adapter<*>): LiveList.Listener {

    private var adapterRef = WeakReference<RecyclerView.Adapter<*>>(adapter)

    override fun onChanged() {
        adapterRef.get()?.notifyDataSetChanged()
    }

    override fun onRangeChanged(start: Int, count: Int) {
        adapterRef.get()?.notifyItemRangeChanged(start, count)
    }

    override fun onRangeInserted(start: Int, count: Int) {
        adapterRef.get()?.notifyItemRangeInserted(start, count)
    }

    override fun onRangeRemoved(start: Int, count: Int) {
        adapterRef.get()?.notifyItemRangeRemoved(start, count)
    }

    override fun onMove(from: Int, to: Int) {
        adapterRef.get()?.notifyItemMoved(from, to)
    }
}

fun LiveList<*>.bind(adapter: RecyclerView.Adapter<*>? = null) {
    listener = adapter?.let { AdapterListener(it) }
}
