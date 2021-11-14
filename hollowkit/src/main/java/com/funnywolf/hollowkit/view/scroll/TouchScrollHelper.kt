package com.funnywolf.hollowkit.view.scroll

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.Scroller

import androidx.annotation.IntDef
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat

import kotlin.math.abs

/**
 * 处理 touch 事件进行滚动 or fling 的帮助类
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/11/14
 */
class TouchScrollHelper(
    context: Context,
    @ViewCompat.ScrollAxis var supportedScrollAxis: Int,
    private val handler: ScrollHandler
) {

    /**
     * 当前的滚动状态
     */
    @ScrollState
    var scrollState: Int = ScrollState.NONE
        private set

    /**
     * 当前的滚动方向，水平 or 垂直
     * 在 [ScrollState.DRAGGING] 时，会设置为开始拖拽时的手指移动较多的方向
     * 在 [ScrollState.ANIMATION]/[ScrollState.FLING] 时，会设置为 [smoothScroll]/[fling] 传入的值
     */
    @ViewCompat.ScrollAxis
    private var scrollAxis: Int = ViewCompat.SCROLL_AXIS_NONE

    /**
     * 上次触摸事件的 rawX 值
     */
    private var lastX = 0F
    /**
     * 上次触摸事件的 rawY 值
     */
    private var lastY = 0F
    /**
     * down 事件时滚动状态
     */
    private var downScrollState = ScrollState.NONE

    /**
     * 计算速度
     */
    private val velocityTracker: VelocityTracker by lazy(LazyThreadSafetyMode.NONE) {
        VelocityTracker.obtain()
    }

    /**
     * 用来处理松手时的连续滚动和动画
     */
    private val scroller = Scroller(context)
    private var animator: Animator? = null

    /**
     * 拦截事件的最小位移量
     */
    private val touchInterceptSlop: Int

    init {
        val vc = ViewConfiguration.get(context)
        touchInterceptSlop = vc.scaledTouchSlop
    }

    /**
     * 动画移动
     */
    fun smoothScroll(
        @ViewCompat.ScrollAxis axis: Int, dx: Int, dy: Int,
        duration: Int = 250, onEnd: (()->Unit)? = null
    ) {
        stopScroll()
        startScroll(ScrollState.ANIMATION, axis)
        scroller.startScroll(0, 0, dx, dy, duration)
        animator = startScrollerAnimator(scroller, onEnd)
    }

    /**
     * 以某个速度进行惯性滚动
     */
    fun fling(
        @ViewCompat.ScrollAxis axis: Int, vx: Int, vy: Int,
        onEnd: (()->Unit)? = null
    ) {
        stopScroll()
        startScroll(ScrollState.FLING, axis)
        scroller.fling(
            0, 0, vx, vy,
            Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE
        )
        animator = startScrollerAnimator(scroller, onEnd)
    }

    /**
     * 滚动动画
     */
    private fun startScrollerAnimator(
        scroller: Scroller,
        onEnd: (() -> Unit)?
    ) = ValueAnimator.ofInt(scroller.duration).also { animator ->
        var x = 0
        var y = 0
        animator.duration = scroller.duration.toLong()
        animator.addUpdateListener {
            scroller.computeScrollOffset()
            val newX = scroller.currX
            val newY = scroller.currY
            if (!scroll(newX - x, newY - y)) {
                animator.cancel()
            }
            x = newX
            y = newY
        }
        animator.doOnEnd {
            scroller.forceFinished(true)
            onStopScroll()
            onEnd?.invoke()
        }
        animator.start()
    }

    /**
     * 停止滚动
     */
    fun stopScroll() {
        // 停止当前的动画
        animator?.also {
            animator = null
            if (it.isRunning) {
                it.cancel()
            }
        }
        onStopScroll()
    }

    /**
     * 处理 touch 事件拦截
     */
    fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return when (e.action) {
            // down 时不拦截，但需要记录触点位置，寻找触点位置的 child 和支持嵌套滚动的 target
            MotionEvent.ACTION_DOWN -> {
                down(e)
                false
            }
            // move 时如果移动，就自己拦截
            MotionEvent.ACTION_MOVE -> {
                move(e)
                val dx = (e.rawX - lastX).toInt()
                val dy = (e.rawY - lastY).toInt()
                when {
                    supportedScrollAxis and ViewCompat.SCROLL_AXIS_HORIZONTAL != 0
                            && abs(dx) > touchInterceptSlop -> true
                    supportedScrollAxis and ViewCompat.SCROLL_AXIS_VERTICAL != 0
                            && abs(dy) > touchInterceptSlop -> true
                    else -> false
                }
            }
            MotionEvent.ACTION_UP -> {
                up(e, scrollState, scrollAxis)
                // down 的时候不是空闲状态（fling 或动画），down 会停止相关滚动
                // up 之前手指几乎未移动，为了避免触发子 view 的点击事件，拦截 up 事件
                if (downScrollState != ScrollState.NONE) {
                    abs(e.rawX - lastX) < touchInterceptSlop && abs(e.rawY - lastY) < touchInterceptSlop
                } else {
                    false
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                cancel(e, scrollState, scrollAxis)
                false
            }
            else -> false
        }
    }

    /**
     * 处理 touch 事件
     */
    fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> down(e)
            MotionEvent.ACTION_MOVE -> {
                move(e)
                val dx = (e.rawX - lastX).toInt()
                val dy = (e.rawY - lastY).toInt()
                // 不再 dragging 状态时判断是否要进行拖拽
                if (scrollState != ScrollState.DRAGGING) {
                    val absDx = abs(dx)
                    val absDy = abs(dy)
                    val axis = when {
                        supportedScrollAxis and ViewCompat.SCROLL_AXIS_HORIZONTAL != 0
                                && absDx > touchInterceptSlop
                                && absDx > absDy -> {
                            ViewCompat.SCROLL_AXIS_HORIZONTAL
                        }
                        supportedScrollAxis and ViewCompat.SCROLL_AXIS_VERTICAL != 0
                                && absDy > touchInterceptSlop
                                && absDy > absDx -> {
                            ViewCompat.SCROLL_AXIS_VERTICAL
                        }
                        else -> ViewCompat.SCROLL_AXIS_NONE
                    }
                    if (axis != ViewCompat.SCROLL_AXIS_NONE) {
                        lastX = e.rawX
                        lastY = e.rawY
                        startScroll(ScrollState.DRAGGING, axis)
                    }
                } else {
                    // dragging 时计算并分发滚动量
                    scroll(-dx, -dy)
                    lastX = e.rawX
                    lastY = e.rawY
                }
            }
            MotionEvent.ACTION_UP -> {
                val state = scrollState
                val axis = scrollAxis
                onStopScroll()
                up(e, state, axis)
            }
            MotionEvent.ACTION_CANCEL -> {
                val state = scrollState
                val axis = scrollAxis
                onStopScroll()
                cancel(e, state, axis)
            }
            else -> return false
        }
        return true
    }

    private fun down(e: MotionEvent) {
        velocityTracker.clear()
        velocityTracker.addMovement(e)
        lastX = e.rawX
        lastY = e.rawY
        // 记录 down 时的滚动状态
        downScrollState = scrollState
        // 停掉当前的滚动
        stopScroll()
        scrollAxis = ViewCompat.SCROLL_AXIS_NONE
        // 把 down 时的滚动状态也分发给 handler
        handler.onDown(e, downScrollState)
    }

    private fun move(e: MotionEvent) {
        velocityTracker.addMovement(e)
        handler.onMove(e, scrollState)
    }

    private fun startScroll(@ScrollState state: Int, @ViewCompat.ScrollAxis axis: Int) {
        // 开始滚动，设置滚动状态和方向
        scrollState = state
        scrollAxis = axis
        handler.onStartScroll(state, axis)
    }

    private fun scroll(scrollX: Int, scrollY: Int): Boolean {
        return handler.onScroll(scrollState, scrollAxis, scrollX, scrollY)
    }

    /**
     * 滚动停止，返回停止前的滚动状态
     */
    private fun onStopScroll() {
        val state = scrollState
        if (state != ScrollState.NONE) {
            scrollState = ScrollState.NONE
            handler.onStopScroll(state, scrollAxis)
        }
    }

    private fun up(e: MotionEvent, @ScrollState state: Int, @ViewCompat.ScrollAxis axis: Int) {
        velocityTracker.addMovement(e)
        var vx = 0
        var vy = 0
        // 抬起前正在拖拽，才计算速度
        if (state == ScrollState.DRAGGING) {
            velocityTracker.computeCurrentVelocity(1000)
            // velocityTracker 计算的是 touch 方向的移动速度，和 fling/scroll 的方向相反，所以这里要取反得到 fling 的速度
            vx = -velocityTracker.xVelocity.toInt()
            vy = -velocityTracker.yVelocity.toInt()
        }
        handler.onUp(e, state, axis, vx, vy)
    }

    private fun cancel(e: MotionEvent, @ScrollState state: Int, @ViewCompat.ScrollAxis axis: Int) {
        handler.onCancel(e, state, axis)
    }

    interface ScrollHandler {
        /**
         * 手指按下，[state] 为按下时的滚动状态
         */
        fun onDown(e: MotionEvent, @ScrollState state: Int)
        /**
         * 手指移动，[state] 为移动时的滚动状态
         */
        fun onMove(e: MotionEvent, @ScrollState state: Int)
        /**
         * 开始滚动，[state] 为要开始的滚动状态，[axis] 为滚动方向，用于判断当前在哪个方向上滚动
         * 在 [ScrollState.DRAGGING] 时，会设置为开始拖拽时的手指移动较多的方向
         * 在 [ScrollState.ANIMATION]/[ScrollState.FLING] 时，会设置为 [smoothScroll]/[fling] 传入的值
         */
        fun onStartScroll(@ScrollState state: Int, @ViewCompat.ScrollAxis axis: Int)
        /**
         * 处理滚动，返回是否消费，[state] 为当前的滚动状态，[axis] 为滚动方向，[sx] 和 [sy] 时 xy 轴上的滚动量
         */
        fun onScroll(@ScrollState state: Int, @ViewCompat.ScrollAxis axis: Int, sx: Int, sy: Int): Boolean
        /**
         * 滚动停止，[state] 为停止前的滚动状态，[axis] 为停止前的滚动方向
         */
        fun onStopScroll(@ScrollState state: Int, @ViewCompat.ScrollAxis axis: Int)
        /**
         * 手指抬起，[state] 为抬起时的滚动状态，[axis] 为抬起时的滚动方向
         * 进入 [ScrollState.DRAGGING] 后抬起，会计算 xy 轴的滚动速度 [vx] 和 [vy]
         */
        fun onUp(e: MotionEvent, @ScrollState state: Int, @ViewCompat.ScrollAxis axis: Int, vx: Int, vy: Int)
        /**
         * touch 事件取消，[state] 为取消时的滚动状态
         */
        fun onCancel(e: MotionEvent, @ScrollState state: Int, @ViewCompat.ScrollAxis axis: Int)
    }
}

/**
 * 用于描述正处于的滚动状态
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
         * 正在动画
         */
        const val ANIMATION = 2
        /**
         * 正在 fling
         */
        const val FLING = 3
    }
}
