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
import androidx.core.view.*
import com.funnywolf.hollowkit.utils.findHorizontalNestedScrollingTarget
import com.funnywolf.hollowkit.utils.findVerticalNestedScrollingTarget
import kotlin.math.abs

/**
 * 嵌套滑动布局
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/8
 */
open class BehavioralScrollView : FrameLayout, NestedScrollingParent3, NestedScrollingChild3 {

    /**
     * 当前的可滚动方向
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
            if (field != value) {
                log("NestedScrollState $field -> $value")
                field = value
            }
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
    private val childHelper by lazy { NestedScrollingChildHelper(this) }

    init {
        isNestedScrollingEnabled = true
    }

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
            else -> 0F
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        minScroll = 0
        maxScroll = 0
        when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> layoutHorizontal()
            ViewCompat.SCROLL_AXIS_VERTICAL -> layoutVertical()
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
        // behavior 优先处理，不处理走默认逻辑
        behavior?.handleDispatchTouchEvent(this, e)?.also {
            log("handleDispatchTouchEvent $it")
            return it
        }
        // 在 down 时复位一些标志位，停掉 scroller 的动画
        if (e.action == MotionEvent.ACTION_DOWN) {
            lastScrollDir = 0
            state = NestedScrollState.NONE
            scroller.abortAnimation()
        }
        return super.dispatchTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            // down 时不拦截，但需要记录触点位置，寻找触点位置的 child 和垂直滚动的 target
            MotionEvent.ACTION_DOWN -> {
                startNestedScroll(scrollAxis, ViewCompat.TYPE_TOUCH)
                lastX = e.rawX
                lastY = e.rawY
                // 如果子 view 有重叠的情况，这里记录的 target 并不完全准确，不过这里只做为是否拦截事件的判断
                target = when (scrollAxis) {
                    ViewCompat.SCROLL_AXIS_HORIZONTAL -> findHorizontalNestedScrollingTarget(e.rawX, e.rawY)
                    ViewCompat.SCROLL_AXIS_VERTICAL -> findVerticalNestedScrollingTarget(e.rawX, e.rawY)
                    else -> null
                }
                false
            }
            // move 时如果移动，且没有 target 就自己拦截
            MotionEvent.ACTION_MOVE -> when (scrollAxis) {
                ViewCompat.SCROLL_AXIS_HORIZONTAL -> if (abs(e.rawX - lastX) > abs(e.rawY - lastY) && target == null) {
                    true
                } else {
                    lastX = e.rawX
                    false
                }
                ViewCompat.SCROLL_AXIS_VERTICAL -> if (abs(e.rawY - lastY) > abs(e.rawX - lastX) && target == null) {
                    true
                } else {
                    lastY = e.rawY
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
        behavior?.handleTouchEvent(this, e)?.also {
            log("handleTouchEvent $it")
            return it
        }
        when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    velocityTracker.addMovement(e)
                    lastX = e.rawX
                    lastY = e.rawY
                    // down 时分发开始 nested scroll
                    startNestedScroll(scrollAxis, ViewCompat.TYPE_TOUCH)
                }
                MotionEvent.ACTION_MOVE -> {
                    velocityTracker.addMovement(e)
                    val dx = (lastX - e.rawX).toInt()
                    val dy = (lastY - e.rawY).toInt()
                    lastX = e.rawX
                    lastY = e.rawY
                    // 不再 dragging 状态时判断是否要进行拖拽
                    if (state != NestedScrollState.DRAGGING) {
                        val canDrag = when (scrollAxis) {
                            ViewCompat.SCROLL_AXIS_HORIZONTAL -> abs(dx) > abs(dy)
                            ViewCompat.SCROLL_AXIS_VERTICAL -> abs(dx) < abs(dy)
                            else -> false
                        }
                        if (canDrag) {
                            state = NestedScrollState.DRAGGING
                            parent?.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                    // move 时计算并分发滚动量
                    if (state == NestedScrollState.DRAGGING) {
                        dispatchScrollFromSelf(dx, dy, ViewCompat.TYPE_TOUCH)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    velocityTracker.addMovement(e)
                    velocityTracker.computeCurrentVelocity(1000)
                    fling(-velocityTracker.xVelocity, -velocityTracker.yVelocity)
                    velocityTracker.clear()
                }
            }
        return true
    }

    // NestedScrollChild
    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper.hasNestedScrollingParent()
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return childHelper.hasNestedScrollingParent(type)
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper.startNestedScroll(axes)
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return childHelper.startNestedScroll(axes, type)
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun stopNestedScroll() {
        childHelper.stopNestedScroll()
    }

    override fun stopNestedScroll(type: Int) {
        childHelper.stopNestedScroll(type)
    }
    // NestedScrollChild

    // NestedScrollParent
    override fun getNestedScrollAxes(): Int {
        return scrollAxis
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> (axes and ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0
            ViewCompat.SCROLL_AXIS_VERTICAL -> (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
            else -> false
        }
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
        startNestedScroll(axes, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        dispatchNestedPreScrollFromChild(dx, dy, consumed)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        dispatchNestedPreScrollFromChild(dx, dy, consumed, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        dispatchNestedScrollFromChild(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        dispatchNestedScrollFromChild(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
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
        dispatchNestedScrollFromChild(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        if (!consumed) {
            dispatchNestedFling(velocityX, velocityY, true)
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
        stopNestedScroll(type)
    }
    // NestedScrollParent

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
            dispatchScrollFromSelf(dx, dy, ViewCompat.TYPE_NON_TOUCH)
            invalidate()
        } else if (state == NestedScrollState.ANIMATION || state == NestedScrollState.FLING) {
            state = NestedScrollState.NONE
        }
    }

    /**
     * 分发来自自身 touch 事件、fling 或动画的滚动量
     * -> dispatchNestedPreScroll
     * -> handleScrollSelf
     * -> dispatchNestedScroll
     */
    private fun dispatchScrollFromSelf(dx: Int, dy: Int, type: Int) {
        log("dispatchScrollFromSelf: type=$type, x=$dx, y=$dy")
        val consumed = IntArray(2)
        // 滚动自己前，向父级分发 pre scroll
        dispatchNestedPreScroll(dx, dy, consumed, null, type)
        when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> {
                // 计算消耗的滚动量
                val parentUnconsumedX = dx - consumed[0]
                val consumedX = consumed[0] + handleScrollSelf(parentUnconsumedX, type)
                // 复用 consumed 数组
                consumed[0] = 0
                // 滚动自己后，向父级分发 nested scroll
                dispatchNestedScroll(consumedX, 0, dx - consumedX, 0, null, type, consumed)
            }
            ViewCompat.SCROLL_AXIS_VERTICAL -> {
                // 计算消耗的滚动量
                val parentUnconsumedY = dy - consumed[1]
                val consumedY = consumed[1] + handleScrollSelf(parentUnconsumedY, type)
                // 复用 consumed 数组
                consumed[1] = 0
                // 滚动自己后，向父级分发 nested scroll
                dispatchNestedScroll(0, consumedY, 0, dy - consumedY, null, type, consumed)
            }
        }
    }

    /**
     * 分发来自子 view pre scroll 的滚动量
     * -> dispatchNestedPreScroll
     * -> scrollSelfFirst
     */
    private fun dispatchNestedPreScrollFromChild(
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int = ViewCompat.TYPE_TOUCH
    ) {
        log("dispatchNestedPreScrollFromChild: type=$type, x=$dx, y=$dy")
        // 向父级分发 pre scroll
        dispatchNestedPreScroll(dx, dy, consumed, null, type)
        // 判断自己是否需要处理，并计算消耗的滚动量
        when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> {
                val parentUnconsumedX = dx - consumed[0]
                val scrollSelfFirst = behavior?.scrollSelfFirst(this, parentUnconsumedX, type)
                log("parentUnconsumedX = $parentUnconsumedX, scrollSelfFirst = $scrollSelfFirst")
                if (scrollSelfFirst == true) {
                    consumed[0] += handleScrollSelf(parentUnconsumedX, type)
                }
            }
            ViewCompat.SCROLL_AXIS_VERTICAL ->{
                val parentUnconsumedY = dy - consumed[1]
                val scrollSelfFirst = behavior?.scrollSelfFirst(this, parentUnconsumedY, type)
                log("parentUnconsumedY = $parentUnconsumedY, scrollSelfFirst = $scrollSelfFirst")
                if (scrollSelfFirst == true) {
                    consumed[1] += handleScrollSelf(parentUnconsumedY, type)
                }
            }
        }
        // dispatchNestedScroll 会在 onNestedScroll 时分发
    }

    /**
     * 分发来自子 view nested scroll 的滚动量
     * -> handleScrollSelf
     * -> dispatchNestedScroll
     */
    private fun dispatchNestedScrollFromChild(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int = ViewCompat.TYPE_TOUCH,
        consumed: IntArray? = null
    ) {
        log("dispatchNestedPreScrollFromChild: type=$type, x=$dxUnconsumed, y=$dyUnconsumed")
        // dispatchNestedPreScroll 已经在 onNestedPreScroll 分发过了
        when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> {
                val consumedX = handleScrollSelf(dxUnconsumed, type)
                consumed?.set(0, consumedX)
                // 滚动自己后，向父级分发 nested scroll
                dispatchNestedScroll(dxConsumed + consumedX, 0, dxUnconsumed - consumedX, 0, null, type)
            }
            ViewCompat.SCROLL_AXIS_VERTICAL -> {
                val consumedY = handleScrollSelf(dyUnconsumed, type)
                consumed?.set(1, consumedY)
                // 滚动自己后，向父级分发 nested scroll
                dispatchNestedScroll(0, dyConsumed + consumedY, 0, dyUnconsumed - consumedY, null, type)
            }
        }
    }

    /**
     * 处理自身滚动
     */
    private fun handleScrollSelf(scroll: Int, @ViewCompat.NestedScrollType type: Int): Int {
        // behavior 优先决定是否滚动自身
        val handle = behavior?.handleScrollSelf(this, scroll, type)
        val consumed = when(handle) {
            true -> scroll
            false -> 0
            else -> if (canScrollSelf(scroll)) {
                log("canScrollSelf")
                scrollBy(scroll, scroll)
                scroll
            } else {
                0
            }
        }
        log("handleScrollSelf: type=$type, $handle $scroll -> $consumed")
        return consumed
    }

    /**
     * 根据当前方向判断自身是否可以滚动
     */
    private fun canScrollSelf(dir: Int): Boolean {
        return when (scrollAxis) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> canScrollHorizontally(dir)
            ViewCompat.SCROLL_AXIS_VERTICAL -> canScrollVertically(dir)
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
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> l - oldl
            ViewCompat.SCROLL_AXIS_VERTICAL -> t - oldt
            else -> 0
        }
        onScrollChangedListeners.forEach { it(this) }
    }

    fun log(text: String) {
        if (enableLog) {
            Log.d(javaClass.simpleName, "${behavior?.javaClass} $text")
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
