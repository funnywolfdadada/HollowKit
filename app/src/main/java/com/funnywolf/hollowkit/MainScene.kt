package com.funnywolf.hollowkit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.douban.DoubanDetailScene
import com.funnywolf.hollowkit.list.ListDemoScene
import com.funnywolf.hollowkit.scroll.behavior.NestedScrollBehaviorScene

/**
 * 主页
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class MainScene: Scene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.scene_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewById<View>(R.id.bt_test)?.setOnClickListener {
            navigationScene?.push(TestScene::class.java)
        }

        findViewById<View>(R.id.bt_list)?.setOnClickListener {
            navigationScene?.push(ListDemoScene::class.java)
        }

        findViewById<View>(R.id.bt_douban)?.setOnClickListener {
            navigationScene?.push(DoubanDetailScene::class.java)
        }

        findViewById<View>(R.id.bt_nested_scroll_behavior)?.setOnClickListener {
            navigationScene?.push(NestedScrollBehaviorScene::class.java)
        }

    }

}