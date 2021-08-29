package com.funnywolf.hollowkit.app.fragments.douban.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Scroller
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import com.funnywolf.hollowkit.view.constrains
import com.funnywolf.hollowkit.view.findHorizontalNestedScrollingTarget
import com.funnywolf.hollowkit.view.findVerticalNestedScrollingTarget
import kotlin.math.abs

/**
 * 未知区域
 */
const val JELLY_REGION_NONE = 0
/**
 * 顶部的区域
 */
const val JELLY_REGION_TOP = 1
/**
 * 底部的区域
 */
const val JELLY_REGION_BOTTOM = 2
/**
 * 左边的区域
 */
const val JELLY_REGION_LEFT = 3
/**
 * 右边的区域
 */
const val JELLY_REGION_RIGHT = 4

@IntDef(JELLY_REGION_NONE, JELLY_REGION_TOP, JELLY_REGION_BOTTOM, JELLY_REGION_LEFT, JELLY_REGION_RIGHT)
@Retention(AnnotationRetention.SOURCE)
annotation class JellyRegion

/**
 * 果冻一般的弹性视图
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/4
 */

class JellyLayout : FrameLayout, NestedScrollingParent2 {

    /**
     * 当前滚动所在的区域，一次只支持在一个区域滚动
     */
    @JellyRegion
    var currRegion = JELLY_REGION_NONE
        get() = when {
            scrollY < 0 -> JELLY_REGION_TOP
            scrollY > 0 -> JELLY_REGION_BOTTOM
            scrollX < 0 -> JELLY_REGION_LEFT
            scrollX > 0 -> JELLY_REGION_RIGHT
            else -> JELLY_REGION_NONE
        }
        private set

    /**
     * 当前区域的滚动进度
     */
    @FloatRange(from = 0.0, to = 1.0)
    var currProcess = 0F
        get() = when {
            scrollY < 0 -> if (minScrollY != 0) { scrollY.toFloat() / minScrollY } else { 0F }
            scrollY > 0 -> if (maxScrollY != 0) { scrollY.toFloat() / maxScrollY } else { 0F }
            scrollX < 0 -> if (minScrollX != 0) { scrollX.toFloat() / minScrollX } else { 0F }
            scrollX > 0 -> if (maxScrollX != 0) { scrollX.toFloat() / maxScrollX } else { 0F }
            else -> 0F
        }
        private set

    /**
     * 上次 x 轴的滚动方向，主要用来判断是否发生了滚动
     */
    var lastScrollXDir: Int = 0
        private set

    /**
     * 上次 y 轴的滚动方向
     */
    var lastScrollYDir: Int = 0
        private set

    /**
     * 发生滚动时的回调
     */
    var onScrollChangedListener: ((JellyLayout)->Unit)? = null

    /**
     * 复位时的回调，返回是否拦截处理复位事件
     */
    var onResetListener: ((JellyLayout)->Boolean)? = null

    /**
     * 复位时的动画时间
     */
    var resetDuration: Int = 500

    /**
     * 滚动的阻尼
     */
    var resistence = 2F

    private var topView: View? = null
    private var bottomView: View? = null
    private var leftView: View? = null
    private var rightView: View? = null

    /**
     * x 轴滚动的最小值 = -左边视图宽度
     */
    private var minScrollX = 0

    /**
     * x 轴滚动的最大值 = 右边视图宽度
     */
    private var maxScrollX = 0

    /**
     * y 轴滚动的最小值 = -顶部视图高度
     */
    private var minScrollY = 0

    /**
     * y 轴滚动的最大值 = 底部视图高度
     */
    private var maxScrollY = 0

    /**
     * 上次触摸事件的 x 值，用于处理自身的滑动事件
     */
    private var lastX = 0F

    /**
     * 上次触摸事件的 y 值，用于处理自身的滑动事件
     */
    private var lastY = 0F

    /**
     * 用来处理松手时的连续滚动
     */
    private val scroller = Scroller(context)

    private val parentHelper = NestedScrollingParentHelper(this)

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    fun setTopView(v: View?): JellyLayout {
        removeView(topView)
        topView = v
        if (v != null) { addView(v) }
        return this
    }

