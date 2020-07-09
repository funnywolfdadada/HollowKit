package com.funnywolf.hollowkit.view.scroll.behavior

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Space
import androidx.core.view.ViewCompat
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.utils.dp
import java.lang.ref.WeakReference

/**
 * 有位移的下拉刷新
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/14
 */
class PullRefreshBehavior(
    contentView: View,
    private var refreshListener: ((PullRefreshBehavior)->Unit)? = null
) : NestedScrollBehavior {

    var enable: Boolean = true
        set(value) {
            field = value
            if (!value) {
                isRefreshing = false
            }
        }

    var isRefreshing: Boolean = false
        set(value) {
            if (value && value != field) {
                refreshListener?.invoke(this)
            }
            field = value
            bsvRef?.get()?.also {
                it.smoothScrollTo(if (value) { 0 } else { it.maxScroll })
            }
            if (value) {
                refreshView.loadingView.alpha = 1F
                refreshView.animator.start()
            } else {
                refreshView.animator.cancel()
            }
        }

    private val refreshHeight = 72.dp

    val refreshView = RefreshView(contentView.context).also {
        it.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, refreshHeight)
        it.loadingView.layoutParams?.also { lp ->
            lp.height = refreshHeight / 2
            lp.width = refreshHeight / 2
        }
    }

    private var process: Float = 0F
        set(value) {
            if (isRefreshing) {
                return
            }
            field = value
            refreshView.animator.cancel()
            refreshView.loadingView.alpha = process * 3
            refreshView.loadingView.rotation = process * 3 * 360
        }

    private var bsvRef: WeakReference<BehavioralScrollView>? = null
    private val onScrollChanged: (BehavioralScrollView)->Unit = {
        process = (it.maxScroll - it.scrollY) / refreshHeight.toFloat()
    }

    override val scrollAxis: Int = ViewCompat.SCROLL_AXIS_VERTICAL
    override val prevView: View? = Space(contentView.context).also {
        it.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, refreshHeight)
    }
    override val midView: View = refreshView
    override val nextView: View? = contentView

    override fun afterLayout(v: BehavioralScrollView) {
        super.afterLayout(v)
        v.scrollTo(0, v.maxScroll)
        bsvRef = WeakReference(v)
        v.onScrollChangedListeners.add(onScrollChanged)
    }

    override fun handleDispatchTouchEvent(v: BehavioralScrollView, e: MotionEvent): Boolean? {
        return when {
            !enable -> null
            // 抬手时，如果头部已经滚出来了，且未刷新，则根据滚出的距离设置刷新状态
            (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_CANCEL)
                    && v.scrollY < v.maxScroll
                    && !isRefreshing -> {
                isRefreshing = v.scrollY < 0
                true
            }
            else -> null
        }
    }

    override fun handleNestedPreScrollFirst(v: BehavioralScrollView, scroll: Int, type: Int): Boolean? {
        val handle = when {
            !enable -> null
            v.state == NestedScrollState.ANIMATION -> null
            // 只在自身发生滚动，且不在刷新过程中，即拖拽头部 view 的过程中，优先自己处理
            v.scrollY < v.maxScroll -> true
            else -> null
        }
        v.log("handleNestedPreScrollFirst $handle, state = ${v.state}, type = $type, isRefreshing = $isRefreshing")
        return handle
    }

    override fun handleNestedScrollFirst(
        v: BehavioralScrollView,
        scroll: Int,
        type: Int
    ): Boolean? {
        return if (v.state == NestedScrollState.ANIMATION) {
            null
        } else {
            false
        }
    }

    override fun handleScrollSelf(v: BehavioralScrollView, scroll: Int, type: Int): Boolean? {
        val handle = when {
            !enable -> false
            v.state == NestedScrollState.ANIMATION -> null
            isRefreshing -> if (v.scrollY < 0 || (v.scrollY == 0 && scroll < 0)) {
                false
            } else {
                null
            }
            // 不处理 non touch 滚动量
            type == ViewCompat.TYPE_NON_TOUCH -> false
            // touch 类型的滚动都要拦下来
            type == ViewCompat.TYPE_TOUCH -> {
                // 不再刷新中的就滚动自身，根据滚动方向决定是否添加阻尼效果
                v.scrollBy(0, scroll / if (scroll < 0) { 2 } else { 1 })
                true
            }
            // 非 touch 的且不是动画滚动不处理
            else -> false
        }
        v.log("handleScrollSelf $handle, state = ${v.state}, type = $type, isRefreshing = $isRefreshing")
        return handle
    }

}

class RefreshView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    val loadingView = ImageView(context)

    val animator: ValueAnimator = ValueAnimator.ofFloat(0F, 360F).apply {
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener {
            loadingView.rotation = it.animatedValue as? Float ?: 0F
        }
    }

    init {
        loadingView.setImageResource(R.drawable.ic_loading)
        addView(loadingView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
            it.gravity = Gravity.CENTER
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }
}
