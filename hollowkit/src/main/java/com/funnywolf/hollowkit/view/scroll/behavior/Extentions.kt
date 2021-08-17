package com.funnywolf.hollowkit.view.scroll.behavior

import androidx.core.view.ViewCompat

/**
 * 这里放一些只访问公共属性/方法的扩展方法
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/10/24
 */
/**
 * 当前滚动的百分比
 */
fun BehavioralScrollView.currProcess(): Float {
    return when(nestedScrollAxes) {
        ViewCompat.SCROLL_AXIS_HORIZONTAL -> if (scrollX > 0) {
            if (maxScroll != 0) { scrollX.toFloat() / maxScroll } else { 0F }
        } else {
            if (minScroll != 0) { scrollX.toFloat() / minScroll } else { 0F }
        }
        ViewCompat.SCROLL_AXIS_VERTICAL -> if (scrollY > 0) {
            if (maxScroll != 0) { scrollY.toFloat() / maxScroll } else { 0F }
        } else {
            if (minScroll != 0) { scrollY.toFloat() / minScroll } else { 0F }
        }
        else -> 0F
    }
}

/**
 * 是否发生了滚动
 */
fun BehavioralScrollView.isScrolled(): Boolean = when (nestedScrollAxes) {
    ViewCompat.SCROLL_AXIS_VERTICAL -> scrollY != 0
    ViewCompat.SCROLL_AXIS_HORIZONTAL -> scrollX != 0
    else -> false
}

/**
 * 当前滚动位置在 0 或者最大最小值，则认为是稳定位置
 */
fun BehavioralScrollView.inStablePosition(): Boolean {
    val scroll = when (nestedScrollAxes) {
        ViewCompat.SCROLL_AXIS_VERTICAL -> scrollY
        ViewCompat.SCROLL_AXIS_HORIZONTAL -> scrollX
        else -> return true
    }
    return scroll == 0 || scroll == minScroll || scroll == maxScroll
}

/**
 * 发生嵌套滚动的直接子 view 是否完全展示出来
 */
fun BehavioralScrollView.isScrollChildTotalShowing(): Boolean {
    val v = nestedScrollChild ?: return true
    return when (nestedScrollAxes) {
        ViewCompat.SCROLL_AXIS_VERTICAL -> v.y - scrollY >= 0 && v.y + v.height - scrollY <= height
        ViewCompat.SCROLL_AXIS_HORIZONTAL -> v.x - scrollX >= 0 && v.x + v.width - scrollX <= width
        else -> return true
    }
}
