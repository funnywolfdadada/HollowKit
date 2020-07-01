package com.funnywolf.hollowkit.scroll.behavior

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scene.ktx.postDelayed
import com.funnywolf.hollowkit.utils.simpleInit
import com.funnywolf.hollowkit.utils.toast
import com.funnywolf.hollowkit.utils.westWorldHolderBackgroundColor
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.PullRefreshBehavior

/**
 * 下拉刷新 demo
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/27
 */
class PullRefreshScene: UserVisibleHintGroupScene() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val context = inflater.context
        val rv = RecyclerView(context).apply {
            simpleInit(55, westWorldHolderBackgroundColor)
        }
        val behavior = PullRefreshBehavior(rv).apply {
            refreshView.loadingView.colorFilter = PorterDuffColorFilter(westWorldHolderBackgroundColor, PorterDuff.Mode.SRC_IN)
            refreshListener = {
                postDelayed(Runnable {
                    context.toast("Refresh success")
                    isRefreshing = false
                }, 3000)
            }
        }
        return BehavioralScrollView(context).apply {
            enableLog = true
            setupBehavior(behavior)
        }
    }

}