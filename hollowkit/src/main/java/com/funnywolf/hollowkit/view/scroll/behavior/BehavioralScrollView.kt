package com.funnywolf.hollowkit.view.scroll.behavior

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import com.funnywolf.hollowkit.view.constrains
import com.funnywolf.hollowkit.view.findChildUnder
import com.funnywolf.hollowkit.view.findHorizontalNestedScrollingTarget
import com.funnywolf.hollowkit.view.findVerticalNestedScrollingTarget
import com.funnywolf.hollowkit.view.isUnder
import com.funnywolf.hollowkit.view.stopScroll
import kotlin.math.abs

/**
 * 可以方便控制嵌套滚动优先级的布局，处理嵌套滚动相关的通用逻辑
 *
 * 1、不关心内部视图如何布局，继承 [ConstraintLayout] 只是为了方便构建布局
 * 2、布局完成后，会自动根据布局的上下左右边界，确定滚动范围，并支持子类修改
 * 3、处理 touch 事件的拦截和分发，支持随手指滚动和 fling
 * 4、实现了 [NestedScrollingParent3] 和 [NestedScrollingChild3]，支持多级嵌套
 * 5、对子类暴露 [NestedScrollBehavior]，只用实现很少的方法，就可以控制嵌套滚动各个阶段的优先级问题
 * 6、推荐继承而不是组合的方式是因为，嵌套滚动的功能性比较垂直，而且会和自身有很多交互
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/26
 */
open class BehavioralScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr), NestedScrollingParent3, NestedScrollingChild3, NestedScrollBehavior {

    /**
     * 滚动方向，水平 or 垂直
     */
    @ViewCompat.ScrollAxis
    var scrollAxis: Int = ViewCompat.SCROLL_AXIS_VERTICAL

    /**
     * 滚动的最小值
     */
    var minScroll = 0
        protected set

    /**
     * 滚动的最大值
     */
    var maxScroll = 0
        protected set

    /**
     * 上次滚动的方向
     */
    var lastScrollDir = 0
        private set

    /**
     * 当前的滚动状态
     */
    @ScrollState
    var state: Int = ScrollState.NONE
        private set(value) {
            if (field != value) {
                val from = field
                field = value
                listeners.forEach { it.onStateChanged(this, from, value) }
            }
        }

    /**
     * 滚动相关状态的回调
     */
    val listeners = HashSet<BehavioralScrollListener>()

    /**
     * 当前发生嵌套滚动的直接子 view
     */
    var nestedScrollChild: View? = null
        private set

    /**
     * 当前发生嵌套滚动的目标 view
     */
    var nestedScrollTarget: View? = null
        private set

    /**
     * 使能日志
     */
    var enableLog = false

    // region 一些辅助变量

    /**
     * 拦截事件的最小位移量
     */
    private val touchInterceptSlop: Int

    /**
     * fling 的最小速度
     */
    private val minFlingVelocity: Float
    /**
     * fling 的最大速度
     */
    private val maxFlingVelocity: Float

    /**
     * 用来处理松手时的连续滚动和动画
     */
    private val scroller = Scroller(context)

    /**
     * scroller 结束时的回调
     */
    private var onEndListener: ((BehavioralScrollView)->Unit)? = null

    /**
     * 嵌套滚动帮助类
     */
    private val parentHelper by lazy { NestedScrollingParentHelper(this) }
    private val childHelper by lazy { NestedScrollingChildHelper(this) }

    /**
     * 上次触摸事件的 x 值，或者 scroller.currX，用于处理自身的滑动事件或动画
     */
    private var lastX = 0F
    /**
     * 上次触摸事件的 y 值，或者 scroller.currY，用于处理自身的滑动事件或动画
     */
    private var lastY = 0F
    /**
     * 上次触摸事件的时间戳
     */
    private var lastEventTime = 0L
    /**
     * 上次 move 事件的 x 变化量
     */
    private var moveDx = 0
    /**
     * 上次 move 事件的 y 变化量
     */
    private var moveDy = 0
    /**
     * 上次 move 事件的时间间隔
     */
    private var moveDuration = 0L

