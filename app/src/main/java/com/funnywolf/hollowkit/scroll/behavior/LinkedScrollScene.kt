package com.funnywolf.hollowkit.scroll.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.utils.*
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.LinkedScrollBehavior
import com.funnywolf.hollowkit.view.scroll.behavior.SwipeRefreshBehavior

/**
 * 联动滚动
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/7/12
 */
class LinkedScrollScene: UserVisibleHintGroupScene() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val context = inflater.context
        val rvTop = RecyclerView(context).apply {
            simpleInit(55, westWorldHolderBackgroundColor)
        }
        val rvBottom = RecyclerView(context).apply {
            simpleInit(55, defaultHolderBackgroundColor)
        }
        val content = BehavioralScrollView(context).apply {
            setupBehavior(LinkedScrollBehavior(rvTop, rvBottom, { rvTop }, { rvBottom }))
        }
        return BehavioralScrollView(context).apply {
            setupBehavior(SwipeRefreshBehavior(content) {
                postDelayed(2222) {
                    it.isRefreshing = false
                }
            })
        }
    }

}