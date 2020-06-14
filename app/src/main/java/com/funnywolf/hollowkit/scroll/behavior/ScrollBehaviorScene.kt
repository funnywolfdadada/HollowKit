package com.funnywolf.hollowkit.scroll.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.PushOptions
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.utils.toast
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.PullRefreshBehavior

/**
 * [BehavioralScrollView] 的 Demo 入口
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class ScrollBehaviorScene: Scene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.scene_nested_scroll_behavior, container, false)
        return BehavioralScrollView(inflater.context).apply {
            setupBehavior(PullRefreshBehavior(v).apply {
                enable = true
                refreshListener = {
                    postDelayed({
                        isRefreshing = false
                        context.toast("refresh success")
                    }, 3000)
                }
            })
            enableLog = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewById<View>(R.id.bt_bottom_sheet)?.setOnClickListener {
            navigationScene?.push(BottomSheetScene(), PushOptions.Builder().setTranslucent(true).build())
        }
    }

}
