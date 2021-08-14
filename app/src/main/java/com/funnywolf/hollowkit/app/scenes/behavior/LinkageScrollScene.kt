package com.funnywolf.hollowkit.app.scenes.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.app.databinding.SceneLinkageScrollBinding
import com.funnywolf.hollowkit.app.utils.initPictures
import com.funnywolf.hollowkit.drawable.RoundRectDrawable
import com.funnywolf.hollowkit.utils.*
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollListener
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.BottomSheetLayout

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/26
 */
class LinkageScrollScene: UserVisibleHintGroupScene() {

    private lateinit var binding: SceneLinkageScrollBinding

    private val floatingHeight = 100.dp

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        binding = SceneLinkageScrollBinding.inflate(inflater, container, false)

        binding.rvLinkageTop.initPictures()
        binding.rvLinkageBottom.initPictures(true)
        binding.rvLinkageBottom.background = RoundRectDrawable(0xFF03A9F4.toInt(), 20.dp, 20.dp, 0, 0)

        binding.linkageScroll.topScrollTarget = { binding.rvLinkageTop }
        binding.linkageScroll.listeners.add(object: BehavioralScrollListener {
            override fun onScrollChanged(v: BehavioralScrollView, from: Int, to: Int) {
                updateFloatState()
            }
        })

        binding.bottomSheet.setup(BottomSheetLayout.POSITION_MIN, floatingHeight)
        updateFloatState()
        return binding.root
    }

    private fun updateFloatState() {
        if (binding.bottomSheet.indexOfChild(binding.rvLinkageBottom) >= 0) {
            if (binding.linkageScroll.scrollY >= floatingHeight) {
                binding.bottomSheet.visibility = View.GONE
                binding.bottomSheet.removeView(binding.rvLinkageBottom)
                if (binding.layoutBottom.indexOfChild(binding.rvLinkageBottom) < 0) {
                    binding.layoutBottom.addView(binding.rvLinkageBottom)
                }
                binding.linkageScroll.bottomScrollTarget = { binding.rvLinkageBottom }
            }
        } else {
            if (binding.linkageScroll.scrollY < floatingHeight) {
                binding.linkageScroll.bottomScrollTarget = null
                if (binding.layoutBottom.indexOfChild(binding.rvLinkageBottom) >= 0) {
                    binding.layoutBottom.removeView(binding.rvLinkageBottom)
                }
                if (binding.bottomSheet.indexOfChild(binding.rvLinkageBottom) < 0) {
                    binding.bottomSheet.addView(binding.rvLinkageBottom)
                }
                binding.bottomSheet.visibility = View.VISIBLE
            }
        }
    }

}