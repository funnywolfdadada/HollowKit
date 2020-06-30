package com.funnywolf.hollowkit.view.scroll.behavior

import android.view.View

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
    override val scrollVertical: Boolean = true
    override val prevView: View? = null
    override val prevScrollTarget: NestedScrollTarget? = null
    override val midView: View = headerView
    override val midScrollTarget: NestedScrollTarget? = null
    override val nextView: View? = contentView
    override val nextScrollTarget: NestedScrollTarget? = null

    override fun scrollSelfFirst(v: BehavioralScrollView, scroll: Int, type: Int): Boolean {
        return true
    }

}