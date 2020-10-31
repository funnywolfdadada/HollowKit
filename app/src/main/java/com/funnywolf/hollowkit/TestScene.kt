package com.funnywolf.hollowkit

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.view.StatefulLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/9
 */
class TestScene: Scene() {

    @Volatile
    var state = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.scene_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sl = view.findViewById<StatefulLayout>(R.id.stateful)
        sl.viewArray.put(1, View(view.context).apply {
            setBackgroundColor(Color.RED)
            layoutParams = FrameLayout.LayoutParams(200.dp, 100.dp, Gravity.CENTER)
        })
        sl.viewArray.put(2, View(view.context).apply {
            setBackgroundColor(0x4000FF00)
            layoutParams = FrameLayout.LayoutParams(100.dp, 200.dp, Gravity.CENTER)
        })

        val btn = view.findViewById<Button>(R.id.next)
        btn.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val s = state++ % 3
                btn.text = s.toString()
                sl.state = s
            }
        }
    }

}
