package com.funnywolf.hollowkit.view.scroll.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/26
 */
class CommonScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BehavioralScrollView(context, attrs, defStyleAttr) {

    override var behavior: NestedScrollBehavior? = null

    fun setViews(prev: View?, mid: View?, next: View?): CommonScrollView {
        removeAllViews()
        prevView = prev?.also { addView(it) }
        midView = mid?.also { addView(it) }
        nextView = next?.also { addView(it) }
        return this
    }

    fun setBehavior(v: NestedScrollBehavior?): CommonScrollView {
        behavior = v
        requestLayout()
        return this
    }

}
