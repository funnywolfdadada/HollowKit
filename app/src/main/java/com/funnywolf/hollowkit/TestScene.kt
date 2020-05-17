package com.funnywolf.hollowkit

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.utils.simpleInit
import com.funnywolf.hollowkit.view.scroll.behavior.*

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
        val context = container.context
        val recyclerView1 = RecyclerView(context).also {
            it.simpleInit(50)
            it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
                lp.topMargin = 100
//                    lp.bottomMargin = 100
            }
            it.overScrollMode = View.OVER_SCROLL_NEVER
        }
        val recyclerView2 = RecyclerView(context).also {
            it.simpleInit(50, Color.GREEN)
            it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
                lp.topMargin = 100
            }
        }
        val space = Space(context).also {
            it.layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).also { lp ->
                lp.topMargin = 100
            }
        }

        val frameLayout = FrameLayout(context).also {
            it.setBackgroundColor(Color.BLUE)
            it.layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).also { lp ->
                lp.topMargin = 100
            }
            it.addView(recyclerView1)
        }
        val bnsl = BehavioralNestedScrollLayout(
            container.context
        ).apply {
            setupBehavior(BottomSheetBehavior(frameLayout, BottomSheetBehavior.POSITION_MID, 100, 500))
        }
        return FrameLayout(context).apply {
            addView(recyclerView2)
            addView(bnsl)
        }
    }

}