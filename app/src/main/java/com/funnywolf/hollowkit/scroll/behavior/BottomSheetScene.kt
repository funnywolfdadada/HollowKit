package com.funnywolf.hollowkit.scroll.behavior

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.douban.*
import com.funnywolf.hollowkit.recyclerview.LiveList
import com.funnywolf.hollowkit.douban.view.RightDragToOpenView
import com.funnywolf.hollowkit.douban.view.ToolbarView
import com.funnywolf.hollowkit.drawable.RoundRectDrawable
import com.funnywolf.hollowkit.utils.*
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.BottomSheetBehavior
import com.funnywolf.hollowkit.view.scroll.behavior.JellyBehavior

/**
 * 底部浮层页
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class BottomSheetScene: UserVisibleHintGroupScene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        val context = container.context
        val toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()
        val toolBar = ToolbarView(context).apply {
            setup("电视", "西部世界 第三季", R.drawable.poster_westworld_season_3, westWorldHolderBackgroundColor)
            setListeners(View.OnClickListener { navigationScene?.pop() }, null)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbarHeight)
        }
        val recyclerView = RecyclerView(context).apply {
            clipToPadding = false
            setPaddingRelative(0, toolbarHeight, 0, 0)
            layoutManager = LinearLayoutManager(context)
            adapter = SimpleAdapter(
                listOf(
                    Pictures().apply {
                        add(Picture(R.drawable.picture_1))
                        add(Picture(R.drawable.picture_2))
                        add(Picture(R.drawable.picture_3))
                        add(Picture(R.drawable.picture_4))
                        add(Picture(R.drawable.picture_5))
                        add(Picture(R.drawable.picture_6))
                        add(Picture(R.drawable.picture_1))
                        add(Picture(R.drawable.picture_2))
                        add(Picture(R.drawable.picture_3))
                        add(Picture(R.drawable.picture_4))
                        add(Picture(R.drawable.picture_5))
                        add(Picture(R.drawable.picture_6))
                    },
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
                )
            )
                .addHolderInfo(
                    HolderInfo(
                        Picture::class.java,
                        R.layout.holder_big_picture,
                        PictureViewHolder::class.java
                    )
                )
                .addHolderInfo(
                    HolderInfo(
                        Pictures::class.java,
                        R.layout.holder_behavior_scroll_view,
                        PicturesViewHolder::class.java
                    )
                )
        }
        val f = FrameLayout(context).apply {
            background = RoundRectDrawable(westWorldHolderBackgroundColor, 20, 20, 0, 0)
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

        val wh = Point().let {
            requireActivity().windowManager.defaultDisplay.getRealSize(it)
            it.y
        }
        val bottomSheet = BehavioralScrollView(context).apply {
            enableLog = true
            setupBehavior(BottomSheetBehavior(f, BottomSheetBehavior.POSITION_MID, wh / 4, wh / 2))
        }
        return FrameLayout(context).apply {
            addView(RecyclerView(context).apply { simpleInit(111) })
            addView(bottomSheet)
        }
    }
}

class PicturesViewHolder(v: View): SimpleHolder<Pictures>(v) {
    private val bsv = v<BehavioralScrollView>(R.id.behavior_scroll_view)
    private val recyclerView = RecyclerView(v.context)

    private val dragView =
        RightDragToOpenView(v.context)

    private val liveList = LiveList<Any>()

    init {
        dragView.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT).also {
            it.topMargin = 20.dp
            it.bottomMargin = 20.dp
        }
        recyclerView.adapter = SimpleAdapter(
            liveList.get()
        )
            .addHolderInfo(
                HolderInfo(
                    Picture::class.java,
                    R.layout.holder_douban_picture,
                    PictureViewHolder::class.java
                )
            )
        recyclerView.layoutManager = LinearLayoutManager(v.context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
        bsv?.enableLog = true
        bsv?.setupBehavior(JellyBehavior(ViewCompat.SCROLL_AXIS_HORIZONTAL, recyclerView, View(v.context), dragView))
        bsv?.onScrollChangedListeners?.add {
            dragView.process = it.currProcess()
        }
    }

    override fun onBind(data: Pictures) {
        super.onBind(data)
        liveList.clearAddAll(data)
    }
}
