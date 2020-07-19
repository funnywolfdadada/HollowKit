package com.funnywolf.hollowkit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.utils.simpleInit
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.SwipeRefreshBehavior
import com.funnywolf.hollowkit.view.scroll.behavior.commonBehavior

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/9
 */
class TestScene: Scene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val rv = RecyclerView(context).apply {
            simpleInit()
        }
//        val contentView = SwipeRefreshLayout(context).apply {
//            addView(rv)
//        }
        val contentView = BehavioralScrollView(context)
            .setupBehavior(SwipeRefreshBehavior(rv))
        return BehavioralScrollView(context)
            .commonBehavior(
                ViewCompat.SCROLL_AXIS_VERTICAL,
                Space(context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200.dp)
                },
                contentView,
                null
            ) {
                handleNestedScrollFirst = { v, s, t -> true }
                handleScrollSelf = { v, s, t ->
                    if (v.canScrollVertically(s)) {
                        v.scrollBy(0, s / 2)
                        true
                    } else {
                        false
                    }
                }
            }
    }

}
