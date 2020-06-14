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
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.NestedScrollType
import com.funnywolf.hollowkit.utils.*
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
    var scrollVertical: Boolean? = null
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
     * 是否正在 fling，Scroller 带来的滚动可能是 fling 也可能是 [smoothScrollTo]
     */
    var isFling = false
        private set

    var enableLog = false

    /**
     * 发生滚动时的回调
     */
    var onScrollChangedListener: ((BehavioralScrollView)->Unit)? = null

    private var behavior: NestedScrollBehavior? = null
    private val children = arrayOfNulls<View>(3)
    private val targets = arrayOfNulls<NestedScrollTarget>(3)

    /**
     * 发生嵌套滚动的直接子 view
     */
    private var child: View? = null
    /**
     * 发生嵌套滚动的目标 view
     */
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
        scrollVertical = behavior?.scrollVertical
        children[0] = behavior?.prevView?.also { addView(it) }
        targets[0] = behavior?.prevScrollTarget
        children[1] = behavior?.midView?.also { addView(it) }
        targets[1] = behavior?.midScrollTarget
        children[2] = behavior?.nextView?.also { addView(it) }
        targets[2] = behavior?.nextScrollTarget
    }

    fun smoothScrollTo(dest: Int, duration: Int = 300) {
        smoothScrollSelf(dest, duration)
    }

    /**
     * 当前滚动的百分比
     */
    fun currProcess(): Float {
        return when(scrollVertical) {
            true -> if (scrollY > 0) {
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
            false -> if (scrollX > 0) {
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
        when (scrollVertical) {
            true -> layoutVertical()
            false -> layoutHorizontal()
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
        // 在 down 时复位一些标志位，停掉 scroller 的动画
        if (e.action == MotionEvent.ACTION_DOWN) {
            lastScrollDir = 0
            isFling = false
            scroller.abortAnimation()
        }
        // behavior 优先处理，不处理走默认逻辑
        return behavior?.handleDispatchTouchEvent(this, e)
            ?: super.dispatchTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        // behavior 优先处理，不处理时走自身的拦截逻辑
        return behavior?.handleInterceptTouchEvent(this, e)
            ?: when (e.action) {
                // down 时不拦截，但需要记录触点位置，寻找触点位置的 child 和垂直滚动的 target
                MotionEvent.ACTION_DOWN -> {
                    lastX = e.x
                    lastY = e.y
                    // layout 时，直接子 view 不会有重叠的情况，所以这里的 child 是准确的
                    child = findChildUnder(e.rawX, e.rawY)
                    // 如果子 view 有重叠的情况，这里记录的 target 并不完全准确，不过这里只做为是否拦截事件的判断
                    target = when (scrollVertical) {
                        true -> findVerticalNestedScrollingTarget(e.rawX, e.rawY)
                        false -> findHorizontalNestedScrollingTarget(e.rawX, e.rawY)
                        else -> null
                    }
                    false
                }
                // move 时如果移动，且没有 target 就自己拦截
                MotionEvent.ACTION_MOVE -> when (scrollVertical) {
                    true -> if (abs(e.y - lastY) > abs(e.x - lastX) && target == null) {
                        true
                    } else {
                        lastY = e.y
                        false
                    }
                    false -> if (abs(e.x - lastX) > abs(e.y - lastY) && target == null) {
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
        return behavior?.handleTouchEvent(this, e)
            ?: when (e.action) {
                // down 不处理，之后的就无法处理
                MotionEvent.ACTION_DOWN -> {
                    velocityTracker.addMovement(e)
                    lastX = e.x
                    lastY = e.y
                    // 请求父 view 不要拦截事件
                    requestDisallowInterceptTouchEvent(true)
                    true
                }
                // move 时计算并分发滚动量
                MotionEvent.ACTION_MOVE -> {
                    velocityTracker.addMovement(e)
                    val dx = (lastX - e.x).toInt()
                    val dy = (lastY - e.y).toInt()
                    lastX = e.x
                    lastY = e.y
                    dispatchBothScroll(dx, dy, ViewCompat.TYPE_TOUCH)
                    true
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

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return when (scrollVertical) {
            true -> (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
            false -> (axes and ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0
            else -> false
        }
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
        // 记录嵌套滚动的 child 和 target
        this.child = child
        this.target = target
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        when (dispatchBothScroll(dx, dy, type)) {
            true -> consumed[1] = dy
            false -> consumed[0] = dx
        }
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
        dispatchBothScroll(dxUnconsumed, dyUnconsumed, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        dispatchBothScroll(dxUnconsumed, dyUnconsumed, type)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        // fling 一定会拦截下来处理
        fling(velocityX, velocityY)
        return true
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
    }

    private fun fling(vx: Float, vy: Float) {
        isFling = true
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
        isFling = false
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
        val sv = scrollVertical ?: return
        if (scroller.computeScrollOffset()) {
            val dx = (scroller.currX - lastX).toInt()
            val dy = (scroller.currY - lastY).toInt()
            lastX = scroller.currX.toFloat()
            lastY = scroller.currY.toFloat()
            log("computeScroll dx = $dx, dy = $dy, vx = ${scroller.currVelocity}")

            if (dispatchBothScroll(dx, dy, ViewCompat.TYPE_NON_TOUCH) == null) {
                // 自身不处理滚动时，在这里需要手动滚动 target
                if (sv) {
                    target?.scrollBy(0, dy)
                } else {
                    target?.scrollBy(dx, 0)
                }
            }
            invalidate()
        } else {
            isFling = false
        }
    }

    /**
     * 分发水平和垂直的滚动量，这里有 x 和 y 轴的滚动量，会先判断方向，再分发当前方向的滚动量
     * @return 自己处理的滚动量方向，true 表示垂直，false 表示水平，null 表示自己不处理
     */
    private fun dispatchBothScroll(dx: Int, dy: Int, @ViewCompat.NestedScrollType type: Int): Boolean? {
        return when (scrollVertical) {
            true -> if (dispatchScroll(dy, type)) {
                true
            } else {
                null
            }
            false -> if (dispatchScroll(dx, type)) {
                false
            } else {
                null
            }
            else -> null
        }
    }

    /**
     * 分发当前方向的滚动量
     * @return 是否自己处理滚动量
     */
    private fun dispatchScroll(scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean {
        log("dispatchScroll $scroll ${type == ViewCompat.TYPE_TOUCH} ${target?.canScrollVertically(scroll)}")
        return when {
            // 默认 0 可以处理
            scroll == 0 -> true
            // 优先自己滚动且自己可以滚动，就自己滚动
            scrollSelfFirst(scroll, type) && canScrollSelf(scroll) -> {
                scrollSelf(scroll, type)
                true
            }
            // 判断 target 是否滚动
            shouldTargetScroll(scroll, type) && canScrollTarget(scroll) -> false
            // target 不滚动就自己滚动
            else -> {
                switchTargetIfNeed(scroll, type)
                scrollSelf(scroll, type)
                true
            }
        }
    }

    /**
     * 在分发滚动量时，是否优先滚动自己
     */
    private fun scrollSelfFirst(scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean {
        return behavior?.scrollSelfFirst(this, scroll, type) ?: false
    }

    /**
     * 在分发滚动量时，是否允许 target 滚动
     */
    private fun shouldTargetScroll(scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean {
        return behavior?.shouldTargetScroll(this, scroll, type) ?: true
    }

    /**
     * 在分发滚动量 target 不再滚动时，判断是否需要切换 child 和 target
     * 比如在 fling 时，target 在滚到底不滚动时，为了达到平滑的效果，需要把之后的滚动分发给下一个 child 和 target
     */
    private fun switchTargetIfNeed(scroll: Int, @ViewCompat.NestedScrollType type: Int) {
        if (ViewCompat.TYPE_NON_TOUCH != type || canScrollTarget(scroll)) {
            return
        }
        val c = child ?: return
        val index = children.indexOf(c) + if (scroll > 0) { 1 } else { -1 }
        if (index >= 0 && index < children.size) {
            child = children[index]
            target = targets[index]?.invoke(this, scroll, type)
        }
    }

    /**
     * 处理自身滚动
     */
    private fun scrollSelf(scroll: Int, @ViewCompat.NestedScrollType type: Int) {
        // behavior 优先拦截处理，不拦截时就默认处理自身的滚动
        if (behavior?.handleScrollSelf(this, scroll, type) == true) {
            return
        }
        scrollBy(scroll, scroll)
    }

    /**
     * 根据当前方向判断 [target] 是否可以滚动
     */
    private fun canScrollTarget(scroll: Int): Boolean {
        val target = target ?: return false
        return when (scrollVertical) {
            true -> target.canScrollVertically(scroll)
            false -> target.canScrollHorizontally(scroll)
            else -> true
        }
    }

    /**
     * 根据当前方向判断自身是否可以滚动
     */
    private fun canScrollSelf(dir: Int): Boolean {
        return when (scrollVertical) {
            true -> canScrollVertically(dir)
            false -> canScrollHorizontally(dir)
            else -> false
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return when {
            scrollVertical != true -> false
            direction > 0 -> scrollY < maxScroll
            direction < 0 -> scrollY > minScroll
            else -> true
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return when {
            scrollVertical != false -> false
            direction > 0 -> scrollX < maxScroll
            direction < 0 -> scrollX > minScroll
            else -> true
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        super.scrollBy(getScrollByX(x), getScrollByY(y))
    }

    /**
     * 根据方向计算 x 轴的真正滚动量
     */
    private fun getScrollByX(dx: Int): Int {
        val newX = scrollX + dx
        return when {
            scrollVertical != false -> scrollX
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
            scrollVertical != true -> scrollY
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
        lastScrollDir = when (scrollVertical) {
            true -> t - oldt
            false -> l - oldl
            else -> 0
        }
        onScrollChangedListener?.invoke(this)
    }

    private fun log(text: String) {
        if (enableLog) {
            Log.d(javaClass.simpleName, text)
        }
    }
}

/**
 * 嵌套滚动的目标 View
 * 参数分别时 [BehavioralScrollView]、滚动量和 [ViewCompat.NestedScrollType]
 * 返回值可滚动的目标 View
 */
typealias NestedScrollTarget = (BehavioralScrollView, Int, Int)->View

interface NestedScrollBehavior {
    /**
     * 当前的可滚动方向
     * true -> 垂直滚动
     * false -> 水平滚动
     * null -> 不可滚动
     */
    val scrollVertical: Boolean

    val prevView: View?
    val prevScrollTarget: NestedScrollTarget?
    val midView: View
    val midScrollTarget: NestedScrollTarget?
    val nextView: View?
    val nextScrollTarget: NestedScrollTarget?

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
     * 在 [v] onInterceptTouchEvent 时是否处理 touch 事件
     *
     * @param v
     * @param e touch 事件
     * @return true -> 处理，会直接返回 true 拦截事件，false -> 不处理，会直接返回 false，null -> 不关心，会执行默认逻辑
     */
    fun handleInterceptTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? = null

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
    fun scrollSelfFirst(v: BehavioralScrollView, scroll: Int, @NestedScrollType type: Int): Boolean = false

    /**
     * 在 [v] 分发滚动量时，是否允许子 View target 滚动
     *
     * @param v
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return true -> 允许 target 滚动，false -> 不允许其滚动
     */
    fun shouldTargetScroll(v: BehavioralScrollView, scroll: Int, @NestedScrollType type: Int): Boolean = true

    /**
     * 在需要 [v] 自身滚动时，是否拦截处理
     *
     * @param v
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return true -> 拦截下来自己处理，false -> 不拦截，让 [v] 自己滚动
     */
    fun handleScrollSelf(v: BehavioralScrollView, scroll: Int, @NestedScrollType type: Int): Boolean = false

}
