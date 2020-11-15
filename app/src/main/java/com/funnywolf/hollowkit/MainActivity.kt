package com.funnywolf.hollowkit

import android.app.Activity
import android.os.Bundle
import com.bytedance.scene.NavigationSceneUtility
import com.bytedance.scene.SceneDelegate
import com.funnywolf.hollowkit.scenes.MainScene

/**
 * App 的壳 Activity，主页在 [MainScene]
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class MainActivity : Activity() {
    private var delegate: SceneDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = NavigationSceneUtility.setupWithActivity(this, MainScene::class.java)
            .supportRestore(true)
            .build()
    }

    override fun onBackPressed() {
        if (delegate?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
}
