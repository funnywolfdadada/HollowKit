package com.funnywolf.hollowkit

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.utils.simpleInit

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
        val rv = RecyclerView(context).apply {
            simpleInit()
        }
        val content = TestLayout(context)
        return content.apply {
            addView(rv)
        }
    }

}

class TestLayout(context: Context): FrameLayout(context), NestedScrollingParent3 {

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {

    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        Log.d(javaClass.simpleName, "onNestedPreScroll: $target $dy $type")
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        Log.d(javaClass.simpleName, "onNestedScroll: $target $dyConsumed $dyUnconsumed $type")
        consumed[1] = dyUnconsumed
        scrollBy(0, dyUnconsumed)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        Log.d(javaClass.simpleName, "onNestedScroll: $target $dyConsumed $dyUnconsumed $type")
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        Log.d(javaClass.simpleName, "onNestedPreFling: $target $velocityY")
        return super.onNestedPreFling(target, velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        Log.d(javaClass.simpleName, "onNestedFling: $target $velocityY $consumed")
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        Log.d(javaClass.simpleName, "onStopNestedScroll: $target $type")
    }

}