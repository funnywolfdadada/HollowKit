package com.funnywolf.hollowkit.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams

/**
 * 根据状态切换视图的布局，提供了状态切换时的动画
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/10/30
 */
open class StatefulLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 当前状态，可在子线程更新
     * 注：动画过程中的状态更新不会打断动画，但在动画结束时会自动往最新的状态切换
     */
    var state: Int = 0
        set(value) {
            if (Thread.currentThread() != Looper.getMainLooper().thread) {
                post { state = value }
                return
            }
            val from = field
            field = value
            updateState(from, value)
        }

    /**
     * 以 [state] 为 key，对应 view 为 value 的数组，使用方自己填充
     */
    val viewArray = SparseArray<View?>()

    /**
     * [state] 改变时的动画，默认提供尺寸和透明度动画
     */
    var transformAnimator: ((from: Int, to: Int)->Animator?)? = { from, to -> animator(viewArray[from], viewArray[to]) }

    private var updating = false
    private var oneShotOnLayout: (()->Unit)? = null

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        oneShotOnLayout?.invoke()
        oneShotOnLayout = null
    }

    private fun updateState(from: Int, to: Int) {
        // 去重，同样状态没必要再次更新
        if (from == to) {
            return
        }
        // 正在更新中则推出，更新结束时会自动更新到下一个状态
        if (updating) {
            return
        }
        // 更新中
        updating = true
        val toV = viewArray[to]
        // 先把 toV 添加进去
        if (toV != null && indexOfChild(toV) < 0) {
            addView(toV)
        }
        // 不可见不需要动画
        if (!isShown) {
            onUpdateEnd(from, to)
            return
        }
        // 在 onLayout 时可以拿到 toV 的尺寸，方便做动画
        oneShotOnLayout = {
            // 有动画就开始动画，在结束时执行 onUpdateEnd，没有就直接执行
            transformAnimator?.invoke(from, to)?.apply {
                addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        onUpdateEnd(from, to)
                    }
                })
                start()
            } ?: kotlin.run {
                onUpdateEnd(from, to)
            }
        }
        requestLayout()
    }

    private fun onUpdateEnd(from: Int, to: Int) {
        // 更新结束时要把原来的 view 移除，并尝试更新到当前的 state
        val fromV = viewArray[from]
        if (fromV != null && indexOfChild(fromV) >= 0) {
            removeView(fromV)
        }
        updating = false
        updateState(to, state)
    }

}

/**
 * 起止视图的动画
 */
fun StatefulLayout.animator(from: View?, to: View?): Animator? {
    val l = ArrayList<Animator>(2)
    sizeAnimator(from, to)?.also { l.add(it) }
    alphaAnimator(from, to)?.also { l.add(it) }
    return if (l.isEmpty()) {
        null
    } else {
        AnimatorSet().apply { playTogether(l) }
    }
}

/**
 * 起止视图的尺寸动画
 */
fun StatefulLayout.sizeAnimator(from: View?, to: View?): Animator? {
    // 记录起止尺寸
    val fromWidth = from?.width ?: 0
    val fromHeight = from?.height ?: 0
    val toWidth = to?.width ?: 0
    val toHeight = to?.height ?: 0

    val diffWidth = toWidth - fromWidth
    val diffHeight = toHeight - fromHeight
    // 没有变动就不需要动画
    if (diffWidth == 0 && diffHeight == 0) {
        return null
    }

    // 记录原始布局尺寸
    val originFromWidth = from?.layoutParams?.width ?: LayoutParams.WRAP_CONTENT
    val originFromHeight = from?.layoutParams?.height ?: LayoutParams.WRAP_CONTENT
    val originToWidth = to?.layoutParams?.width ?: LayoutParams.WRAP_CONTENT
    val originToHeight = to?.layoutParams?.height ?: LayoutParams.WRAP_CONTENT
    // 把 to 设置到起始尺寸（from 不需要）
    to?.setLayoutSize(fromWidth, fromHeight)
    return ValueAnimator.ofFloat(0F, 1F).apply {
        addUpdateListener {  v ->
            val p = v.animatedValue as Float
            val w = fromWidth + (p * diffWidth).toInt()
            val h = fromHeight + (p * diffHeight).toInt()

            from?.setLayoutSize(w, h)
            to?.setLayoutSize(w, h)
        }
        addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                // 动画结束设置回原始布局尺寸
                from?.setLayoutSize(originFromWidth, originFromHeight)
                to?.setLayoutSize(originToWidth, originToHeight)
            }
        })
    }
}

/**
 * 起止视图的透明度动画
 */
fun StatefulLayout.alphaAnimator(from: View?, to: View?): Animator? {
    if (from == null && to == null) {
        return null
    }
    return ValueAnimator.ofFloat(0F, 1F).apply {
        addUpdateListener {
            val p = it.animatedValue as Float
            from?.alpha = 1 - p
            to?.alpha = p
        }
    }
}
