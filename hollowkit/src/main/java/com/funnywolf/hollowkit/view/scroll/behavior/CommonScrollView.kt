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

    public override var behavior: NestedScrollBehavior? = super.behavior

}
