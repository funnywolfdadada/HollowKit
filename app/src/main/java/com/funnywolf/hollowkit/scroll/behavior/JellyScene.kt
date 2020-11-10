package com.funnywolf.hollowkit.scroll.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.utils.initHorizontalPictures
import com.funnywolf.hollowkit.utils.initPictures
import com.funnywolf.hollowkit.view.scroll.behavior.JellyLayout

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/10/24
 */
class JellyScene: UserVisibleHintGroupScene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        return inflater.inflate(R.layout.scene_jelly_layout, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jelly = view.findViewById<JellyLayout>(R.id.jelly_layout)
        val rvTop = view.findViewById<RecyclerView>(R.id.rv_jelly_top)
        val rvBottom = view.findViewById<RecyclerView>(R.id.rv_jelly_bottom)

        rvTop.initHorizontalPictures()
        rvBottom.initPictures(true)

        jelly.resistance = { _, _ -> 0.5F }
        jelly.onTouchRelease = {
            it.smoothScrollTo(if (it.lastScrollDir < 0) { it.minScroll } else { 0 })
        }
    }

}