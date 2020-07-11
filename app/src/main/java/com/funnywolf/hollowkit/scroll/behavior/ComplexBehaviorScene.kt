package com.funnywolf.hollowkit.scroll.behavior

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.utils.defaultHolderBackgroundColor
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.utils.simpleInit
import com.funnywolf.hollowkit.utils.westWorldHolderBackgroundColor
import com.funnywolf.hollowkit.view.scroll.behavior.*

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/7/12
 */
class ComplexBehaviorScene: UserVisibleHintGroupScene() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        val context = inflater.context

        val linked = BehavioralScrollView(context).apply {
            val rvTop = RecyclerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300.dp)
                simpleInit(55, westWorldHolderBackgroundColor)
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
            val rvBottom = RecyclerView(context).apply {
                simpleInit(55, defaultHolderBackgroundColor)
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
            setupBehavior(LinkedScrollBehavior(rvTop, rvBottom, { rvTop }, { rvBottom }))
        }

        val collapse = BehavioralScrollView(context).apply {
            val header = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageResource(R.drawable.poster_westworld_season_3)
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200.dp)
            }
            setupBehavior(CollapsingHeaderBehavior(linked, header, false))
        }

        return BehavioralScrollView(context).apply {
            enableLog = true
            setupBehavior(PullRefreshBehavior(collapse) {
                postDelayed(2222) {
                    it.isRefreshing = false
                }
            }.apply {
                refreshView.loadingView.colorFilter = PorterDuffColorFilter(westWorldHolderBackgroundColor, PorterDuff.Mode.SRC_IN)
            })
        }
    }

}