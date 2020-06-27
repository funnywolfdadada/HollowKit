package com.funnywolf.hollowkit.scroll.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.douban.view.ToolbarView
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.utils.simpleInit
import com.funnywolf.hollowkit.utils.westWorldHolderBackgroundColor
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.HeaderBehavior

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/27
 */
class CollapsingHeaderScene: UserVisibleHintGroupScene() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val context = inflater.context

        val toolbarHeight = 50.dp
        val toolbar = ToolbarView(context).apply {
            setup("电视", "西部世界 第三季", R.drawable.poster_westworld_season_3, westWorldHolderBackgroundColor)
            setListeners(View.OnClickListener { navigationScene?.pop() }, null)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbarHeight)
        }

        val headerHeight = 200.dp
        val headerView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.picture_3)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, headerHeight)
        }
        val rv = RecyclerView(context).apply {
            simpleInit(55, westWorldHolderBackgroundColor)
        }
        val behavior = HeaderBehavior(rv, headerView).apply {

        }
        val behavioralScrollView = BehavioralScrollView(context).apply {
            setupBehavior(behavior)
            onScrollChangedListeners.add {
                toolbar.process = it.scrollY / headerHeight.toFloat()
            }
        }
        return FrameLayout(context).apply {
            addView(behavioralScrollView)
            addView(toolbar)
        }
    }

}