package com.funnywolf.hollowkit.app.fragments.douban.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import com.funnywolf.hollowkit.app.databinding.ViewDoubanToolbarBinding
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.view.setRoundRect

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

    private val binding = ViewDoubanToolbarBinding.inflate(LayoutInflater.from(context), this)

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        binding.ivPoster.setRoundRect(4.dp.toFloat())
    }

    fun setup(title: CharSequence?, detail: CharSequence?, posterId: Int, bgColor: Int): ToolbarView {
        binding.tvTitle.text = title
        binding.tvDetail.text = detail
        binding.ivPoster.visibility = if (posterId > 0) {
            binding.ivPoster.setImageResource(posterId)
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.bgView.setBackgroundColor(bgColor)
        return this
    }

    fun setListeners(clickBack: OnClickListener?, clickMore: OnClickListener?) {
        binding.ivBack.setOnClickListener(clickBack)
        binding.ivMore.setOnClickListener(clickMore)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (process < 1) {
            binding.detailLayout.y = binding.detailLayout.height.toFloat()
        }
    }

    private fun updateProcess(v: Float) {
        binding.bgView.alpha = v
        val titleY: Float
        val titleAlpha: Float
        val detailY: Float
        val detailAlpha: Float
        if (process != 1F && v == 1F) {
            titleY = -binding.tvTitle.height.toFloat()
            titleAlpha = 0F
            detailY = 0F
            detailAlpha = 1F
        } else if (process == 1F && v != 1F) {
            titleY = 0F
            titleAlpha = 1F
            detailY = binding.detailLayout.height.toFloat()
            detailAlpha = 0F
        } else {
            return
        }
        binding.tvTitle.animate().y(titleY).alpha(titleAlpha).setDuration(100).start()
        binding.detailLayout.animate().y(detailY).alpha(detailAlpha).setDuration(100).start()
    }

}
