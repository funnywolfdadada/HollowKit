package com.funnywolf.hollowkit.view.scroll.behavior

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.ViewCompat
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.utils.dp
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * 下拉刷新
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/14
 */
class PullRefreshBehavior(
    override val midView: View,
    override val midScrollTarget: NestedScrollTarget? = null
) : NestedScrollBehavior {

    private val refreshView = RefreshView(midView.context).also {
        it.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    override val scrollVertical: Boolean = true
    override val prevView: View? = refreshView
    override val prevScrollTarget: NestedScrollTarget? = null
    override val nextView: View? = null
    override val nextScrollTarget: NestedScrollTarget? = null

    private var bsvRef: WeakReference<BehavioralScrollView>? = null
    private val onScrollChanged: (BehavioralScrollView)->Unit = {
        refreshView.process = abs(it.currProcess())
    }

    var enable: Boolean = true
        set(value) {
            field = value
            if (!value) {
                isRefreshing = false
            }
        }

    var refreshListener: (()->Unit)? = null

    var isRefreshing: Boolean
        get() = refreshView.isRefreshing
        set(value) {
            if (value && value != refreshView.isRefreshing) {
                refreshListener?.invoke()
            }
            refreshView.isRefreshing = value
            bsvRef?.get()?.smoothScrollTo(if (value) { -refreshView.height / 2 } else { 0 })
        }

    override fun afterLayout(v: BehavioralScrollView) {
        super.afterLayout(v)
        bsvRef = WeakReference(v)
        v.onScrollChangedListener = onScrollChanged
    }

    override fun handleDispatchTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? {
        if (e.action == MotionEvent.ACTION_UP && !isRefreshing) {
            isRefreshing = abs(v.scrollY) > refreshView.refreshHeight
            return true
        }
        return super.handleDispatchTouchEvent(v, e)
    }

    override fun scrollSelfFirst(v: BehavioralScrollView, scroll: Int, type: Int): Boolean {
        return (v.scrollY != 0 && !isRefreshing) || (type == ViewCompat.TYPE_NON_TOUCH && !v.isFling)
    }

    override fun handleScrollSelf(v: BehavioralScrollView, scroll: Int, type: Int): Boolean {
        return when {
            isRefreshing -> type != ViewCompat.TYPE_NON_TOUCH || v.isFling
            type == ViewCompat.TYPE_TOUCH -> {
                v.scrollBy(0, scroll / if (scroll < 0) { 2 } else { 1 })
                true
            }
            v.isFling -> true
            else -> super.handleScrollSelf(v, scroll, type)
        }
    }

}

class RefreshView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val loadingView = ImageView(context)
    private val animator = ValueAnimator.ofFloat(0F, 360F).apply {
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener {
            loadingView.rotation = it.animatedValue as? Float ?: 0F
        }
    }

    val refreshHeight = (36 * 2).dp

    var isRefreshing: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                loadingView.alpha = 1F
                animator.start()
            } else {
                animator.cancel()
            }
        }

    var process = 0F
        set(value) {
            if (field == value) {
                return
            }
            field = value
            update()
        }

    init {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, refreshHeight * 2)
        setPadding(0, refreshHeight, 0, 0)
        setBackgroundColor(Color.BLACK)
        loadingView.setImageResource(R.drawable.ic_loading)
        loadingView.setPadding(refreshHeight / 4, refreshHeight / 4, refreshHeight / 4, refreshHeight / 4)
        addView(loadingView, LayoutParams(refreshHeight, refreshHeight).also {
            it.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        })
    }

    private fun update() {
        if (isRefreshing) {
            return
        }
        animator.cancel()
        loadingView.alpha = process * 3
        loadingView.rotation = process * 3 * 360
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }
}
