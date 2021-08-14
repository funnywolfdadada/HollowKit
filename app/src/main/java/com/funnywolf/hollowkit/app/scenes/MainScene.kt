package com.funnywolf.hollowkit.app.scenes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.app.databinding.SceneMainBinding
import com.funnywolf.hollowkit.app.scenes.douban.DoubanDetailScene
import com.funnywolf.hollowkit.app.scenes.behavior.ScrollBehaviorScene

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
        val binding = SceneMainBinding.inflate(inflater, container, false)
        bind(binding.btTest, TestScene::class.java)
        bind(binding.btNestedScrollBehavior, ScrollBehaviorScene::class.java)
        bind(binding.btRichText, RichTextScene::class.java)
        bind(binding.statefulLayout, StatefulLayoutScene::class.java)
        bind(binding.okhttpProgress, OkHttpProgressScene::class.java)
        bind(binding.btPermission, PermissionRequestScene::class.java)
        bind(binding.btDouban, DoubanDetailScene::class.java)
        bind(binding.btExpandableText, ExpandableTextScene::class.java)
        return binding.root
    }

    private fun bind(v: View, clazz: Class<out Scene>) {
        v.setOnClickListener {
            navigationScene?.push(clazz)
        }
    }

}