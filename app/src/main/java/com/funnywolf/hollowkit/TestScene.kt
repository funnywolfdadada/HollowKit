package com.funnywolf.hollowkit

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.drawable.LinearGradientProvider
import com.funnywolf.hollowkit.drawable.RoundRectDrawable
import com.funnywolf.hollowkit.utils.*

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/9
 */
class TestScene: Scene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        return ScrollView(context).apply {
            val linearLayout = LinearLayout(context).apply {
                setBackgroundColor(0xFF000000.toInt())
                orientation = LinearLayout.VERTICAL
                addView(View(context).apply {
                    background = RoundRectDrawable(0x80FFFFFF.toInt(), 50.dp, inverse = false).apply {
                        ringColor = 0xFFFFFFFF.toInt()
                        ringSize = 40.dp
                    }
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 200.dp))
                addView(View(context).apply {
                    background = RoundRectDrawable(0xFF00FFFF.toInt(), 50.dp, inverse = true).apply {
                        ringColor = 0xFFFFFFFF.toInt()
                        ringSize = 10.dp
                    }
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 200.dp))
                addView(View(context).apply {
                    background = RoundRectDrawable(0xFFFFFFFF.toInt(), 100.dp, inverse = false).apply {
                        fillShaderProvider = LinearGradientProvider(GradientDrawable.Orientation.RIGHT_LEFT, intArrayOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt()))
                        ringColor = 0xFFFFFFFF.toInt()
                        ringSize = 50.dp
                        ringShaderProvider = LinearGradientProvider(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt()))
                    }
                }, ViewGroup.LayoutParams(200.dp, 200.dp))
                addView(View(context).apply {
                    background = RoundRectDrawable(0xFFFFFFFF.toInt(), 100.dp, inverse = true).apply {
                        ringColor = 0xFFFFFFFF.toInt()
                        ringSize = 0.dp
                        fillShaderProvider = LinearGradientProvider(GradientDrawable.Orientation.BR_TL, intArrayOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt()))
                    }
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 200.dp))
                addView(View(context).apply {
                    background = RoundRectDrawable(0xFFFFFFFF.toInt(), 20.dp, inverse = false).apply {
                        ringColor = 0xFFFFFFFF.toInt()
                        ringSize = 10.dp
                        fillShaderProvider = LinearGradientProvider(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt()))
                    }
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 200.dp))
                addView(View(context).apply {
                    background = RoundRectDrawable(0xFFFFFFFF.toInt(), 20.dp, inverse = false).apply {
                        ringColor = 0xFFFFFFFF.toInt()
                        ringSize = 10.dp
                        fillShaderProvider = LinearGradientProvider(GradientDrawable.Orientation.BL_TR, intArrayOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt()))
                    }
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 200.dp))
                addView(View(context).apply {
                    background = RoundRectDrawable(0xFFFFFFFF.toInt(), 20.dp, inverse = false).apply {
                        ringColor = 0xFFFFFFFF.toInt()
                        ringSize = 10.dp
                        fillShaderProvider = LinearGradientProvider(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt()))
                    }
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 200.dp))
                addView(View(context).apply {
                    background = RoundRectDrawable(0xFFFFFFFF.toInt(), 20.dp, inverse = false).apply {
                        ringColor = 0xFFFFFFFF.toInt()
                        ringSize = 10.dp
                        fillShaderProvider = LinearGradientProvider(GradientDrawable.Orientation.TL_BR, intArrayOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt()))
                    }
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 200.dp))
            }
            addView(linearLayout)
        }
    }

}
