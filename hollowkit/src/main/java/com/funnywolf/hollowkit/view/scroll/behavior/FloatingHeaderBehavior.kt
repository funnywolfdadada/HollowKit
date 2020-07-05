package com.funnywolf.hollowkit.view.scroll.behavior

import android.view.View
import androidx.core.view.ViewCompat

/**
 * 悬浮头部效果的 behavior
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/30
 */
class FloatingHeaderBehavior(
    val contentView: View,
    val headerView: View
): NestedScrollBehavior {
    override val scrollAxis: Int = ViewCompat.SCROLL_AXIS_VERTICAL
    override val prevView: View? = null
    override val midView: View = headerView
    override val nextView: View? = contentView

    override fun scrollSelfFirst(v: BehavioralScrollView, scroll: Int, type: Int): Boolean {
        return true
    }

}