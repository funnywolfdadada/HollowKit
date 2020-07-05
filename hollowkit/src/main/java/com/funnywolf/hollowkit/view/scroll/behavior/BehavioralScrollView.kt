package com.funnywolf.hollowkit.view.scroll.behavior

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.FrameLayout
import android.widget.Scroller
import androidx.annotation.IntDef
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import com.funnywolf.hollowkit.utils.findHorizontalNestedScrollingTarget
import com.funnywolf.hollowkit.utils.findVerticalNestedScrollingTarget
import kotlin.math.abs

/**
 * 嵌套滑动布局
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/8
 */
open class BehavioralScrollView : FrameLayout, NestedScrollingParent3 {

    /**
     * 当前的可滚动方向
     * true -> 垂直滚动
     * false -> 水平滚动
     * null -> 不可滚动
     */
    var scrollAxis: Int = ViewCompat.SCROLL_AXIS_NONE
        private set

    /**
     * 滚动的最小值
     */
    var minScroll = 0
        private set

    /**
     * 滚动的最大值
     */
    var maxScroll = 0
        private set

    /**
     * 上次滚动的方向
     */
    var lastScrollDir = 0
        private set

    /**
     * 当前的滚动状态
     */
    @NestedScrollState
    var state: Int = NestedScrollState.NONE
        private set(value) {
            log("NestedScrollState $field -> $value")
            field = value
        }

    var enableLog = false

    /**
     * 发生滚动时的回调
     */
    var onScrollChangedListeners = HashSet<((BehavioralScrollView)->Unit)>()

    private var behavior: NestedScrollBehavior? = null
    private val children = arrayOfNulls<View>(3)

    private var target: View? = null

    /**
     * 上次触摸事件的 x 值，或者 scroller.currX，用于处理自身的滑动事件或动画
     */
    private var lastX = 0F
    /**
     * 上次触摸事件的 y 值，或者 scroller.currY，用于处理自身的滑动事件或动画
     */
    private var lastY = 0F

    /**
     * 用来处理松手时的连续滚动
     */
    private val scroller = Scroller(context)

    /**
     * 用于计算抬手时的速度
     */
    private val velocityTracker by lazy { VelocityTracker.obtain() }

    private val parentHelper by lazy { NestedScrollingParentHelper(this) }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    fun setupBehavior(behavior: NestedScrollBehavior?) {
        removeAllViews()
        this.behavior = behavior
        scrollAxis = behavior?.scrollAxis ?: ViewCompat.SCROLL_AXIS_NONE
        children[0] = behavior?.prevView?.also { addView(it) }
        children[1] = behavior?.midView?.also { addView(it) }
        children[2] = behavior?.nextView?.also { addView(it) }
    }

    fun smoothScrollTo(dest: Int, duration: Int = 300) {
        smoothScrollSelf(dest, duration)
    }

    /**
     * 当前滚动的百分比
     */
    fun currProcess(): Float {
        return when(scrollAxis) {
            ViewCompat.SCROLL_AXIS_VERTICAL -> if (scrollY > 0) {
                if (maxScroll != 0) {
                    scrollY.toFloat() / maxScroll
                } else {
                    0F
                }
            } else {
                if (minScroll != 0) {
                    scrollY.toFloat() / minScroll
                } else {
                    0F
                }
            }
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> if (scrollX > 0) {
                if (maxScroll != 0) {
                    scrollX.toFloat() / maxScroll
                } else {
                    0F
                }
            } else {
                if (minScroll != 0) {
                    scrollX.toFloat() / minScroll
                } else {
                    0F
                }
            }
            else -> 0F
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        minScroll = 0
        maxScroll = 0
        when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_VERTICAL -> layoutVertical()
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> layoutHorizontal()
        }
        behavior?.afterLayout(this)
    }

    private fun layoutVertical() {
        val t = children[1]?.top ?: 0
        val b = children[1]?.bottom ?: 0
        children[0]?.also {
            it.offsetTopAndBottom(t - it.bottom)
            minScroll = it.top
        }
        children[2]?.also {
            it.offsetTopAndBottom(b - it.top)
            maxScroll = it.bottom - height
        }
    }

