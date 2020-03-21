package com.funnywolf.hollowkit

import com.bytedance.scene.Scene
import com.bytedance.scene.ui.SceneActivity

class MainActivity : SceneActivity() {
    override fun supportRestore(): Boolean = true

    override fun getHomeSceneClass(): Class<out Scene> = MainScene::class.java
}