    fun setBottomView(v: View?): JellyLayout {
        removeView(bottomView)
        bottomView = v
        if (v != null) { addView(v) }
        return this
    }

    fun setLeftView(v: View?): JellyLayout {
        removeView(leftView)
        leftView = v
        if (v != null) { addView(v) }
        return this
    }

    fun setRightView(v: View?): JellyLayout {
        removeView(rightView)
        rightView = v
        if (v != null) { addView(v) }
        return this
    }

    fun setProcess(
            @JellyRegion region: Int,
            @FloatRange(from = 0.0, to = 1.0) process: Float = 0F,
            smoothly: Boolean = true
    ) {
        var x = 0
        var y = 0
        when (region) {
            JELLY_REGION_TOP -> y = (minScrollY * process).toInt()
            JELLY_REGION_BOTTOM -> y = (maxScrollY * process).toInt()
            JELLY_REGION_LEFT -> x = (minScrollX * process).toInt()
            JELLY_REGION_RIGHT -> x = (maxScrollX * process).toInt()
        }
        if (smoothly) {
            smoothScrollTo(x, y)
        } else {
            scrollTo(x, y)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        topView?.also {
            it.x = (width - it.width) / 2F
            it.y = -it.height.toFloat()
        }
        bottomView?.also {
            it.x = (width - it.width) / 2F
            it.y = height.toFloat()
        }
        leftView?.also {
            it.x = -it.width.toFloat()
            it.y = (height - it.height) / 2F
        }
        rightView?.also {
            it.x = width.toFloat()
            it.y = (height - it.height) / 2F
        }
        minScrollX = -(leftView?.width ?: 0)
        maxScrollX = rightView?.width ?: 0
        minScrollY = -(topView?.height ?: 0)
        maxScrollY = bottomView?.height ?: 0
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            // down 时停掉 scroller 的滚动，复位滚动方向
            MotionEvent.ACTION_DOWN -> {
                scroller.abortAnimation()
                lastScrollXDir = 0
                lastScrollYDir = 0
            }
            // up 或 cancel 时复位到原始位置，被拦截就不再处理
            // 在这里处理是因为自身可能并没有处理任何 touch 事件，也就不能在 onToucheEvent 中处理到 up 事件
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                // 发生了移动，且不处于复位状态，且未被拦截，则执行复位操作
                if ((lastScrollXDir != 0 || lastScrollYDir != 0)
                    && currRegion != JELLY_REGION_NONE
                    && onResetListener?.invoke(this) != true) {
                    smoothScrollTo(0, 0)
                }
            }
        }
        return super.dispatchTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            // down 时不拦截，但需要记录位置
            MotionEvent.ACTION_DOWN -> {
                lastX = e.x
                lastY = e.y
                false
            }
            // move 时需要根据是否移动，是否有可处理对应方向移动的子 view，判断是否要自己拦截
            MotionEvent.ACTION_MOVE -> {
                val dx = (lastX - e.x).toInt()
                val dy = (lastY - e.y).toInt()
                lastX = e.x
                lastY = e.y
                if (dx == 0 && dy == 0) {
                    false
                } else {
                    val target = if (abs(dx) > abs(dy)) {
                        findHorizontalNestedScrollingTarget(e.rawX, e.rawY)
                    } else {
                        findVerticalNestedScrollingTarget(e.rawX, e.rawY)
                    }
                    target == null
                }

            }
            else -> super.onInterceptTouchEvent(e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            // down 不处理，之后的就无法处理
            MotionEvent.ACTION_DOWN -> {
                lastX = e.x
                lastY = e.y
                true
            }
            // move 时判断自身是否能够处理
            MotionEvent.ACTION_MOVE -> {
                val dx = (lastX - e.x).toInt()
                val dy = (lastY - e.y).toInt()
                lastX = e.x
                lastY = e.y
                if (dispatchScroll(dx, dy)) {
                    // 自己可以处理就请求父 view 不要拦截事件
                    requestDisallowInterceptTouchEvent(true)
                    true
                } else {
                    false
                }
            }
            else -> super.onTouchEvent(e)
        }
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        // 只处理 touch 相关的滚动
        return type == ViewCompat.TYPE_TOUCH
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    /**
     * 根据滚动区域和新的滚动量确定是否消耗 target 的滚动，滚动区域和处理优先级关系：
     * [JELLY_REGION_TOP] 或 [JELLY_REGION_BOTTOM] -> 自己优先处理 y 轴滚动
     * [JELLY_REGION_LEFT] 或 [JELLY_REGION_RIGHT] -> 自己优先处理 x 轴滚动
     */
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        when (currRegion) {
            JELLY_REGION_TOP, JELLY_REGION_BOTTOM -> if (canScrollVertically(dy)) {
                consumed[1] = dy
            }
            JELLY_REGION_LEFT, JELLY_REGION_RIGHT -> if (canScrollHorizontally(dx)) {
                consumed[0] = dx
            }
        }
        dispatchScroll(consumed[0], consumed[1])
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        dispatchScroll(dxUnconsumed, dyUnconsumed)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
    }

    /**
     * 分发滚动量，当滚动区域已知时，只处理对应方向上的滚动，未知时先通过滚动量确定方向，再滚动
     */
    private fun dispatchScroll(dScrollX: Int, dScrollY: Int): Boolean {
        val dx = (dScrollX / resistence).toInt()
        val dy = (dScrollY / resistence).toInt()
        if (dx == 0 && dy == 0) {
            return true
        }
        val horizontal = when (currRegion) {
            JELLY_REGION_TOP, JELLY_REGION_BOTTOM -> false
            JELLY_REGION_LEFT, JELLY_REGION_RIGHT -> true
            else -> abs(dScrollX) > abs(dScrollY)
        }
        return if (horizontal) {
            if (canScrollHorizontally(dx)) {
                scrollBy(dx, 0)
                true
            } else {
                false
            }
        } else {
            if (canScrollVertically(dy)) {
                scrollBy(0, dy)
                true
            } else {
                false
            }
        }
    }

    /**
     * 利用 scroller 平滑滚动
     */
    private fun smoothScrollTo(x: Int, y: Int) {
        if (scrollX == x && scrollY == y) {
            return
        }
        scroller.startScroll(scrollX, scrollY, x - scrollX, y - scrollY, resetDuration)
        invalidate()
    }

    /**
     * 计算并滚到需要滚动到的位置
     */
    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            invalidate()
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return if (direction > 0) {
            scrollX < maxScrollX
        } else {
            scrollX > minScrollX
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return if (direction > 0) {
            scrollY < maxScrollY
        } else {
            scrollY > minScrollY
        }
    }

    /**
     * 具体滚动的限制取决于当前的滚动区域 [currRegion]，这里的区域判断分得很细，可以使得一次只处理一个区域的滚动，
     * 否则会存在在临界位置的一次大的滚动导致滚过了的问题。
     * 具体规则:
     * [JELLY_REGION_LEFT] -> 只能在水平 [[minScrollX], 0] 范围内滚动
     * [JELLY_REGION_RIGHT] -> 只能在水平 [0, [maxScrollX]] 范围内滚动
     * [JELLY_REGION_TOP] -> 只能在垂直 [[minScrollY], 0] 范围内滚动
     * [JELLY_REGION_BOTTOM] -> 只能在垂直 [0, [maxScrollY]] 范围内滚动
     * [JELLY_REGION_NONE] -> 水平是在 [[minScrollX], [maxScrollX]] 范围内，垂直在 [[minScrollY], [maxScrollY]]
     */
    override fun scrollTo(x: Int, y: Int) {
        val region = currRegion
        val xx = when(region) {
            JELLY_REGION_LEFT -> x.constrains(minScrollX, 0)
            JELLY_REGION_RIGHT -> x.constrains(0, maxScrollX)
            else -> x.constrains(minScrollX, maxScrollX)
        }
        val yy = when(region) {
            JELLY_REGION_TOP -> y.constrains(minScrollY, 0)
            JELLY_REGION_BOTTOM -> y.constrains(0, maxScrollY)
            else -> y.constrains(minScrollY, maxScrollY)
        }
        super.scrollTo(xx, yy)
    }

    /**
     * 当滚动位置发生变化时，分发滚动区域和对应方向上的滚动百分比
     */
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        lastScrollXDir = l - oldl
        lastScrollYDir = t - oldt
        onScrollChangedListener?.invoke(this)
    }

}


