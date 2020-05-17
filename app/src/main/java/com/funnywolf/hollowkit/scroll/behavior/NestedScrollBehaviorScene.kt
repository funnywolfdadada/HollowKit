package com.funnywolf.hollowkit.scroll.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.PushOptions
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralNestedScrollLayout

/**
 * [BehavioralNestedScrollLayout] 的 Demo 入口
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class NestedScrollBehaviorScene: Scene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.scene_nested_scroll_behavior, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewById<View>(R.id.bt_bottom_sheet)?.setOnClickListener {
            navigationScene?.push(BottomSheetBehaviorScene(), PushOptions.Builder().setTranslucent(true).build())
        }

    }

}
