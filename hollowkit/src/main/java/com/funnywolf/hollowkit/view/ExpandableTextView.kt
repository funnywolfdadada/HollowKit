package com.funnywolf.hollowkit.view

import android.content.Context
import android.os.Build
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView

/**
 * 支持展示展开的 TextView，是否展示展开只和 [getMaxLines] 有关
 * 正常设置 [setMaxLines] 即可，当末尾需要省略时，会展示展开的文本
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/7/31
 */
class ExpandableTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): AppCompatTextView(context, attrs, defStyleAttr), Runnable {

    /**
     * 保存原始文本
     */
    private var rawText: CharSequence? = text
    private var lastWidth = 0

    /**
     * 展开的文本
     */
    var expandText: CharSequence? = null
        set(value) {
            field = value
            // 展开文本变更，需要更新下
            update()
        }

    override fun setText(text: CharSequence?, type: BufferType?) {
        rawText = text
        super.setText(text, type)
        // 文本变更，需要更新下
        update()
    }

    override fun setMaxLines(maxLines: Int) {
        super.setMaxLines(maxLines)
        // 最大行数影响省略号，需要更新下
        update()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        // padding 影响文本展示的宽度，需要更新下
        update()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 宽度变时需要更新
        if (lastWidth != width) {
            lastWidth = width
            update()
        }
    }

    private fun update() {
        if (width > 0) {
            run()
        } else {
            // post 一下防止还没 layout 完，拿不到宽高
            post(this)
        }
    }

    override fun run() {
        val start = System.currentTimeMillis()
        super.setText(generateCurrentText(), BufferType.NORMAL)
        Log.d("ExpandableTextView", "update cost ${System.currentTimeMillis() - start}")
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
        // 计算行宽，非法时返回原文本
        val lineWidth = width - paddingLeft - paddingRight
        if (lineWidth < 0) {
            return rawText
        }
        // 构造 StaticLayout 用于测量
        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(rawText, 0, rawText.length, paint, lineWidth).build()
        } else {
            StaticLayout(rawText, paint, lineWidth, Layout.Alignment.ALIGN_NORMAL, 1F, 0F, true)
        }
        // 小于最大行数，不需要展开，返回原文本
        if (layout.lineCount < maxLines) {
            return rawText
        }
        // 构造新文本，计算最后一行是否有省略，是否展示展开
        val newText = SpannableStringBuilder()
        // 如果至少有两行文本，最后一行之前的原样保留
        if (layout.lineCount >= 2) {
            newText.append(rawText, 0, layout.getLineStart(maxLines - 1))
        }
        // 计算最后一行的文本，在行宽减去省略号和展开文本宽度后的结果
        val lastLine = rawText.subSequence(layout.getLineStart(maxLines - 1), layout.getLineEnd(maxLines - 1))
        val ellipsizedLastLine = TextUtils.ellipsize(
            lastLine,
            paint,
            // 可用宽度 = 行宽 - 展开文本的宽度
            lineWidth - paint.measureText(expandText, 0, expandText.length),
            TextUtils.TruncateAt.END
        )
        // 添加最后一行的文本
        newText.append(ellipsizedLastLine)
        // 判断最后一行文本是否被省略，省略的话加上展开文本
        if (ellipsizedLastLine != lastLine) {
            // 添加上展开文本
            newText.append(expandText)
        }
        return newText
    }

}
