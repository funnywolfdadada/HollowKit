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

/**
 * 嵌套滑动布局
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/8
 */
open class BehavioralNestedScrollLayout : FrameLayout, NestedScrollingParent3 {

    private var behavior: NestedScrollBehavior? = null
    private val views = arrayOfNulls<View>(3)
    private val targets = arrayOfNulls<NestedScrollTarget>(3)

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
     * 是否正在 fling
     */
    var isFling = false
        private set

    /**
     * 上次触摸事件的 y 值，用于处理自身的滑动事件
     */
    private var lastPosition = 0F

    private var nestedScrollChild: View? = null
    private var nestedScrollTarget: View? = null

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
        views[0] = behavior?.prevView?.also { addView(it) }
        targets[0] = behavior?.prevScrollTarget
        views[1] = behavior?.midView?.also { addView(it) }
        targets[1] = behavior?.midScrollTarget
        views[2] = behavior?.nextView?.also { addView(it) }
        targets[2] = behavior?.nextScrollTarget
    }

    fun smoothScrollTo(dest: Int, duration: Int = 300) {
        smoothScrollSelf(dest, duration)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        minScroll = 0
        maxScroll = 0
        when (scrollVertical) {
            true -> layoutVertical()
        }
        behavior?.afterLayout(this)
    }

    private fun layoutVertical() {
        val t = views[1]?.top ?: 0
        val b = views[1]?.bottom ?: 0
        views[0]?.also {
            it.offsetTopAndBottom(t - it.bottom)
            minScroll = it.top
        }
        views[2]?.also {
            it.offsetTopAndBottom(b - it.top)
            maxScroll = it.bottom - height
        }
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            lastScrollDir = 0
            isFling = false
            scroller.abortAnimation()
        }
        return behavior?.handleDispatchTouchEvent(this, e) ?: super.dispatchTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return behavior?.handleInterceptTouchEvent(this, e) ?: when (e.action) {
            // down 时不拦截，但需要记录位置
            MotionEvent.ACTION_DOWN -> {
                lastPosition = e.y
                nestedScrollChild = findChildUnder(e.rawX, e.rawY)
                nestedScrollTarget = findVerticalNestedScrollingTarget(e.rawX, e.rawY)
                false
            }
            // move 时需要根据是否移动，是否有可处理对应方向移动的子 view，判断是否要自己拦截
            MotionEvent.ACTION_MOVE -> {
                if (lastPosition != e.y && nestedScrollTarget == null) {
                    true
                } else {
                    lastPosition = e.y
                    false
                }
            }
            else -> super.onInterceptTouchEvent(e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        return behavior?.handleTouchEvent(this, e) ?: when (e.action) {
            // down 不处理，之后的就无法处理
            MotionEvent.ACTION_DOWN -> {
                velocityTracker.addMovement(e)
                lastPosition = e.y
                // 请求父 view 不要拦截事件
                requestDisallowInterceptTouchEvent(true)
                true
            }
            // move 时计算并分发滚动量
            MotionEvent.ACTION_MOVE -> {
                velocityTracker.addMovement(e)
                val dy = (lastPosition - e.y).toInt()
                lastPosition = e.y
                dispatchScroll(dy, ViewCompat.TYPE_TOUCH)
                true
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker.addMovement(e)
                velocityTracker.computeCurrentVelocity(1000)
                fling(-velocityTracker.yVelocity.toInt())
                velocityTracker.clear()
                true
            }
            else -> super.onTouchEvent(e)
        }
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
        nestedScrollChild = child
        nestedScrollTarget = target
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (dispatchScroll(dy, type)) {
            consumed[1] = dy
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
        dispatchScroll(dyUnconsumed, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        dispatchScroll(dyUnconsumed, type)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        fling(velocityY.toInt())
        return true
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
    }

    private fun fling(v: Int) {
        isFling = true
        lastPosition = 0F
        scroller.fling(lastPosition.toInt(), lastPosition.toInt(), v, v, Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE)
        invalidate()
    }

    private fun smoothScrollSelf(scroll: Int, duration: Int) {
        isFling = false
        lastPosition = 0F
        scroller.startScroll(
            lastPosition.toInt(), lastPosition.toInt(),
            scroll - scrollX, scroll -scrollY,
            duration
        )
        invalidate()
    }

    override fun computeScroll() {
        val sv = scrollVertical ?: return
        if (scroller.computeScrollOffset()) {
            val p = if (sv) {
                scroller.currY
            } else {
                scroller.currX
            }.toFloat()
            val scroll = (p - lastPosition).toInt()
            Log.d(javaClass.simpleName, "computeScroll $scroll")
            if (!dispatchScroll(scroll, ViewCompat.TYPE_NON_TOUCH)) {
                if (sv) {
                    nestedScrollTarget?.scrollBy(0, scroll)
                } else {
                    nestedScrollTarget?.scrollBy(scroll, 0)
                }
            }
            lastPosition = p
            invalidate()
        } else {
            isFling = false
        }
    }

    /**
     * 分发滚动量
     * @return 是否自己处理滚动量
     */
    private fun dispatchScroll(scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean {
        Log.d(javaClass.simpleName, "dispatchScroll $scroll ${type == ViewCompat.TYPE_TOUCH} ${nestedScrollTarget?.canScrollVertically(scroll)}")
        return when {
            // 默认 0 可以处理
            scroll == 0 -> true
            // 优先自己滚动且自己可以滚动，就自己滚动
            scrollSelfFirst(scroll, type) && canScrollSelf(scroll) -> {
                scrollSelf(scroll, type)
                true
            }
            // 判断 nestedScrollTarget 是否滚动
            shouldTargetScroll(scroll, type) -> false
            // nestedScrollTarget 不滚动就自己滚动
            else -> {
                switchTargetIfNeed(scroll, type)
                scrollSelf(scroll, type)
                true
            }
        }
    }

    private fun scrollSelfFirst(scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean {
        return behavior?.scrollSelfFirst(this, scroll, type) ?: false
    }

    private fun shouldTargetScroll(scroll: Int, @ViewCompat.NestedScrollType type: Int): Boolean {
        return (behavior?.shouldTargetScroll(this, scroll, type) ?: true)
                && nestedScrollTarget?.canScrollVertically(scroll) == true
    }

    private fun switchTargetIfNeed(scroll: Int, @ViewCompat.NestedScrollType type: Int) {
        if (ViewCompat.TYPE_NON_TOUCH != type || nestedScrollTarget?.canScrollVertically(scroll) == true) {
            return
        }
        val child = nestedScrollChild ?: return
        val index = views.indexOf(child) + if (scroll > 0) { 1 } else { -1 }
        if (index >= 0 && index < views.size) {
            nestedScrollChild = views[index]
            nestedScrollTarget = targets[index]?.invoke(this, scroll, type)
        }
    }

    private fun scrollSelf(scroll: Int, @ViewCompat.NestedScrollType type: Int) {
        if (behavior?.interceptScrollSelf(this, scroll, type) != true) {
            scrollBy(scroll, scroll)
        }
    }

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

    private fun getScrollByX(dx: Int): Int {
        val newX = scrollX + dx
        return when {
            scrollVertical != false -> scrollX
            scrollX > 0 -> newX.constrains(0, maxScroll)
            scrollX < 0 -> newX.constrains(minScroll, 0)
            else -> newX.constrains(minScroll, maxScroll)
        } - scrollX
    }

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
        lastScrollDir = when (scrollVertical) {
            true -> t - oldt
            false -> l - oldl
            else -> 0
        }
    }

}

/**
 * 嵌套滚动的目标 View
 * 参数分别时 [BehavioralNestedScrollLayout]、滚动量和 [ViewCompat.NestedScrollType]
 * 返回值可滚动的目标 View
 */
typealias NestedScrollTarget = (BehavioralNestedScrollLayout, Int, Int)->View

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
     * @param layout
     */
    fun afterLayout(layout: BehavioralNestedScrollLayout) {
        // do nothing
    }

    /**
     * 在 [layout] dispatchTouchEvent 时是否处理 touch 事件
     *
     * @param layout
     * @param e touch 事件
     * @return true -> 处理，会在 dispatchTouchEvent 中直接返回 true，false -> 直接返回 false，null -> 不关心，会执行默认逻辑
     */
    fun handleDispatchTouchEvent(layout: BehavioralNestedScrollLayout, e: MotionEvent): Boolean? = null

    /**
     * 在 [layout] onInterceptTouchEvent 时是否处理 touch 事件
     *
     * @param layout
     * @param e touch 事件
     * @return true -> 处理，会直接返回 true 拦截事件，false -> 不处理，会直接返回 false，null -> 不关心，会执行默认逻辑
     */
    fun handleInterceptTouchEvent(layout: BehavioralNestedScrollLayout, e: MotionEvent): Boolean? = null

    /**
     * 在 [layout] onTouchEvent 时是否处理 touch 事件
     *
     * @param layout
     * @param e touch 事件
     * @return true -> 处理，会直接返回 true，false -> 不处理，会直接返回 false，null -> 不关心，会执行默认逻辑
     */
    fun handleTouchEvent(layout: BehavioralNestedScrollLayout, e: MotionEvent): Boolean? = null

    /**
     * 在 [layout] 分发滚动量时，是否优先滚动自己
     *
     * @param layout
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return true -> 优先滚动自己，false -> 不优先
     */
    fun scrollSelfFirst(layout: BehavioralNestedScrollLayout, scroll: Int, @NestedScrollType type: Int): Boolean = false

    /**
     * 在 [layout] 分发滚动量时，是否允许子 View target 滚动
     *
     * @param layout
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return true -> 允许 target 滚动，false -> 不允许其滚动
     */
    fun shouldTargetScroll(layout: BehavioralNestedScrollLayout, scroll: Int, @NestedScrollType type: Int): Boolean = true

    /**
     * 在需要 [layout] 自身滚动时，是否拦截处理
     *
     * @param layout
     * @param scroll 滚动量
     * @param type 滚动类型
     * @return true -> 拦截下来自己处理，false -> 不拦截，让 [layout] 自己滚动
     */
    fun interceptScrollSelf(layout: BehavioralNestedScrollLayout, scroll: Int, @NestedScrollType type: Int): Boolean = false

}
