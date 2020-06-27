package com.funnywolf.hollowkit.scroll.behavior

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.PushOptions
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.utils.toast
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.HeaderBehavior
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
        val v: View = inflater.inflate(R.layout.scene_nested_scroll_behavior, container, false)
        return BehavioralScrollView(inflater.context).apply {
//            setupBehavior(PullRefreshBehavior(v).apply {
//                enable = true
//                refreshListener = {
//                    postDelayed({
//                        isRefreshing = false
//                        context.toast("refresh success")
//                    }, 3000)
//                }
//            })
            val height = 200.dp
            val header = View(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
                setBackgroundResource(R.drawable.picture_3)
            }
            v.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                topMargin = 50.dp
            }
            setupBehavior(HeaderBehavior(v, header))
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
