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
            recyclerView = RecyclerView(context)
            recyclerView.simpleInit(50)
            recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
            setupViews(
                recyclerView,
//                View(context).also {
//                    it.setBackgroundColor(Color.YELLOW)
//                    it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
//                        lp.topMargin = 500
//                    }
//                },


                View(context).also {
                    it.setBackgroundColor(Color.BLUE)
                    it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
                        lp.topMargin = 200
                    }
                },


//                RecyclerView(context).also {
//                    it.simpleInit(50, Color.YELLOW)
//                    it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
//                        lp.topMargin = 500
//                    }
//                },
                RecyclerView(context).also {
                    it.simpleInit(50, Color.GREEN)
                    it.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also { lp ->
                        lp.topMargin = 100
                    }


                }
            )
        }
    }

}