package com.funnywolf.hollowkit.scenes.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.drawable.RoundRectDrawable
import com.funnywolf.hollowkit.utils.*
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollListener
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.BottomSheetLayout
import com.funnywolf.hollowkit.view.scroll.behavior.LinkageScrollLayout

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/26
 */
class LinkageScrollScene: UserVisibleHintGroupScene() {

    private lateinit var layoutTop: FrameLayout
    private lateinit var rvTop: RecyclerView

    private lateinit var layoutBottom: FrameLayout
    private lateinit var rvBottom: RecyclerView

    private lateinit var bottomSheet: BottomSheetLayout

    private lateinit var linkageScroll: LinkageScrollLayout

    private val floatingHeight = 100.dp

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        return inflater.inflate(R.layout.scene_linkage_scroll, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linkageScroll = view.findViewById(R.id.linkageScroll)
        layoutTop = view.findViewById(R.id.layoutTop)
        rvTop = view.findViewById(R.id.rvLinkageTop)
        layoutBottom = view.findViewById(R.id.layoutBottom)
        rvBottom = view.findViewById(R.id.rvLinkageBottom)
        bottomSheet = view.findViewById(R.id.bottomSheet)

        rvTop.initPictures()
        rvBottom.initPictures(true)
        rvBottom.background = RoundRectDrawable(0xFF03A9F4.toInt(), 20.dp, 20.dp, 0, 0)

        linkageScroll.topScrollTarget = { rvTop }
        linkageScroll.listeners.add(object: BehavioralScrollListener {
            override fun onScrollChanged(v: BehavioralScrollView, from: Int, to: Int) {
                updateFloatState()
            }
        })

        bottomSheet.setup(BottomSheetLayout.POSITION_MIN, floatingHeight)
        updateFloatState()
    }

    private fun updateFloatState() {
        if (bottomSheet.indexOfChild(rvBottom) >= 0) {
            if (linkageScroll.scrollY >= floatingHeight) {
                bottomSheet.visibility = View.GONE
                bottomSheet.removeView(rvBottom)
                if (layoutBottom.indexOfChild(rvBottom) < 0) {
                    layoutBottom.addView(rvBottom)
                }
                linkageScroll.bottomScrollTarget = { rvBottom }
            }
        } else {
            if (linkageScroll.scrollY < floatingHeight) {
                linkageScroll.bottomScrollTarget = null
                if (layoutBottom.indexOfChild(rvBottom) >= 0) {
                    layoutBottom.removeView(rvBottom)
                }
                if (bottomSheet.indexOfChild(rvBottom) < 0) {
                    bottomSheet.addView(rvBottom)
                }
                bottomSheet.visibility = View.VISIBLE
            }
        }
    }

}