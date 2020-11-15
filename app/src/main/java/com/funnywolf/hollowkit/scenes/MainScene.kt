package com.funnywolf.hollowkit.scenes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.scenes.douban.DoubanDetailScene
import com.funnywolf.hollowkit.scenes.behavior.ScrollBehaviorScene

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

        bind(R.id.bt_test, TestScene::class.java)
        bind(R.id.bt_nested_scroll_behavior, ScrollBehaviorScene::class.java)
        bind(R.id.bt_rich_text, RichTextScene::class.java)
        bind(R.id.stateful_layout, StatefulLayoutScene::class.java)
        bind(R.id.bt_douban, DoubanDetailScene::class.java)
    }

    private fun bind(id: Int, clazz: Class<out Scene>) {
        findViewById<View>(id)?.setOnClickListener {
            navigationScene?.push(clazz)
        }
    }

}