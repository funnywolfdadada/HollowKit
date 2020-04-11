package com.funnywolf.hollowkit.utils

import android.view.View
import android.view.ViewGroup

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/31
 */

fun ViewGroup.containsChild(v: View?): Boolean {
    v ?: return false
    return if (indexOfChild(v) >= 0) {
        true
    } else {
        repeat(childCount) {
            val child = getChildAt(it)
            if (child is ViewGroup && child.containsChild(v)) {
                return true
            }
        }
        false
    }
}