    /**
     * down 事件的坐标和滚动状态
     */
    private var downX = 0F
    private var downY = 0F
    private var downState = ScrollState.NONE

    // endregion

    init {
        isNestedScrollingEnabled = true
        val vc = ViewConfiguration.get(context)
        touchInterceptSlop = vc.scaledTouchSlop
        minFlingVelocity = vc.scaledMinimumFlingVelocity.toFloat()
        maxFlingVelocity = vc.scaledMaximumFlingVelocity.toFloat()
    }

    /**
     * 动画移动到某个滚动量，动画过程中滚动量不会进行分发
     */
    fun smoothScrollTo(scroll: Int, duration: Int = 300, onEnd: ((BehavioralScrollView)->Unit)? = null) {
        log("smoothScrollSelf $scroll")
        stopScroll()
        // 更改状态，开始动画
        state = ScrollState.ANIMATION
        lastX = 0F
        lastY = 0F
        onEndListener = onEnd
        // 这里不区分滚动方向，在分发滚动量的时候会处理
        scroller.startScroll(
                lastX.toInt(), lastY.toInt(),
                scroll - scrollX, scroll - scrollY,
                duration
        )
        invalidate()
    }

    /**
     * 以某个速度进行惯性滚动，fling 过程中滚动量会进行分发
     */
    fun fling(velocity: Float, onEnd: ((BehavioralScrollView)->Unit)? = null) {
        log("fling $velocity")
        stopScroll()
        state = ScrollState.FLING
        lastX = 0F
        lastY = 0F
        onEndListener = onEnd
        // 这里不区分滚动方向，在分发滚动量的时候会处理
        scroller.fling(
            lastX.toInt(), lastY.toInt(), velocity.toInt(), velocity.toInt(),
            Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE
        )
        startNestedScroll(nestedScrollAxes, ViewCompat.TYPE_NON_TOUCH)
        invalidate()
    }

    /**
     * 停止滚动，[stopNestedScroll] 表示是否停止内部嵌套视图的滚动
     */
    fun stopScroll(stopNestedScroll: Boolean = true) {
        // 停止当前的所有动画
        scroller.forceFinished(true)
        if (stopNestedScroll) {
            // 停掉嵌套滚动
            nestedScrollChild?.stopScroll()
        }
    }

