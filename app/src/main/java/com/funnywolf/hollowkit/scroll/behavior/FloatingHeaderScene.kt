package com.funnywolf.hollowkit.scroll.behavior

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.utils.simpleInit
import com.funnywolf.hollowkit.utils.sp
import com.funnywolf.hollowkit.utils.westWorldHolderBackgroundColor
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.FloatingHeaderBehavior

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/30
 */
class FloatingHeaderScene: UserVisibleHintGroupScene() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        val context = inflater.context
        val height = context.resources.getDimension(R.dimen.toolbar_height).toInt()
        val floatingHeader = TextView(context).apply {
            text = "HEADER"
            gravity = Gravity.CENTER
            textSize = 16.sp.toFloat()
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        }
        val rv = RecyclerView(context).apply {
            simpleInit(55, westWorldHolderBackgroundColor)
        }
        return BehavioralScrollView(context).apply {
            setupBehavior(FloatingHeaderBehavior(rv, floatingHeader))
        }
    }

}