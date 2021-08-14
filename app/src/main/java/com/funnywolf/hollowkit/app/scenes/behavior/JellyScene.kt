package com.funnywolf.hollowkit.app.scenes.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.app.databinding.SceneJellyLayoutBinding
import com.funnywolf.hollowkit.app.utils.initHorizontalPictures
import com.funnywolf.hollowkit.app.utils.initPictures

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
        val binding = SceneJellyLayoutBinding.inflate(inflater, container, false)

        binding.rvJellyTop.initHorizontalPictures()
        binding.rvJellyBottom.initPictures(true)

        binding.jellyLayout.resistance = { _, _ -> 0.5F }
        binding.jellyLayout.onTouchRelease = {
            it.smoothScrollTo(if (it.lastScrollDir < 0) { it.minScroll } else { 0 })
        }
        return binding.root
    }

}