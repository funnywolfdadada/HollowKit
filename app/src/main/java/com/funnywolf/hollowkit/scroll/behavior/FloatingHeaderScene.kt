package com.funnywolf.hollowkit.scroll.behavior

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.utils.simpleInit
import com.funnywolf.hollowkit.utils.westWorldHolderBackgroundColor
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.FloatingHeaderBehavior
import com.funnywolf.hollowkit.view.scroll.behavior.PullRefreshBehavior

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/30
 */
class FloatingHeaderScene: UserVisibleHintGroupScene() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        val context = inflater.context
        val height = 200.dp
        val floatingHeader = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.poster_1917)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        }
        val rv = RecyclerView(context).apply {
            simpleInit(55, westWorldHolderBackgroundColor)
        }
        val contentView = BehavioralScrollView(context).apply {
            setupBehavior(PullRefreshBehavior(rv) {
                postDelayed({ it.isRefreshing = false }, 2000)
            }.apply {
                refreshView.loadingView.colorFilter = PorterDuffColorFilter(westWorldHolderBackgroundColor, PorterDuff.Mode.SRC_IN)
            })
        }
        return BehavioralScrollView(context).apply {
            enableLog = true
            setupBehavior(FloatingHeaderBehavior(contentView, floatingHeader))
        }
    }

}