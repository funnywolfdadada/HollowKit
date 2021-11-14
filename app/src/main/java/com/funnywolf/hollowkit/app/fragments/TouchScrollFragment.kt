package com.funnywolf.hollowkit.app.fragments

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.funnywolf.hollowkit.app.R
import com.funnywolf.hollowkit.app.utils.toast
import com.funnywolf.hollowkit.view.constrains
import com.funnywolf.hollowkit.view.scroll.TouchScrollHelper

/**
 * @author https://github.com/funnywolfdadada
 * @since 2021/11/14
 */
class TouchScrollFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TouchScrollView(inflater.context).apply {
            val image = ImageView(context)
            image.setBackgroundColor(0x70FF0000)
            image.scaleType = ImageView.ScaleType.CENTER_CROP
            image.setImageResource(R.drawable.bing19)
//            addView(image, FrameLayout.LayoutParams(8000, 4000))
            val text = TextView(context)
            text.textSize = 32F
            text.text = StringBuilder().also { sb ->
                repeat(2000) { sb.append(it) }
            }
            text.setOnClickListener {
                context.toast("Reset!")
                resetScroll()
            }
            addView(text, FrameLayout.LayoutParams(8000, 4000))
        }
    }

    class TouchScrollView(context: Context): FrameLayout(context), TouchScrollHelper.ScrollHandler {
        private val helper = TouchScrollHelper(context, ViewCompat.SCROLL_AXIS_HORIZONTAL or ViewCompat.SCROLL_AXIS_VERTICAL, this)
        private val scrollBounds = Rect()

        fun resetScroll() {
            helper.smoothScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL or ViewCompat.SCROLL_AXIS_VERTICAL, -scrollX, -scrollY)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            adjustScrollBounds()
        }

        /**
         * 调整默认的滚动边界
         */
        private fun adjustScrollBounds() {
            var l = 0; var r = 0; var t = 0; var b = 0
            for (i in 0 until childCount) {
                val c = getChildAt(i)
                if (c.left < l) {
                    l = c.left
                }
                if (c.right > r) {
                    r = c.right
                }
                if (c.top < t) {
                    t = c.top
                }
                if (c.bottom > b) {
                    b = c.bottom
                }
            }
            scrollBounds.set(l, t, r - width, b - height)
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            return helper.onInterceptTouchEvent(ev)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            return helper.onTouchEvent(event)
        }

        override fun scrollTo(x: Int, y: Int) {
            super.scrollTo(x.constrains(scrollBounds.left, scrollBounds.right), y.constrains(scrollBounds.top, scrollBounds.bottom))
        }

        override fun onDown(e: MotionEvent, state: Int) {
            Log.d(TAG, "onDown $state")
        }

        override fun onMove(e: MotionEvent, state: Int) {
            Log.d(TAG, "onMove $state")
        }

        override fun onStartScroll(state: Int, axis: Int) {
            Log.d(TAG, "onStartScroll $state, $axis")
        }

        override fun onScroll(state: Int, axis: Int, sx: Int, sy: Int): Boolean {
            Log.d(TAG, "onScroll $state, $axis: sx=$sx, sy=$sy")
            var consumed = false
            if (axis and ViewCompat.SCROLL_AXIS_HORIZONTAL != 0) {
                val old = scrollX
                scrollBy(sx, 0)
                consumed = consumed || sx == 0 || scrollX != old
                Log.d(TAG, "onScrollX $consumed $sx $old->$scrollX")
            }
            if (axis and ViewCompat.SCROLL_AXIS_VERTICAL != 0) {
                val old = scrollY
                scrollBy(0, sy)
                consumed = consumed || sy == 0 || scrollY != old
            }
            return consumed
        }

        override fun onStopScroll(state: Int, axis: Int) {
            Log.d(TAG, "onStopScroll $state, $axis")
        }

        override fun onUp(e: MotionEvent, state: Int, axis: Int, vx: Int, vy: Int) {
            Log.d(TAG, "onUp state=$state, axis=$axis, vx=$vx, vy=$vy")
            helper.fling(axis, vx, vy)
        }

        override fun onCancel(e: MotionEvent, state: Int, axis: Int) {
            Log.d(TAG, "onCancel state=$state, axis=$axis")
        }
    }

    companion object {
        private const val TAG = "TouchScroll"
    }
}