    // region layout

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        adjustScrollBounds()
        // 重新 layout 后，滚动范围可能已经变了，当前的滚动量可能超范围了
        // 需要重新矫正，scrollBy 内部会进行滚动范围的矫正
        scrollBy(0, 0)
    }

    /**
     * 调整默认的滚动边界
     */
    protected open fun adjustScrollBounds() {
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
        when (nestedScrollAxes) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> {
                minScroll = l
                maxScroll = r - width
            }
            ViewCompat.SCROLL_AXIS_VERTICAL -> {
                minScroll = t
                maxScroll = b - height
            }
            else -> {
                minScroll = 0
                maxScroll = 0
            }
        }
    }

    // endregion

    // region touch event

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        // TODO: dispatchTouchEvent 里面干了很多事，直接拦截不太好，之后会去掉
        // 优先处理 behavior，不处理走默认逻辑
        handleDispatchTouchEvent(e)?.also {
            log("handleDispatchTouchEvent $it")
            return it
        }
        return super.dispatchTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            // down 时不拦截，但需要记录触点位置，寻找触点位置的 child 和支持嵌套滚动的 target
            MotionEvent.ACTION_DOWN -> {
                // 记录 down 时坐标和滚动状态
                lastX = e.rawX
                lastY = e.rawY
                downX = e.rawX
                downY = e.rawY
                downState = state

                // 在 down 时复位一些标志位，停掉 scroller 的动画
                stopScroll(nestedScrollChild?.isUnder(e.rawX, e.rawY) == false)
                state = ScrollState.NONE
                lastScrollDir = 0

                // 寻找可以嵌套滚动的子 view
                nestedScrollChild = findChildUnder(e.rawX, e.rawY)
                // 如果子 view 有重叠的情况，这里记录的 target 并不完全准确，不过这里只做为是否拦截事件的判断
                nestedScrollTarget = when (nestedScrollAxes) {
                    ViewCompat.SCROLL_AXIS_HORIZONTAL -> findHorizontalNestedScrollingTarget(e.rawX, e.rawY)
                    ViewCompat.SCROLL_AXIS_VERTICAL -> findVerticalNestedScrollingTarget(e.rawX, e.rawY)
                    else -> null
                }
                false
            }
            // move 时如果移动，且没有 target 就自己拦截
            MotionEvent.ACTION_MOVE -> when (nestedScrollAxes) {
                ViewCompat.SCROLL_AXIS_HORIZONTAL -> if (
                        abs(e.rawX - lastX) > touchInterceptSlop
                        && abs(e.rawX - lastX) > abs(e.rawY - lastY)
                        && nestedScrollTarget == null
                ) {
                    lastX = e.rawX
                    true
                } else {
                    false
                }
                ViewCompat.SCROLL_AXIS_VERTICAL -> if (
                        abs(e.rawY - lastY) > touchInterceptSlop
                        && abs(e.rawY - lastY) > abs(e.rawX - lastX)
                        && nestedScrollTarget == null
                ) {
                    lastY = e.rawY
                    true
                } else {
                    false
                }
                else -> false
            }
            MotionEvent.ACTION_UP -> {
                // down 的时候不是空闲状态（fling 或动画），down 会停止相关滚动
                // up 之前手指几乎未移动，为了避免触发子 view 的点击事件，拦截 up 事件
                downState != ScrollState.NONE
                    && abs(lastX - downX) < touchInterceptSlop
                    && abs(lastY - downY) < touchInterceptSlop
            }
            else -> super.onInterceptTouchEvent(e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        // 优先处理 behavior，不处理时自己处理 touch 事件
        handleTouchEvent(e)?.also {
            log("handleTouchEvent $it")
            return it
        }
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = e.rawX
                lastY = e.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                moveDuration = e.eventTime - lastEventTime
                moveDx = (lastX - e.rawX).toInt()
                moveDy = (lastY - e.rawY).toInt()
                lastEventTime = e.eventTime
                lastX = e.rawX
                lastY = e.rawY
                // 不再 dragging 状态时判断是否要进行拖拽
                if (state != ScrollState.DRAGGING) {
                    val canDrag = when (nestedScrollAxes) {
                        ViewCompat.SCROLL_AXIS_HORIZONTAL -> abs(moveDx) > abs(moveDy)
                        ViewCompat.SCROLL_AXIS_VERTICAL -> abs(moveDx) < abs(moveDy)
                        else -> false
                    }
                    if (canDrag) {
                        startNestedScroll(nestedScrollAxes, ViewCompat.TYPE_TOUCH)
                        state = ScrollState.DRAGGING
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                // dragging 时计算并分发滚动量
                if (state == ScrollState.DRAGGING) {
                    dispatchScrollInternal(moveDx, moveDy, ViewCompat.TYPE_TOUCH)
                }
            }
            MotionEvent.ACTION_UP -> {
                state = ScrollState.NONE
                stopNestedScroll(ViewCompat.TYPE_TOUCH)
                val vx = moveDx / (moveDuration / 1000F)
                val vy = moveDy / (moveDuration / 1000F)
                if (!dispatchNestedPreFling(vx, vy)) {
                    handleFling(vx, vy)
                }
            }
        }
        return true
    }

    // endregion

    // region NestedScrollChild
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
    // endregion

    // region NestedScrollParent
    override fun getNestedScrollAxes(): Int {
        return scrollAxis
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and nestedScrollAxes) != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
        startNestedScroll(axes, type)
        nestedScrollChild = child
        nestedScrollTarget = target
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        dispatchNestedPreScrollInternal(dx, dy, consumed)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        dispatchNestedPreScrollInternal(dx, dy, consumed, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        dispatchNestedScrollInternal(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        dispatchNestedScrollInternal(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
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
        dispatchNestedScrollInternal(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
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
        return !consumed && handleFling(velocityX, velocityY)
    }

    override fun onStopNestedScroll(target: View) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }
    // endregion

    // region dispatchScroll

    protected open fun handleFling(vx: Float, vy: Float): Boolean {
        val handled = when(nestedScrollAxes) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> if (abs(vx) > minFlingVelocity) {
                fling(vx.constrains(-maxFlingVelocity, maxFlingVelocity))
                true
            } else {
                false
            }
            ViewCompat.SCROLL_AXIS_VERTICAL -> if (abs(vy) > minFlingVelocity) {
                fling(vy.constrains(-maxFlingVelocity, maxFlingVelocity))
                true
            } else {
                false
            }
            else -> false
        }
        dispatchNestedFling(vx, vy, handled)
        return handled
    }

    override fun computeScroll() {
        when {
            scroller.computeScrollOffset() -> {
                val dx = (scroller.currX - lastX).toInt()
                val dy = (scroller.currY - lastY).toInt()
                lastX = scroller.currX.toFloat()
                lastY = scroller.currY.toFloat()
                // 不分发来自动画的滚动
                if (state == ScrollState.ANIMATION) {
                    scrollBy(dx, dy)
                } else {
                    dispatchScrollInternal(dx, dy, ViewCompat.TYPE_NON_TOUCH)
                }
                invalidate()
            }
            state == ScrollState.ANIMATION -> {
                state = ScrollState.NONE
                onEndListener?.invoke(this)
                onEndListener = null
            }
            state == ScrollState.FLING -> {
                state = ScrollState.NONE
                stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
                onEndListener?.invoke(this)
                onEndListener = null
            }
        }
    }

    /**
     * 分发来自自身 touch 事件或 fling 的滚动量
     * -> [dispatchNestedPreScrollInternal]
     * -> [dispatchScrollSelf]
     * -> [dispatchNestedScrollInternal]
     */
    private fun dispatchScrollInternal(dx: Int, dy: Int, type: Int) {
        log("dispatchScrollInternal: type=$type, x=$dx, y=$dy")
        val consumed = IntArray(2)
        when (nestedScrollAxes) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> {
                var consumedX = 0
                dispatchNestedPreScrollInternal(dx, dy, consumed, type)
                consumedX += consumed[0]
                consumedX += dispatchScrollSelf(dx - consumedX, type)
                val consumedY = consumed[1]
                // 复用数组
                consumed[0] = 0
                consumed[1] = 0
                dispatchNestedScrollInternal(consumedX, consumedY, dx - consumedX, dy - consumedY, type, consumed)
            }
            ViewCompat.SCROLL_AXIS_VERTICAL -> {
                var consumedY = 0
                dispatchNestedPreScrollInternal(dx, dy, consumed, type)
                consumedY += consumed[1]
                consumedY += dispatchScrollSelf(dy - consumedY, type)
                val consumedX = consumed[0]
                // 复用数组
                consumed[0] = 0
                consumed[1] = 0
                dispatchNestedScrollInternal(consumedX, consumedY, dx - consumedX, dy - consumedY, type, consumed)
            }
            else -> {
                dispatchNestedPreScrollInternal(dx, dy, consumed, type)
                val consumedX = consumed[0]
                val consumedY = consumed[1]
                // 复用数组
                consumed[0] = 0
                consumed[1] = 0
                dispatchNestedScrollInternal(consumedX, consumedY, dx - consumedX, dy - consumedY, type, consumed)
            }
        }
    }

    /**
     * 分发 pre scroll 的滚动量
     */
    private fun dispatchNestedPreScrollInternal(
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int = ViewCompat.TYPE_TOUCH
    ) {
        log("dispatchNestedPreScrollInternal: type=$type, x=$dx, y=$dy")
        when (nestedScrollAxes) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> {
                val handleFirst = handleNestedPreScrollFirst(dx, type)
                log("handleNestedPreScrollFirst = $handleFirst")
                when (handleFirst) {
                    true -> {
                        val selfConsumed = dispatchScrollSelf(dx, type)
                        dispatchNestedPreScroll(dx - selfConsumed, dy, consumed, null, type)
                        consumed[0] += selfConsumed
                    }
                    false -> {
                        dispatchNestedPreScroll(dx, dy, consumed, null, type)
                        val selfConsumed = dispatchScrollSelf(dx - consumed[0], type)
                        consumed[0] += selfConsumed
                    }
                    null -> dispatchNestedPreScroll(dx, dy, consumed, null, type)
                }
            }
            ViewCompat.SCROLL_AXIS_VERTICAL ->{
                val handleFirst = handleNestedPreScrollFirst(dy, type)
                log("handleNestedPreScrollFirst = $handleFirst")
                when (handleFirst) {
                    true -> {
                        val selfConsumed = dispatchScrollSelf(dy, type)
                        dispatchNestedPreScroll(dx, dy - selfConsumed, consumed, null, type)
                        consumed[1] += selfConsumed
                    }
                    false -> {
                        dispatchNestedPreScroll(dx, dy, consumed, null, type)
                        val selfConsumed = dispatchScrollSelf(dy - consumed[1], type)
                        consumed[1] += selfConsumed
                    }
                    null -> dispatchNestedPreScroll(dx, dy, consumed, null, type)
                }
            }
            else -> dispatchNestedPreScroll(dx, dy, consumed, null, type)
        }
    }

    /**
     * 分发 nested scroll 的滚动量
     */
    private fun dispatchNestedScrollInternal(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int = ViewCompat.TYPE_TOUCH,
        consumed: IntArray = intArrayOf(0, 0)
    ) {
        log("dispatchNestedScrollInternal: type=$type, x=$dxUnconsumed, y=$dyUnconsumed")
        when (nestedScrollAxes) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> {
                val handleFirst = handleNestedScrollFirst(dxUnconsumed, type)
                log("handleNestedScrollFirst = $handleFirst")
                when (handleFirst) {
                    true -> {
                        val selfConsumed = dispatchScrollSelf(dxUnconsumed, type)
                        dispatchNestedScroll(dxConsumed + selfConsumed, dyConsumed, dxUnconsumed - selfConsumed, dyUnconsumed, null, type, consumed)
                        consumed[0] += selfConsumed
                    }
                    false -> {
                        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type, consumed)
                        val selfConsumed = dispatchScrollSelf(dxUnconsumed - consumed[0], type)
                        consumed[0] += selfConsumed
                    }
                    null -> dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type, consumed)
                }
            }
            ViewCompat.SCROLL_AXIS_VERTICAL -> {
                val handleFirst = handleNestedScrollFirst(dyUnconsumed, type)
                log("handleNestedScrollFirst = $handleFirst")
                when (handleFirst) {
                    true -> {
                        val selfConsumed = dispatchScrollSelf(dyUnconsumed, type)
                        dispatchNestedScroll(dxConsumed, dyConsumed + selfConsumed, dxUnconsumed, dyUnconsumed - selfConsumed, null, type, consumed)
                        consumed[1] += selfConsumed
                    }
                    false -> {
                        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type, consumed)
                        val selfConsumed = dispatchScrollSelf(dyUnconsumed - consumed[1], type)
                        consumed[1] += selfConsumed
                    }
                    null -> dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type, consumed)
                }
            }
            else -> dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null, type, consumed)
        }
    }

    /**
     * 处理自身滚动
     */
    private fun dispatchScrollSelf(scroll: Int, @ViewCompat.NestedScrollType type: Int): Int {
        // 没有滚动量就不用回调 behavior 了
        if (scroll == 0) {
            return 0
        }
        // behavior 优先决定是否滚动自身
        val handle = handleScrollSelf(scroll, type)
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

    // endregion

    // region scrollTo scrollBy

    /**
     * 根据当前方向判断自身是否可以滚动
     */
    private fun canScrollSelf(dir: Int): Boolean {
        return when (nestedScrollAxes) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> canScrollHorizontally(dir)
            ViewCompat.SCROLL_AXIS_VERTICAL -> canScrollVertically(dir)
            else -> false
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return when {
            nestedScrollAxes != ViewCompat.SCROLL_AXIS_VERTICAL -> false
            direction > 0 -> scrollY < maxScroll
            direction < 0 -> scrollY > minScroll
            else -> true
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return when {
            nestedScrollAxes != ViewCompat.SCROLL_AXIS_HORIZONTAL -> false
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
            nestedScrollAxes != ViewCompat.SCROLL_AXIS_HORIZONTAL -> scrollX
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
            nestedScrollAxes != ViewCompat.SCROLL_AXIS_VERTICAL -> scrollY
            scrollY > 0 -> newY.constrains(0, maxScroll)
            scrollY < 0 -> newY.constrains(minScroll, 0)
            else -> newY.constrains(minScroll, maxScroll)
        } - scrollY
    }

    // endregion

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        when (nestedScrollAxes) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> {
                lastScrollDir = l - oldl
                listeners.forEach { it.onScrollChanged(this, oldl, l) }
            }
            ViewCompat.SCROLL_AXIS_VERTICAL -> {
                lastScrollDir = t - oldt
                listeners.forEach { it.onScrollChanged(this, oldt, t) }
            }
            else -> lastScrollDir = 0
        }
    }

    private fun log(text: String) {
        if (enableLog) {
            Log.d(javaClass.simpleName, text)
        }
    }

}