    private fun layoutHorizontal() {
        val l = children[1]?.left ?: 0
        val r = children[1]?.right ?: 0
        children[0]?.also {
            it.offsetLeftAndRight(l - it.right)
            minScroll = it.left
        }
        children[2]?.also {
            it.offsetLeftAndRight(r - it.left)
            maxScroll = it.right - width
        }
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        val handle = behavior?.handleDispatchTouchEvent(this, e)
        log("handleDispatchTouchEvent $handle")
        if (handle != null) {
            return handle
        }
        // 在 down 时复位一些标志位，停掉 scroller 的动画
        if (e.action == MotionEvent.ACTION_DOWN) {
            lastScrollDir = 0
            state = NestedScrollState.NONE
            scroller.abortAnimation()
        }
        // behavior 优先处理，不处理走默认逻辑
        return super.dispatchTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            // down 时不拦截，但需要记录触点位置，寻找触点位置的 child 和垂直滚动的 target
            MotionEvent.ACTION_DOWN -> {
                lastX = e.x
                lastY = e.y
                // 如果子 view 有重叠的情况，这里记录的 target 并不完全准确，不过这里只做为是否拦截事件的判断
                target = when (scrollAxis) {
                    ViewCompat.SCROLL_AXIS_VERTICAL -> findVerticalNestedScrollingTarget(e.rawX, e.rawY)
                    ViewCompat.SCROLL_AXIS_HORIZONTAL -> findHorizontalNestedScrollingTarget(e.rawX, e.rawY)
                    else -> null
                }
                false
            }
            // move 时如果移动，且没有 target 就自己拦截
            MotionEvent.ACTION_MOVE -> when (scrollAxis) {
                ViewCompat.SCROLL_AXIS_VERTICAL -> if (abs(e.y - lastY) > abs(e.x - lastX) && target == null) {
                    true
                } else {
                    lastY = e.y
                    false
                }
                ViewCompat.SCROLL_AXIS_HORIZONTAL -> if (abs(e.x - lastX) > abs(e.y - lastY) && target == null) {
                    true
                } else {
                    lastX = e.x
                    false
                }
                else -> false
            }
            else -> super.onInterceptTouchEvent(e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        // behavior 优先处理，不处理时自己处理 touch 事件
        val handle = behavior?.handleTouchEvent(this, e)
        log("handleTouchEvent $handle")
        return handle ?: when (e.action) {
                // down 不处理，之后的就无法处理
                MotionEvent.ACTION_DOWN -> {
                    velocityTracker.addMovement(e)
                    lastX = e.x
                    lastY = e.y
                    true
                }
                // move 时计算并分发滚动量
                MotionEvent.ACTION_MOVE -> {
                    velocityTracker.addMovement(e)
                    val dx = (lastX - e.x).toInt()
                    val dy = (lastY - e.y).toInt()
                    lastX = e.x
                    lastY = e.y
                    if (state == NestedScrollState.DRAGGING) {
                        when (scrollAxis) {
                            ViewCompat.SCROLL_AXIS_VERTICAL -> handleScrollSelf(dy, ViewCompat.TYPE_TOUCH) != 0
                            ViewCompat.SCROLL_AXIS_HORIZONTAL -> handleScrollSelf(dx, ViewCompat.TYPE_TOUCH) != 0
                            else -> false
                        }
                    } else {
                        when (scrollAxis) {
                            ViewCompat.SCROLL_AXIS_VERTICAL -> abs(dx) < abs(dy) && handleScrollSelf(dy, ViewCompat.TYPE_TOUCH) != 0
                            ViewCompat.SCROLL_AXIS_HORIZONTAL -> abs(dx) > abs(dy) && handleScrollSelf(dx, ViewCompat.TYPE_TOUCH) != 0
                            else -> false
                        }.also {
                            if (it) {
                                state = NestedScrollState.DRAGGING
                                requestDisallowInterceptTouchEvent(true)
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    velocityTracker.addMovement(e)
                    velocityTracker.computeCurrentVelocity(1000)
                    fling(-velocityTracker.xVelocity, -velocityTracker.yVelocity)
                    velocityTracker.clear()
                    true
                }
                else -> super.onTouchEvent(e)
            }
    }

    override fun getNestedScrollAxes(): Int {
        return super.getNestedScrollAxes()
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_VERTICAL -> (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> (axes and ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0
            else -> false
        }
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_VERTICAL -> consumed[1] = handleNestedPreScroll(dy, type)
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> consumed[0] = handleNestedPreScroll(dx, type)
            else -> return
        }
    }

    private fun handleNestedPreScroll(scroll: Int, type: Int): Int {
        val scrollSelfFirst = behavior?.scrollSelfFirst(this, scroll, type)
        log("scrollSelfFirst $scrollSelfFirst")
        return if (scrollSelfFirst == true) {
            handleScrollSelf(scroll, type)
        } else {
            0
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, IntArray(2))
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
        when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_VERTICAL -> consumed[1] = handleScrollSelf(dyUnconsumed, type)
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> consumed[0] = handleScrollSelf(dxUnconsumed, type)
        }
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        if (!consumed) {
            fling(velocityX, velocityY)
            return true
        }
        return false
    }

    override fun onStopNestedScroll(child: View) {
        onStopNestedScroll(child, ViewCompat.TYPE_TOUCH)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
    }

    private fun fling(vx: Float, vy: Float) {
        log("fling $vx $vy")
        state = NestedScrollState.FLING
        lastX = 0F
        lastY = 0F
        // 这里不区分滚动方向，在分发滚动量的时候会处理
        scroller.abortAnimation()
        scroller.fling(
            lastX.toInt(), lastY.toInt(), vx.toInt(), vy.toInt(),
            Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE
        )
        invalidate()
    }

    private fun smoothScrollSelf(scroll: Int, duration: Int) {
        log("smoothScrollSelf $scroll")
        state = NestedScrollState.ANIMATION
        lastX = 0F
        lastY = 0F
        // 这里不区分滚动方向，在分发滚动量的时候会处理
        scroller.abortAnimation()
        scroller.startScroll(
            lastX.toInt(), lastY.toInt(),
            scroll - scrollX, scroll - scrollY,
            duration
        )
        invalidate()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            val dx = (scroller.currX - lastX).toInt()
            val dy = (scroller.currY - lastY).toInt()
            lastX = scroller.currX.toFloat()
            lastY = scroller.currY.toFloat()
            when (scrollAxis) {
                ViewCompat.SCROLL_AXIS_VERTICAL -> handleScrollSelf(dy, ViewCompat.TYPE_NON_TOUCH)
                ViewCompat.SCROLL_AXIS_HORIZONTAL -> handleScrollSelf(dx, ViewCompat.TYPE_NON_TOUCH)
                else -> scroller.abortAnimation()
            }
            invalidate()
        } else if (state == NestedScrollState.ANIMATION || state == NestedScrollState.FLING) {
            state = NestedScrollState.NONE
        }
    }

    /**
     * 处理自身滚动
     */
    private fun handleScrollSelf(scroll: Int, @ViewCompat.NestedScrollType type: Int): Int {
        // behavior 优先决定滚动多少
        val handle = behavior?.handleScrollSelf(this, scroll, type)
        log("handleScrollSelf $scroll -> $handle")
        when(handle) {
            true -> return scroll
            false -> return 0
        }
        val canSelfScroll = canScrollSelf(scroll)
        log("canScrollSelf $canSelfScroll")
        return if (canSelfScroll) {
            scrollBy(scroll, scroll)
            scroll
        } else {
            0
        }
    }

    /**
     * 根据当前方向判断自身是否可以滚动
     */
    private fun canScrollSelf(dir: Int): Boolean {
        return when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_VERTICAL -> canScrollVertically(dir)
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> canScrollHorizontally(dir)
            else -> false
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return when {
            scrollAxis != ViewCompat.SCROLL_AXIS_VERTICAL -> false
            direction > 0 -> scrollY < maxScroll
            direction < 0 -> scrollY > minScroll
            else -> true
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return when {
            scrollAxis != ViewCompat.SCROLL_AXIS_HORIZONTAL -> false
            direction > 0 -> scrollX < maxScroll
            direction < 0 -> scrollX > minScroll
            else -> true
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        val xx = getScrollByX(x)
        val yy = getScrollByY(y)
        super.scrollBy(xx, yy)
        log("scrollBy $x -> $xx, $y -> $yy")
    }

    /**
     * 根据方向计算 x 轴的真正滚动量
     */
    private fun getScrollByX(dx: Int): Int {
        val newX = scrollX + dx
        return when {
            scrollAxis != ViewCompat.SCROLL_AXIS_HORIZONTAL -> scrollX
            scrollX > 0 -> newX.constrains(0, maxScroll)
            scrollX < 0 -> newX.constrains(minScroll, 0)
            else -> newX.constrains(minScroll, maxScroll)
        } - scrollX
    }

    /**
     * 根据方向计算 y 轴的真正滚动量
     */
    private fun getScrollByY(dy: Int): Int {
        val newY = scrollY + dy
        return when {
            scrollAxis != ViewCompat.SCROLL_AXIS_VERTICAL -> scrollY
            scrollY > 0 -> newY.constrains(0, maxScroll)
            scrollY < 0 -> newY.constrains(minScroll, 0)
            else -> newY.constrains(minScroll, maxScroll)
        } - scrollY
    }

    private fun Int.constrains(min: Int, max: Int): Int = when {
        this < min -> min
        this > max -> max
        else -> this
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        // 更新滚动的方向
        lastScrollDir = when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_VERTICAL -> t - oldt
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> l - oldl
            else -> 0
        }
        onScrollChangedListeners.forEach { it(this) }
    }

    fun log(text: String) {
        if (enableLog) {
            Log.d(javaClass.simpleName, text)
        }
    }
}

/**
 * 用于描述 [BehavioralScrollView] 正处于的嵌套滚动状态，和滚动类型 [ViewCompat.NestedScrollType] 共同描述滚动量
 */
@IntDef(NestedScrollState.NONE, NestedScrollState.DRAGGING, NestedScrollState.ANIMATION, NestedScrollState.FLING)
@Retention(AnnotationRetention.SOURCE)
annotation class NestedScrollState {
    companion object {
        /**
         * 无状态
         */
        const val NONE = 0
        /**
         * 正在拖拽
         */
        const val DRAGGING = 1
        /**
         * 正在动画
         */
        const val ANIMATION = 2
        /**
         * 正在 fling
         */
        const val FLING = 3
    }
}

interface NestedScrollBehavior {
    /**
     * 当前的可滚动方向
     */
    @ViewCompat.ScrollAxis
    val scrollAxis: Int

    val prevView: View?
    val midView: View
    val nextView: View?

    /**
     * 在 layout 之后的回调
     *
     * @param v
     */
    fun afterLayout(v: BehavioralScrollView) {
        // do nothing
    }

    /**
     * 在 [v] dispatchTouchEvent 时是否处理 touch 事件
     *
     * @param v
     * @param e touch 事件
     * @return true -> 处理，会在 dispatchTouchEvent 中直接返回 true，false -> 直接返回 false，null -> 不关心，会执行默认逻辑
     */
    fun handleDispatchTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? = null

    /**
     * 在 [v] onTouchEvent 时是否处理 touch 事件
     *
     * @param v
     * @param e touch 事件
     * @return true -> 处理，会直接返回 true，false -> 不处理，会直接返回 false，null -> 不关心，会执行默认逻辑
     */
    fun handleTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? = null

    /**
     * 在 [v] 分发滚动量时，是否优先滚动自己
     *
     * @param v
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return true -> 优先滚动自己，false -> 不优先
     */
    fun scrollSelfFirst(v: BehavioralScrollView, scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean = false

    /**
     * 在需要 [v] 自身滚动时，是否需要处理
     *
     * @param v
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return 是否处理自身滚动，true -> 处理，false -> 不处理，null -> 不关心，会执行默认自身滚动
     */
    fun handleScrollSelf(v: BehavioralScrollView, scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean? = null

}
