package com.funnywolf.hollowkit.utils

import android.util.SparseArray
import android.view.View
import com.funnywolf.hollowkit.R

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/11/3
 */
inline fun <reified V: View> View.find(id: Int): V? {
    return requestFinder().find(id) as? V
}

fun View.requestFinder(): ViewFinder {
    val id = R.id.view_find_cache_id
    return (getTag(id) as? ViewFinder) ?: ViewFinder(this).also { setTag(id, it) }
}

class ViewFinder(private val v: View): SparseArray<View?>(), View.OnAttachStateChangeListener {

    init {
        v.addOnAttachStateChangeListener(this)
    }

    fun find(id: Int): View? {
        return get(id) ?: v.findViewById<View>(id)?.also {
            put(id, it)
        }
    }

    override fun onViewAttachedToWindow(v: View?) {
        // do nothing
    }

    override fun onViewDetachedFromWindow(v: View?) {
        clear()
    }

}
