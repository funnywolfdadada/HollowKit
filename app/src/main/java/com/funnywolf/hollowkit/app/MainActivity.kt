package com.funnywolf.hollowkit.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentActivity
import com.bytedance.scene.NavigationSceneUtility
import com.bytedance.scene.SceneDelegate
import com.funnywolf.hollowkit.drawable.RoundRectDrawable
import com.funnywolf.hollowkit.monitor.MainThreadWatchDog
import com.funnywolf.hollowkit.monitor.VsyncTick
import com.funnywolf.hollowkit.app.scenes.MainScene
import kotlinx.coroutines.*

/**
 * App 的壳 Activity，主页在 [MainScene]
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class MainActivity : FragmentActivity() {
    private var delegate: SceneDelegate? = null
    private var fpsWindow: FpsWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startWatchDog()
        delegate = NavigationSceneUtility.setupWithActivity(this, MainScene::class.java)
            .supportRestore(true)
            .build()
        fpsWindow = FpsWindow(this).show()
    }

    private fun startWatchDog() {
        MainThreadWatchDog.threshold = 500
        MainThreadWatchDog.callback = { a ->
            Log.d("MainThreadWatchDog", "start ---------------------------")
            a.forEach {
                Log.d("MainThreadWatchDog", "$it")
            }
            Log.d("MainThreadWatchDog", "end -----------------------------")
        }
        MainThreadWatchDog.enable = true
    }

    override fun onBackPressed() {
        if (delegate?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        VsyncTick.enable = true
        fpsWindow?.start()
    }

    override fun onStop() {
        super.onStop()
        VsyncTick.enable = false
        fpsWindow?.stop()
    }

}

class FpsWindow(private val activity: Activity): PopupWindow() {

    private val textView: TextView = TextView(activity).also {
        it.textSize = 16F
        it.setTextColor(Color.RED)
        it.background = RoundRectDrawable(0x40000000, 10)
        it.setPadding(16)
        val lock = Object()
        it.setOnClickListener {
            GlobalScope.launch {
                delay(1000)
                synchronized(lock) { lock.notifyAll() }
            }
            synchronized(lock) { lock.wait() }
        }
    }
    private var job: Job? = null

    init {
        contentView = textView
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    fun show(): FpsWindow {
        activity.window.decorView.post {
            showAtLocation(activity.window.decorView, Gravity.TOP or Gravity.START, 0, 0)
        }
        return this
    }

    fun start() {
        job?.cancel()
        job = GlobalScope.launch {
            while (isActive) {
                val last = VsyncTick.count
                delay(1000)
                val fpsText = "FPS: ${VsyncTick.count - last}"
                textView.post {
                    textView.text = fpsText
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

}