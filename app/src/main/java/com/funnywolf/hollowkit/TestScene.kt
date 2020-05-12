package com.funnywolf.hollowkit

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.utils.simpleInit
import com.funnywolf.hollowkit.view.BehavioralNestedScrollLayout
import com.funnywolf.hollowkit.view.NestedScrollBehavior

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/9
 */
class TestScene: Scene() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return BehavioralNestedScrollLayout(container.context).apply {
            recyclerView = RecyclerView(context).also {
                it.simpleInit(50)
                it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
//                    lp.topMargin = 100
                }
            }
            recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
            setupBehavior(
                NestedScrollBehavior(recyclerView)
                    .setPrevView(View(context).also {
                        it.setBackgroundColor(Color.BLUE)
                        it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
                            lp.height = 100
                        }
                    })
                    .setNextView(RecyclerView(context).also {
                        it.simpleInit(50, Color.GREEN)
                        it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
                            lp.topMargin = 100
                        }
                    })
                    .setOverScrollOffset(-100, 100)
            )
        }
    }

}