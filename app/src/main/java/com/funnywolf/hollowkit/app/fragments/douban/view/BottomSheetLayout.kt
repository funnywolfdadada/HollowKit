package com.funnywolf.hollowkit.app.fragments.douban.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.FrameLayout
import android.widget.Scroller
import androidx.annotation.FloatRange
import com.funnywolf.hollowkit.view.findScrollableTarget
import com.funnywolf.hollowkit.view.isUnder
import kotlin.math.abs

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

class BottomSheetLayout: FrameLayout {

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
    private var initState: Int =
        BOTTOM_SHEET_STATE_COLLAPSED

    /**
     * y 轴最小的滚动值，此时 [contentView] 在底部露出 [minShowingHeight]
     */
    private var minScrollY = 0

    /**
     * y 轴最大的滚动值，此时 [contentView] 全部露出
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
     * 用来处理平滑滚动
     */
    private val scroller = Scroller(context)

    /**
     * 用于计算自身时的 y 轴速度，处理自身的 fling
     */
    private val velocityTracker = VelocityTracker.obtain()

    /**
     * fling 是一连串连续的滚动操作，这里需要暂存 fling 的目标 view
     */
    private var flingTarget: View? = null

    /**
     * 暂存 scroller 上次计算的 y 轴滚动位置，用以计算当前需要滚动的距离
     */
    private var lastComputeY = 0

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
            // down 时，记录触点位置，复位上次的滚动方向，停掉动画
            MotionEvent.ACTION_DOWN -> {
                lastX = e.x
                lastY = e.y
                lastDir = 0
                if (isFling()) {
                    scroller.abortAnimation()
                }
            }
            // up 或 cancel 时判断是否要平滑滚动到稳定位置
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
        // 正在滚动中肯定要自己拦截处理
        if (state == BOTTOM_SHEET_STATE_SCROLLING) {
            return true
        }
        // move 时，在内容 view 区域，且 y 轴偏移更大，就拦截
        return if (e.action == MotionEvent.ACTION_MOVE) {
            contentView?.isUnder(e.rawX, e.rawY) == true && abs(lastX - e.x) < abs(lastY - e.y)
        } else {
            lastX = e.x
            lastY = e.y
            super.onInterceptTouchEvent(e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            // down 时，触点在内容视图上时才继续处理
            MotionEvent.ACTION_DOWN -> {
                velocityTracker.clear()
                velocityTracker.addMovement(e)
                contentView?.isUnder(e.rawX, e.rawY) == true
            }
            // move 时分发滚动量
            MotionEvent.ACTION_MOVE -> {
                velocityTracker.addMovement(e)
                val dy = (lastY - e.y).toInt()
                lastY = e.y
                dispatchScrollY(dy, contentView?.findScrollableTarget(e.rawX, e.rawY, dy))
            }
            // up 时要处理子 view 的 fling
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker.addMovement(e)
                velocityTracker.computeCurrentVelocity(1000)
                val yv = -velocityTracker.yVelocity.toInt()
                handleFling(yv, contentView?.findScrollableTarget(e.rawX, e.rawY, yv))
                true
            }
            else -> super.onTouchEvent(e)
        }
    }

    /**
     * fling 只用于目标 view 的滚动，不用于自身滚动
     */
    private fun handleFling(yv: Int, target: View?) {
        target ?: return
        lastComputeY = 0
        flingTarget = target
        scroller.fling(0, lastComputeY, 0, yv, 0, 0, Int.MIN_VALUE, Int.MAX_VALUE)
        invalidate()
    }

    /**
     * 利用 [scroller] 平滑滚动到目标位置，只用于自身的滚动
     */
    private fun smoothScrollToY(y: Int) {
        if (scrollY == y) {
            return
        }
        lastComputeY = scrollY
        flingTarget = null
        scroller.startScroll(0, scrollY, 0, y - scrollY)
        invalidate()
    }

    /**
     * 是否是 fling 只取决于有没有要 fling 的目标 view
     */
    private fun isFling(): Boolean = flingTarget != null

    /**
     * 计算 [scroller] 当前的滚动量并分发，不再处理就关掉动画
     * 动画结束时及时复位 fling 的目标 view
     */
    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            val currentY = scroller.currY
            val dScrollY = currentY - lastComputeY
            lastComputeY = currentY
            if (!dispatchScrollY(dScrollY, flingTarget)) {
                scroller.abortAnimation()
            }
            invalidate()
        } else {
            flingTarget = null
        }
    }

    /**
     * 分发 y 轴滚动事件
     * 展开状态：优先处理 [target]，然后如果不是 fling （fling 不用于自身的滚动）才处理自己
     * 非展开状态：只处理自己
     *
     * @param dScrollY y 轴的滚动量
     * @param target 可以处理改滚动量的目标 view
     * @return 是否可以处理
     */
    private fun dispatchScrollY(dScrollY: Int, target: View?): Boolean {
        // 0 默认可以处理
        if (dScrollY == 0) {
            return true
        }
        return if (state == BOTTOM_SHEET_STATE_EXTENDED) {
            if (target != null && target.canScrollVertically(dScrollY)) {
                target.scrollBy(0, dScrollY)
                true
            } else if (!isFling() && canScrollVertically(dScrollY)) {
                scrollBy(0, dScrollY)
                true
            } else {
                false
            }
        } else if (canScrollVertically(dScrollY)) {
            scrollBy(0, dScrollY)
            true
        } else {
            false
        }
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
     * 当发生滚动时，更新滚动方向和当前内容视图的状态
     */
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        lastDir = t - oldt
        onProcessChangedListener?.invoke(this)
    }

}