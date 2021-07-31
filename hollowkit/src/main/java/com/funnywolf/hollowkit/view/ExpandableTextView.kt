package com.funnywolf.hollowkit.view

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView

/**
 * 支持展示展开的 TextView，是否展示展开只和 [getMaxLines] 有关
 * 正常设置 [setMaxLines] 即可，当末尾需要省略时，会展示展开的文案
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/7/31
 */
class ExpandableTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): AppCompatTextView(context, attrs, defStyleAttr) {

    /**
     * 保存原始文案
     */
    private var rawText: CharSequence? = text

    /**
     * 展开的文案和颜色
     */
    private var expandText: CharSequence? = null
    private var expandTextColor: Int = 0

    fun setExpandText(text: CharSequence?, color: Int): ExpandableTextView {
        expandText = text
        expandTextColor = color
        // 展开文案和颜色变更，需要更新下
        update()
        return this
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        rawText = text
        super.setText(text, type)
        // 文案变更，需要更新下
        update()
    }

    override fun setMaxLines(maxLines: Int) {
        super.setMaxLines(maxLines)
        // 最大行数影响省略号，需要更新下
        update()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        // padding 影响文案展示的宽度，需要更新下
        update()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 宽度没变时不需要更新
        if (measuredWidth != width) {
            update()
        }
    }

    private fun update() {
        // post 一下防止还没 layout 完，拿不到宽高
        post {
            val start = System.currentTimeMillis()
            super.setText(generateCurrentText(), BufferType.NORMAL)
            Log.d("ExpandableTextView", "update cost ${System.currentTimeMillis() - start}")
        }
    }

    private fun generateCurrentText(): CharSequence? {
        // 不限制最大行数，不需要展开
        if (maxLines <= 0 || maxLines == Int.MAX_VALUE) {
            return rawText
        }
        val rawText = rawText
        val expandText = expandText
        if (rawText.isNullOrEmpty() || expandText.isNullOrEmpty()) {
            return rawText
        }
        // 计算行宽，非法时返回原文案
        val lineWidth = width - paddingLeft - paddingRight
        if (lineWidth < 0) {
            return rawText
        }
        // 构造 StaticLayout 用于测量
        val layout = StaticLayout.Builder.obtain(rawText, 0, rawText.length, paint, lineWidth)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        // 没有行数，或者最后一行也没有被省略，说明不需要展开，返回原始文案
        if (layout.lineCount <= 0 || layout.getEllipsisCount(layout.lineCount - 1) <= 0) {
            return rawText
        }
        // 最后一行有省略，需要展示展开
        val newText = SpannableStringBuilder()
        // 如果至少有两行文案，最后一行之前的原样保留
        if (layout.lineCount >= 2) {
            newText.append(rawText, 0, layout.getLineStart(layout.lineCount - 1))
        }
        // 计算最后一行的文案，在行宽减去省略号和展开文案宽度后的结果
        TextUtils.ellipsize(
            // 最后一样的文案
            rawText.subSequence(layout.getLineStart(layout.lineCount - 1), layout.getLineEnd(layout.lineCount - 1)),
            paint,
            // 可用宽度 = 行宽 - 展开文案的宽度
            lineWidth - paint.measureText(expandText, 0, expandText.length),
            TextUtils.TruncateAt.END
        ).also {
            // 得到最后一行的文案
            newText.append(it)
        }
        // 添加上展开文案
        newText.append(expandText, ForegroundColorSpan(expandTextColor), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        return newText
    }

}
