package com.funnywolf.hollowkit

import android.app.Activity
import android.os.Bundle
import com.bytedance.scene.NavigationSceneUtility
import com.bytedance.scene.SceneDelegate

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
