package com.funnywolf.hollowkit.app.fragments.behavior

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.funnywolf.hollowkit.app.databinding.PageSecondFloorBinding
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.app.utils.initPictures
import com.funnywolf.hollowkit.recyclerview.BoundaryCallback
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.inStablePosition
import com.funnywolf.hollowkit.view.scroll.behavior.isScrollChildTotalShowing

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/28
 */
class SecondFloorFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ViewGroup {
        val binding = PageSecondFloorBinding.inflate(inflater, container, false)
        val rv1 = binding.rv1
        val rv2 = binding.rv2

        rv2.overScrollMode = View.OVER_SCROLL_NEVER
        BoundaryCallback(rv2)
            .onReachStart { Log.d("BoundaryCallback", "onReachStart") }
            .onReachEnd { Log.d("BoundaryCallback", "onReachEnd") }

        rv1.initPictures(true)
        rv2.initPictures()

        val tips = binding.tips
        binding.secondFloor.apply {
            minRefreshHeight = 50.dp
            refreshHeight = 100.dp
            onOpenStateChanged = {
                tips.text = when (openState) {
                    PreRefreshing -> "下拉刷新"
                    CanRefreshing -> "松开刷新"
                    Refreshing -> {
                        postDelayed({
                            isRefreshing = false
                            rv1.initPictures(true)
                            rv2.initPictures()
                        }, 2000)
                        "刷新中"
                    }
                    CanOpening -> "下拉二楼"
                    Opening -> "点击关闭"
                    else -> tips.text
                }
            }
            tips.setOnClickListener {
                openState = Closed
            }
        }
        return binding.root
    }

}

class SecondFloorLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BehavioralScrollView(context, attrs, defStyleAttr) {

    val None = 0
    /**
     * 二楼关闭状态
     */
    val Closed = 1
    /**
     * 刷新前状态
     */
    val PreRefreshing = 2
    /**
     * 可刷新状态
     */
    val CanRefreshing = 3
    /**
     * 刷新中状态
     */
    val Refreshing = 4
    /**
     * 可打开二楼状态
     */
    val CanOpening = 5
    /**
     * 二楼打开状态
     */
    val Opening = 6

    var refreshHeight: Int = 0
    var minRefreshHeight: Int = 0

    private var _openState: Int = None
    var openState: Int
        get() = _openState
        set(value) {
            when(value) {
                Refreshing -> smoothScrollTo(-refreshHeight)
                Opening -> smoothScrollTo(minScroll)
                Closed -> smoothScrollTo(0)
            }
        }

    var onOpenStateChanged: ((Int)->Unit)? = null

    var isRefreshing: Boolean
        get() = openState == Refreshing
        set(value) {
            if (value) {
                if (openState == CanRefreshing) {
                    openState = Refreshing
                }
            } else {
                if (openState == Refreshing) {
                    openState = Closed
                }
            }
        }

    override fun handleDispatchTouchEvent(e: MotionEvent): Boolean? {
        if ((e.action == MotionEvent.ACTION_CANCEL || e.action == MotionEvent.ACTION_UP)
            && !inStablePosition()) {
            onTouchRelease()
            return true
        }
        return super.handleDispatchTouchEvent(e)
    }

    private fun onTouchRelease() {
        val sy = scrollY
        if (sy < 0 && sy != minScroll) {
            openState = when {
                lastScrollDir > 0 -> Closed
                sy > -minRefreshHeight -> Closed
                sy > -refreshHeight -> Refreshing
                else -> Opening
            }
        } else if (sy > 0) {
            openState = Closed
        }
    }

    override fun handleNestedPreScrollFirst(scroll: Int, type: Int): Boolean? {
        return if (inStablePosition() && isScrollChildTotalShowing()) {
            null
        } else {
            false
        }
    }

    override fun handleNestedScrollFirst(scroll: Int, type: Int): Boolean? {
        return true
    }

    override fun handleScrollSelf(scroll: Int, type: Int): Boolean? {
        return when (type) {
            ViewCompat.TYPE_NON_TOUCH -> false
            ViewCompat.TYPE_TOUCH -> {
                val r = if (scrollY > 0) {
                    if (scroll > 0) { 0.5F } else { 1F }
                } else {
                    if (scroll > 0) { 1F } else { 0.5F }
                }
                scrollBy(0, (scroll * r).toInt())
                true
            }
            else -> null
        }
    }

    private fun getCurrentOpenState() = when {
        scrollY == 0 -> Closed
        scrollY < 0 && scrollY > -minRefreshHeight -> PreRefreshing
        scrollY <= -minRefreshHeight && scrollY > -refreshHeight -> CanRefreshing
        scrollY == -refreshHeight -> Refreshing
        scrollY < -refreshHeight && scrollY > minScroll -> CanOpening
        scrollY == minScroll -> Opening
        else -> None
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val current = getCurrentOpenState()
        if (_openState != current) {
            _openState = current
            onOpenStateChanged?.invoke(current)
        }
    }
}
