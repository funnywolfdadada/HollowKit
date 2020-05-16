package com.funnywolf.hollowkit

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.utils.simpleInit
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralNestedScrollLayout
import com.funnywolf.hollowkit.view.scroll.behavior.NestedScrollBehavior
import com.funnywolf.hollowkit.view.scroll.behavior.NestedScrollTarget

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
        return BehavioralNestedScrollLayout(
            container.context
        ).apply {
            val recyclerView1 = RecyclerView(context).also {
                it.simpleInit(50)
                it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
//                    lp.topMargin = 100
                }
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
            recyclerView1.overScrollMode = View.OVER_SCROLL_NEVER
            setupBehavior(
                object: NestedScrollBehavior{
                    override val scrollVertical: Boolean = true
                    override val prevView: View? = space
                    override val prevScrollTarget: NestedScrollTarget? = null
                    override val midView: View = recyclerView1
//                    override val midScrollTarget: NestedScrollTarget? = {_, _, _ -> recyclerView1}
                    override val midScrollTarget: NestedScrollTarget? = null
                    override val nextView: View? = recyclerView2
//                    override val nextScrollTarget: NestedScrollTarget? = {_, _, _ -> recyclerView2}
                    override val nextScrollTarget: NestedScrollTarget? = null
                }
            )
        }
    }

}