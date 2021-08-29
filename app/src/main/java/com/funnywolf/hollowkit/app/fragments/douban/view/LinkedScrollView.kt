package com.funnywolf.hollowkit.app.fragments.douban.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.FrameLayout
import android.widget.Scroller
import com.funnywolf.hollowkit.view.findChildUnder
import com.funnywolf.hollowkit.view.findScrollableTarget
import kotlin.math.abs

/**
 * 实现顶部视图和底部视图联动滑动的 view
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/30
 */
class LinkedScrollView: FrameLayout {

    /**
     * 顶部视图容器
     */
    val topContainer: FrameLayout = FrameLayout(context)

    /**
     * 顶部视图中的可滚动 view，当自身无法再向下滚动时，会把剩余的滚动分发给它
     */
    private var topScrollableView: (()->View?)? = null

    /**
     * 底部视图容器
     */
    val bottomContainer: FrameLayout = FrameLayout(context)

    /**
     * 底部视图中的可滚动 view，当自身无法再向上滚动时，会把剩余的滚动分发给它
     */
    private var bottomScrollableView: (()->View?)? = null

    /**
     * y 轴的最大滚动范围 = 顶部视图高度 + 底部视图高度 - 自身的高度
     */
    private var maxScrollY = 0

    /**
     * 上次触摸事件的 x 值，用于判断是否拦截事件
     */
    private var lastX = 0F

    /**
     * 上次触摸事件的 y 值，用于处理自身的滑动事件
     */
    private var lastY = 0F

    /**
     * 主要用于计算 fling 后的滚动距离
     */
    private val scroller = Scroller(context)

    /**
     * 用于计算自身时的 y 轴速度，处理自身的 fling
     */
    private val velocityTracker = VelocityTracker.obtain()

    /**
     * fling 是一连串连续的滚动操作，这里需要暂存 fling 相关的 view
     */
    private var flingChild: View? = null
    private var flingTarget: View? = null

    /**
     * 暂存 fling 时上次的 y 轴位置，用以计算当前需要滚动的距离
     */
    private var lastFlingY = 0

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        addView(topContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(bottomContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun setTopView(v: View, scrollableChild: (()->View?)? = null) {
        topContainer.removeAllViews()
        topContainer.addView(v)
        topScrollableView = scrollableChild
        requestLayout()
    }

    fun removeTopView() {
        topContainer.removeAllViews()
        topScrollableView = null
    }

    fun setBottomView(v: View, scrollableChild: (()->View?)? = null) {
        bottomContainer.removeAllViews()
        bottomContainer.addView(v)
        bottomScrollableView = scrollableChild
        requestLayout()
    }

    fun removeBottomView() {
        bottomContainer.removeAllViews()
        bottomScrollableView = null
    }

    /**
     * 布局时，topContainer 在顶部，bottomContainer 紧挨着 topContainer 底部
     * 布局完还要计算下最大的滚动距离
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        topContainer.layout(0, 0, topContainer.measuredWidth, topContainer.measuredHeight)
        bottomContainer.layout(0, topContainer.measuredHeight, bottomContainer.measuredWidth,
            topContainer.measuredHeight + bottomContainer.measuredHeight)
        maxScrollY = topContainer.measuredHeight + bottomContainer.measuredHeight - height
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            // 手指按下就中止 fling 等滑动行为
            scroller.forceFinished(true)
        }
        return super.dispatchTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = e.x
                lastY = e.y
                false
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(lastX - e.x) < abs(lastY - e.y)) {
                    true
                } else {
                    lastX = e.x
                    lastY = e.y
                    false
                }
            }
            else -> super.onInterceptTouchEvent(e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                // 手指按下时记录 y 轴初始位置
                lastY = e.y
                velocityTracker.clear()
                velocityTracker.addMovement(e)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                // 移动时分发滚动量
                val dScrollY = (lastY - e.y).toInt()
                val child = findChildUnder(e.rawX, e.rawY)
                dispatchScrollY(dScrollY, child, child?.findScrollableTarget(e.rawX, e.rawY, dScrollY))
                lastY = e.y
                velocityTracker.addMovement(e)
                true
            }
            MotionEvent.ACTION_UP -> {
                // 手指抬起时计算 y 轴速度，然后自身处理 fling
                velocityTracker.addMovement(e)
                velocityTracker.computeCurrentVelocity(1000)
                val yv = -velocityTracker.yVelocity.toInt()
                val child = findChildUnder(e.rawX, e.rawY)
                handleFling(yv, child, child?.findScrollableTarget(e.rawX, e.rawY, yv))
                true
            }
            else -> super.onTouchEvent(e)
        }
    }

    /**
     * 处理 fling，通过 scroller 计算 fling，暂存 fling 的初值和需要 fling 的 view
     */
    private fun handleFling(yv: Int, child: View?, target: View?) {
        lastFlingY = 0
        scroller.fling(0, lastFlingY, 0, yv, 0, 0, Int.MIN_VALUE, Int.MAX_VALUE)
        flingChild = child
        flingTarget = target
        invalidate()
    }

    /**
     * 计算 fling 的滚动量，并将其分发到真正需要处理的 view
     */
    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            val currentFlingY = scroller.currY
            val dScrollY = currentFlingY - lastFlingY
            dispatchScrollY(dScrollY, flingChild, flingTarget)
            lastFlingY = currentFlingY
            invalidate()
        } else {
            flingChild = null
        }
    }

    private fun dispatchScrollY(dScrollY: Int, child: View?, target: View?) {
        if (dScrollY == 0) {
            return
        }
        // 滚动所处的位置没有在子 view，或者子 view 没有完全显示出来
        // 或者子 view 中没有要处理滚动的 target，或者 target 不在能够滚动
        if (child == null || !isChildTotallyShowing(child)
            || target == null || !target.canScrollVertically(dScrollY)) {
            // 优先自己处理，处理不了再根据滚动方向交给顶部或底部的 view 处理
            when {
                canScrollVertically(dScrollY) -> scrollBy(0, dScrollY)
                dScrollY > 0 -> bottomScrollableView?.invoke()?.scrollBy(0, dScrollY)
                else -> topScrollableView?.invoke()?.scrollBy(0, dScrollY)
            }
        } else {
            target.scrollBy(0, dScrollY)
        }
    }

    private fun isChildTotallyShowing(c: View): Boolean {
        val relativeY = c.y - scrollY
        return relativeY >= 0 && relativeY + c.height <= height
    }

    /**
     * 滚动范围是[0, [maxScrollY]]，根据方向判断垂直方向是否可以滚动
     */
    override fun canScrollVertically(direction: Int): Boolean {
        return if (direction > 0) {
            scrollY < maxScrollY
        } else {
            scrollY > 0
        }
    }

    /**
     * 滚动前做范围限制
     */
    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, when {
            y < 0 -> 0
            y > maxScrollY -> maxScrollY
            else -> y
        })
    }

}
