package com.funnywolf.hollowkit.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.utils.dp2pix
import com.funnywolf.hollowkit.utils.setRoundRect
import kotlinx.android.synthetic.main.view_douban_toolbar.view.*

/**
 * 豆瓣详情页的标题栏
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/10
 */
class ToolbarView: ConstraintLayout {

    @FloatRange(from = 0.0, to = 1.0)
    var process: Float = 1F
        set(value) {
            val v = when {
                value < 0 -> 0F
                value > 1 -> 1F
                else -> value
            }
            updateProcess(v)
            field = v
        }

    private val bgView: View
    private val ivBack: View
    private val tvTitle: TextView
    private val ivMore: View
    private val detailLayout: View
    private val tvDetail: TextView
    private val ivPoster: ImageView

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_douban_toolbar, this)
        bgView = findViewById(R.id.bg_view)
        ivBack = findViewById(R.id.iv_back)
        tvTitle = findViewById(R.id.tv_title)
        ivMore = findViewById(R.id.iv_more)
        detailLayout = findViewById(R.id.detail_layout)
        tvDetail = findViewById(R.id.tv_detail)
        ivPoster = findViewById(R.id.iv_poster)

        ivPoster.setRoundRect(4.dp2pix(context).toFloat())
    }

    fun setup(title: CharSequence?, detail: CharSequence?, posterId: Int, bgColor: Int): ToolbarView {
        tvTitle.text = title
        tvDetail.text = detail
        ivPoster.visibility = if (posterId > 0) {
            ivPoster.setImageResource(posterId)
            View.VISIBLE
        } else {
            View.GONE
        }
        bgView.setBackgroundColor(bgColor)
        return this
    }

    fun setListeners(clickBack: OnClickListener?, clickMore: OnClickListener?) {
        ivBack.setOnClickListener(clickBack)
        ivMore.setOnClickListener(clickMore)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (process < 1) {
            detailLayout.y = detailLayout.height.toFloat()
        }
    }

    private fun updateProcess(v: Float) {
        bgView.alpha = v
        val titleY: Float
        val titleAlpha: Float
        val detailY: Float
        val detailAlpha: Float
        if (process != 1F && v == 1F) {
            titleY = -tvTitle.height.toFloat()
            titleAlpha = 0F
            detailY = 0F
            detailAlpha = 1F
        } else if (process == 1F && v != 1F) {
            titleY = 0F
            titleAlpha = 1F
            detailY = detailLayout.height.toFloat()
            detailAlpha = 0F
        } else {
            return
        }
        tvTitle.animate().y(titleY).alpha(titleAlpha).setDuration(100).start()
        detailLayout.animate().y(detailY).alpha(detailAlpha).setDuration(100).start()
    }

}
