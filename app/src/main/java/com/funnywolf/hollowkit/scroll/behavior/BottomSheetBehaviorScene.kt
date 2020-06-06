package com.funnywolf.hollowkit.scroll.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.douban.Picture
import com.funnywolf.hollowkit.douban.PictureViewHolder
import com.funnywolf.hollowkit.recyclerview.HolderInfo
import com.funnywolf.hollowkit.recyclerview.SimpleAdapter
import com.funnywolf.hollowkit.utils.roundRectDrawable
import com.funnywolf.hollowkit.view.ToolbarView
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralNestedScrollLayout
import com.funnywolf.hollowkit.view.scroll.behavior.BottomSheetBehavior

/**
 * 底部浮层页
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class BottomSheetBehaviorScene: Scene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        val context = container.context
        val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()
        val toolBar = ToolbarView(context).apply {
            setup("电视", "西部世界 第三季", R.drawable.poster_westworld_season_3, 0xFF9E7D6D.toInt())
            setListeners(View.OnClickListener { navigationScene?.pop() }, null)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbarHeight)
        }
        val recyclerView = RecyclerView(context).apply {
            clipToPadding = false
            setPaddingRelative(0, toolbarHeight, 0, 0)
            layoutManager = LinearLayoutManager(context)
            adapter = SimpleAdapter(listOf(
                Picture(R.drawable.picture_1),
                Picture(R.drawable.picture_2),
                Picture(R.drawable.picture_3),
                Picture(R.drawable.picture_4),
                Picture(R.drawable.picture_5),
                Picture(R.drawable.picture_6),
                Picture(R.drawable.picture_1),
                Picture(R.drawable.picture_2),
                Picture(R.drawable.picture_3),
                Picture(R.drawable.picture_4),
                Picture(R.drawable.picture_5),
                Picture(R.drawable.picture_6)
            )).addHolderInfo(HolderInfo(Picture::class.java, R.layout.holder_big_picture, PictureViewHolder::class.java))
        }
        val f = FrameLayout(context).apply {
            background = roundRectDrawable(0xFF9E7D6D.toInt(), 20, 20, 0, 0)
            addView(recyclerView)
            addView(toolBar)
            recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                private var scroll = 0F
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    scroll += dy
                    toolBar.process = scroll / toolbarHeight
                }
            })
        }

        return BehavioralNestedScrollLayout(context).apply {
            setupBehavior(BottomSheetBehavior(f, BottomSheetBehavior.POSITION_MID, 100, 500))
        }
    }
}