package com.funnywolf.hollowkit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Scroller
import androidx.annotation.FloatRange
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat

/**
 * 底部弹出式的 view 容器
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/4
 */

/**
 * 折叠状态，此时只露出最小显示高度
 */
const val BOTTOM_SHEET_STATE_COLLAPSED = 1
/**
 * 正在滚动的状态
 */
const val BOTTOM_SHEET_STATE_SCROLLING = 2
/**
 * 展开状态，此时露出全部内容
 */
const val BOTTOM_SHEET_STATE_EXTENDED = 3

class BottomSheetLayout: FrameLayout, NestedScrollingParent2 {

    /**
     * 内容视图的状态
     */
    var state = 0
        get() = when (scrollY) {
            minScrollY -> BOTTOM_SHEET_STATE_COLLAPSED
            maxScrollY -> BOTTOM_SHEET_STATE_EXTENDED
            else -> BOTTOM_SHEET_STATE_SCROLLING
        }
        private set

    /**
     * 当前滚动的进度，[BOTTOM_SHEET_STATE_COLLAPSED] 时是 0，[BOTTOM_SHEET_STATE_EXTENDED] 时是 1
     */
    @FloatRange(from = 0.0, to = 1.0)
    var process = 0F
        get() = if (maxScrollY > minScrollY) {
            (scrollY - minScrollY).toFloat() / (maxScrollY - minScrollY)
        } else {
            0F
        }
        private set

    /**
     * 上一次发生滚动时的滚动方向，用于在松手时判断需要滚动到的位置
     */
    var lastDir = 0
        private set

    /**
     * 当 [process] 发生变化时的回调
     */
    var onProcessChangedListener: ((BottomSheetLayout)->Unit)? = null

    /**
     * 松开回弹时的回调，返回是否拦截该事件
     */
    var onReleaseListener: ((BottomSheetLayout)->Boolean)? = null

    /**
     * 内容视图
     */
    var contentView: View? = null
        private set

    /**
     * 内容视图最小的显示高度
     */
    private var minShowingHeight = 0

    /**
     * 添加内容视图时的初始状态
     */
    private var initState: Int = BOTTOM_SHEET_STATE_COLLAPSED

    /**
     * y 轴最小的滚动值，此时 [contentView] 在底部露出 [minShowingHeight]
     */
    private var minScrollY = 0

    /**
     * y 轴最大的滚动值，此时 [contentView] 全部露出
     */
    private var maxScrollY = 0

    /**
     * 上次触摸事件的 y 值，用于处理自身的滑动事件
     */
    private var lastY = 0F

    /**
     * 用来处理平滑滚动
     */
    private val scroller = Scroller(context)

    private val parentHelper = NestedScrollingParentHelper(this)

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    fun setContentView(
        contentView: View,
        minShowingHeight: Int,
        initState: Int = BOTTOM_SHEET_STATE_COLLAPSED
    ) {
        removeAllViews()
        this.contentView = contentView
        this.minShowingHeight = if (minShowingHeight < 0) { 0 } else { minShowingHeight }
        this.initState = initState
        addView(contentView)
    }

    fun removeContentView() {
        removeAllViews()
        state = 0
        contentView = null
        minShowingHeight = 0
        initState = 0
        minScrollY = 0
        maxScrollY = 0
    }

    fun setProcess(@FloatRange(from = 0.0, to = 1.0) process: Float, smoothly: Boolean = true) {
        val y = ((maxScrollY - minScrollY) * process + minScrollY).toInt()
        if (smoothly) {
            smoothScrollToY(y)
        } else {
            scrollTo(0, y)
        }
    }

    /**
     * 布局正常布局，布局完成后根据 [minShowingHeight] 和 [contentView] 的位置计算滚动范围，并滚动到初始状态的位置
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        minScrollY = 0
        maxScrollY = 0
        contentView?.also {
            if (minShowingHeight > it.height) {
                minShowingHeight = it.height
            }
            minScrollY = it.top + minShowingHeight - height
            maxScrollY = it.bottom - height
            if (initState == BOTTOM_SHEET_STATE_EXTENDED) {
                setProcess(1F, false)
            } else {
                setProcess(0F, false)
            }
        }
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            // down 时复位上次的滚动方向
            MotionEvent.ACTION_DOWN -> lastDir = 0
            // up 或 cancel 时平滑滚动到目标位置
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 发生了移动，且处于滚动中的状态，且未被拦截，则自己处理
                if (lastDir != 0
                    && state == BOTTOM_SHEET_STATE_SCROLLING
                    && onReleaseListener?.invoke(this) != true) {
                    smoothScrollToY(if (lastDir > 0) { maxScrollY } else { minScrollY })
                    // 这里返回 true 防止分发给子 view 导致其抖动
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        // 不拦截 down，发生了移动，且不是在展开状态，就拦截事件
        if (e.action != MotionEvent.ACTION_DOWN
            && lastY != e.y
            && state != BOTTOM_SHEET_STATE_EXTENDED
        ) {
            return true
        }
        lastY = e.y
        return super.onInterceptTouchEvent(e)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            // down 在内容视图上时才做处理，由于 y 轴会发生滚动，所以这里还要对 y 轴进行滚动补偿
            MotionEvent.ACTION_DOWN -> {
                lastY = e.y
                contentView?.let { v ->
                    e.x in v.x..(v.x + v.width) && (e.y + scrollY) in v.y..(v.y + v.height)
                } ?: false
            }
            // move 时如果可以自己可以滚动就处理
            MotionEvent.ACTION_MOVE -> {
                val dy = (lastY - e.y).toInt()
                lastY = e.y
                if (canScrollVertically(dy)) {
                    scrollBy(0, dy)
                    true
                } else {
                    false
                }
            }
            else -> super.onTouchEvent(e)
        }
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        // 只处理垂直方向
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        // 不是在展开状态，就拦截滚动
        if (state != BOTTOM_SHEET_STATE_EXTENDED) {
            consumed[1] = dy
            scrollBy(0, dy)
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        // 对于 target 未消耗的滚动，我们只处理 touch 造成的滚动，不处理 fling
        // 因为展开状态时，向上滚动的 fling 可能会把内容带着往上滚动
        if (type == ViewCompat.TYPE_TOUCH) {
            scrollBy(0, dyUnconsumed)
        }
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
    }

    /**
     * 滚动范围是[[minScrollY], [maxScrollY]]，根据方向判断垂直方向是否可以滚动
     */
    override fun canScrollVertically(direction: Int): Boolean {
        return if (direction > 0) {
            scrollY < maxScrollY
        } else {
            scrollY > minScrollY
        }
    }

    /**
     * 滚动前做范围限制
     */
    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, when {
            y < minScrollY -> minScrollY
            y > maxScrollY -> maxScrollY
            else -> y
        })
    }

    /**
     * 利用 [scroller] 平滑滚动到目标位置
     */
    private fun smoothScrollToY(y: Int) {
        if (scrollY == y) {
            return
        }
        scroller.startScroll(0, scrollY, 0, y - scrollY)
        invalidate()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(0, scroller.currY)
            invalidate()
        }
    }

    /**
     * 当发生滚动时，更新滚动方向和当前内容视图的状态
     */
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        lastDir = t - oldt
        onProcessChangedListener?.invoke(this)
    }

}