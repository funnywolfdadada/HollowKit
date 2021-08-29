package com.funnywolf.hollowkit.app.fragments.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.funnywolf.hollowkit.app.databinding.PageJellyLayoutBinding
import com.funnywolf.hollowkit.app.utils.initHorizontalPictures
import com.funnywolf.hollowkit.app.utils.initPictures

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/10/24
 */
class JellyFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ViewGroup {
        val binding = PageJellyLayoutBinding.inflate(inflater, container, false)

        binding.rvJellyTop.initHorizontalPictures()
        binding.rvJellyBottom.initPictures(true)

        binding.jellyLayout.resistance = { _, _ -> 0.5F }
        binding.jellyLayout.onTouchRelease = {
            it.smoothScrollTo(if (it.lastScrollDir < 0) { it.minScroll } else { 0 })
        }
        return binding.root
    }

}