/**
 * 用于描述正处于的嵌套滚动状态，和滚动类型 [ViewCompat.NestedScrollType] 共同描述滚动量
 */
@IntDef(ScrollState.NONE, ScrollState.DRAGGING, ScrollState.ANIMATION, ScrollState.FLING)
@Retention(AnnotationRetention.SOURCE)
annotation class ScrollState {
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
         * 正在动画，动画产生的滚动不会被分发
         */
        const val ANIMATION = 2
        /**
         * 正在 fling
         */
        const val FLING = 3
    }
}

/**
 * 嵌套滚动的优先级
 */
interface NestedScrollBehavior {

    /**
     * 在 dispatchTouchEvent 时是否处理 touch 事件
     *
     * @param e touch 事件
     * @return true -> 处理，会在 dispatchTouchEvent 中直接返回 true，false -> 直接返回 false，null -> 不关心，会执行默认逻辑
     */
    fun handleDispatchTouchEvent(e: MotionEvent): Boolean? = null

    /**
     * 在 onTouchEvent 时是否处理 touch 事件
     *
     * @param e touch 事件
     * @return true -> 处理，会直接返回 true，false -> 不处理，会直接返回 false，null -> 不关心，会执行默认逻辑
     */
    fun handleTouchEvent(e: MotionEvent): Boolean? = null

    /**
     * 在 onNestedPreScroll 时，是否优先自己处理
     *
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return true -> 自己优先，false -> 自己不优先，null -> 不处理 onNestedPreScroll
     */
    fun handleNestedPreScrollFirst(scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean? = null

    /**
     * 在 onNestedScroll 时，是否优先自己处理
     *
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return true -> 自己优先，false -> 自己不优先，null -> 不处理 onNestedPreScroll
     */
    fun handleNestedScrollFirst(scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean? = false

    /**
     * 在需要自身滚动时，是否处理
     *
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return 是否处理自身滚动，true -> 处理，false -> 不处理，null -> 不关心，会执行默认自身滚动
     */
    fun handleScrollSelf(scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean? = null

}

/**
 * 嵌套滚动回调
 */
interface BehavioralScrollListener {

    /**
     * 滚动状态变化的回调
     */
    fun onStateChanged(v: BehavioralScrollView, @ScrollState from: Int, @ScrollState to: Int) {}

    /**
     * 滚动值变化的回调
     */
    fun onScrollChanged(v: BehavioralScrollView, from: Int, to: Int) {}

}
