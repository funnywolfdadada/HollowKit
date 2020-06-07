package com.funnywolf.hollowkit.richtext

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import androidx.annotation.IntDef

/**
 * 统一装饰 span，支持：
 * - 文本替换和画笔设置
 * - 上下左右的 padding 和 margin
 * - 整体的对其方式
 * - 背景图片
 * - 前置图片和后置图片及其大小设置
 * - 替换图片及其大小设置
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/30
 */
class UniDecorSpan: ReplacementSpan() {

    /**
     * 整体对齐方式
     */
    @TextAlign var align = TextAlign.BASELINE

    /**
     * 文本的 padding，top 和 bottom 会影响字体的高度和 [fmi]，left 和 right 影响距离左右图片的距离
     */
    val padding = Rect(0, 0, 0, 0)

    /**
     * 文本的 margin，top 和 bottom 不影响实际高度，left 和 right 是富文本距离左右文本的距离
     */
    val margin = Rect(0, 0, 0, 0)

    /**
     * 提供文本的替换和颜色大小
     */
    var textProvider: TextProvider? = null

    /**
     * 背景图，背景图的宽高不受 provider 控制
     * 高度等于 [fmi] 的 ascent 到 descent 距离
     * 宽度等于整体宽度 [totalWidth] 减去左右的 margin
     */
    var backgroundDrawable: DrawableProvider? = null

    /**
     * 替换文本的图片
     */
    var replacementDrawable: DrawableProvider? = null
    private var replacementBaselineShift = 0

    /**
     * 文本左图
     */
    var leftDrawable: DrawableProvider? = null
    private var leftBaselineShift = 0

    /**
     * 文本右图
     */
    var rightDrawable: DrawableProvider? = null
    private var rightBaselineShift = 0

    /**
     * 是否绘制 [fmi] 的各个参数的实际位置，方便 debug
     */
    var drawFontMatrix = false

    /**
     * 经过替换后真正要绘制的文本
     */
    private var realText: String? = null

    /**
     * 文本的字体尺寸信息
     */
    private var fmi = Paint.FontMetricsInt()

    /**
     * 文本左边起始位置的偏移
     */
    private var textLeftShift = 0

    /**
     * 富文本的总体宽度，包括 [padding]、[margin]、[leftDrawable]、[rightDrawable]、文本宽度或 [replacementDrawable]
     */
    private var totalWidth = 0

    /**
     * 根据对齐方式 [align]，计算的基线偏移量，用于绘制文本
     */
    private var baselineShift = 0

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        realText = getRealText(text, start, end)
        if (realText.isNullOrEmpty()) {
            return 0
        }
        measure(paint, fm)
        return totalWidth
    }

    private fun getRealText(text: CharSequence?, start: Int, end: Int): String? {
        text ?: return null
        val s = text.subSequence(start, end).toString()
        return if (textProvider != null) {
            textProvider?.text(s)
        } else {
            s
        }
    }

    private fun measure(paint: Paint, outFmi: Paint.FontMetricsInt?) {
        // 保留原始 fontMetrics
        val originFmi = paint.fontMetricsInt
        // 设置文本大小
        textProvider?.also {
            it.setupPaint(paint)
        }
        // 计算需要的 fontMetrics
        calculateFmi(originFmi, paint, outFmi)
        // 计算总体宽度
        totalWidth = margin.left + margin.right + padding.left + padding.right
        // 文本左边偏移
        textLeftShift = margin.left + padding.left
        // 计算字体或替换图片的宽度和基线偏移
        replacementDrawable?.also {
            replacementBaselineShift = calculateDrawableShift(it.height(fmi), it.align())
            totalWidth += it.width(fmi)
        } ?: realText?.also {
            totalWidth += paint.measureText(it).toInt()
        }
        // 计算图片宽度和基线偏移
        leftDrawable?.also {
            leftBaselineShift = calculateDrawableShift(it.height(fmi), it.align())
            val width = it.width(fmi)
            totalWidth += width
            // 更新文本左边的偏移
            textLeftShift += width
        }
        rightDrawable?.also {
            rightBaselineShift = calculateDrawableShift(it.height(fmi), it.align())
            val width = it.width(fmi)
            totalWidth += width
        }
    }

    private fun calculateFmi(originFmi: Paint.FontMetricsInt, paint: Paint, outFmi: Paint.FontMetricsInt?) {
        // 更新并结合 padding 和 margin 计算 font matrix
        paint.getFontMetricsInt(fmi)
        fmi.top -= padding.top + margin.top
        fmi.ascent -= padding.top
        fmi.descent += padding.bottom
        fmi.bottom += padding.bottom + margin.bottom

        // 根据对齐方式计算基线的偏移量
        baselineShift = when (align) {
            TextAlign.ASCENT -> originFmi.ascent - fmi.ascent
            TextAlign.CENTER -> {
                val heightOffset = (fmi.descent - fmi.ascent) - (originFmi.descent - originFmi.ascent)
                heightOffset / 2 - (fmi.descent - originFmi.descent)
            }
            TextAlign.DESCENT -> originFmi.descent - fmi.descent
            else -> 0
        }

        // 根据基线偏移更新 font matrix
        fmi.top += baselineShift
        fmi.ascent += baselineShift
        fmi.descent += baselineShift
        fmi.bottom += baselineShift

        // 更新输出的 font matrix
        outFmi?.apply {
            top = fmi.top
            ascent = fmi.ascent
            descent = fmi.descent
            bottom = fmi.bottom
            leading = fmi.leading
        }
    }

    private fun calculateDrawableShift(height: Int, @TextAlign align: Int): Int {
        return when (align) {
            TextAlign.ASCENT -> fmi.ascent
            TextAlign.DESCENT -> fmi.descent - height
            // 其他情况都居中对齐
            else -> {
                val heightOffset = (fmi.descent - fmi.ascent) - height
                fmi.ascent + heightOffset / 2
            }
        }
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val t = realText
        if (t.isNullOrEmpty()) {
            return
        }
        val xi = x.toInt()

        drawBackground(canvas, xi, y)
        drawLeft(canvas, xi, y)
        drawRight(canvas, xi, y)
        if (replacementDrawable != null) {
            drawReplacement(canvas, xi, y)
        } else {
            drawText(t, canvas, paint, x, y.toFloat())
        }

        if (drawFontMatrix) {
            canvas.drawLine(x, y.toFloat() + fmi.top, x + totalWidth, y.toFloat() + fmi.top, paint)
            canvas.drawLine(x, y.toFloat() + fmi.ascent, x + totalWidth, y.toFloat() + fmi.ascent, paint)
            canvas.drawLine(x, y.toFloat() + fmi.descent, x + totalWidth, y.toFloat() + fmi.descent, paint)
            canvas.drawLine(x, y.toFloat() + fmi.bottom, x + totalWidth, y.toFloat() + fmi.bottom, paint)
            canvas.drawLine(x, y.toFloat() + baselineShift, x + totalWidth, y.toFloat() + baselineShift, paint)
        }
    }

    private fun drawBackground(c: Canvas, x: Int, y: Int) {
        val dp = backgroundDrawable ?: return
        val d = dp.drawable()
        // 背景的尺寸只受 font matrix 和 padding 影响
        d.setBounds(
            x + margin.left,
            y + fmi.ascent,
            x + totalWidth - margin.right,
            y + fmi.descent
        )
        d.draw(c)
    }

    private fun drawLeft(c: Canvas, x: Int, y: Int) {
        val dp = leftDrawable ?: return
        val d = dp.drawable()
        val left = x + margin.left
        val top = y + leftBaselineShift
        val width = dp.width(fmi)
        val height = dp.height(fmi)
        d.setBounds(left, top, left + width, top + height)
        d.draw(c)
    }

    private fun drawRight(c: Canvas, x: Int, y: Int) {
        val dp = rightDrawable ?: return
        val d = dp.drawable()
        val right = x + totalWidth - margin.right
        val top = y + rightBaselineShift
        val width = dp.width(fmi)
        val height = dp.height(fmi)
        d.setBounds(right - width, top, right, top + height)
        d.draw(c)
    }

    private fun drawReplacement(c: Canvas, x: Int, y: Int) {
        val dp = replacementDrawable ?: return
        val d = dp.drawable()
        val left = x + textLeftShift
        val top = y + replacementBaselineShift
        val width = dp.width(fmi)
        val height = dp.height(fmi)
        d.setBounds(left, top, left + width, top + height)
        d.draw(c)
    }

    private fun drawText(t: String, c: Canvas, p: Paint, x: Float, y: Float) {
        textProvider?.also {
            it.setupPaint(p)
        }
        c.drawText(t, x + textLeftShift, y + baselineShift, p)
    }

}

@IntDef(TextAlign.BASELINE, TextAlign.ASCENT, TextAlign.CENTER, TextAlign.DESCENT)
@Retention(AnnotationRetention.SOURCE)
annotation class TextAlign {
    companion object {
        const val BASELINE = 0
        const val ASCENT = 1
        const val CENTER = 2
        const val DESCENT = 3
    }
}

interface TextProvider {

    /**
     * 文本，可以在这里替换文本
     *
     * @param rawText 原始文本
     * @return 要绘制的文本
     */
    fun text(rawText: String): String? = rawText

    /**
     * 设置 Paint 的文本大小，颜色等参数
     *
     * @param paint 画笔
     */
    fun setupPaint(paint: Paint) = Unit

}

interface DrawableProvider {

    /**
     * 获取 Drawable
     *
     * @return 要绘制的 Drawable
     */
    fun drawable(): Drawable

    /**
     * 获取 Drawable 高度
     *
     * @param fmi 当前文案的字体信息
     * @return Drawable 高度，默认和字体 descent 到 ascent 的距离
     */
    fun height(fmi: Paint.FontMetricsInt): Int = fmi.descent - fmi.ascent

    /**
     * 获取 Drawable 宽度
     *
     * @param fmi 当前文案的字体信息
     * @return Drawable 宽度，默认和字体 descent 到 ascent 的距离
     */
    fun width(fmi: Paint.FontMetricsInt): Int = fmi.descent - fmi.ascent

    /**
     * Drawable 的对齐方式，默认是和文案居中对齐（图片没有基线，所以不存在基线对齐）
     *
     * @return 对齐方式
     */
    @TextAlign fun align(): Int = TextAlign.CENTER

}

class SimpleTextProvider(
    val replacementText: String? = null,
    val textSize: Float? = null,
    val textColor: Int? = null,
    val fakeBold: Boolean? = null,
    val underline: Boolean? = null,
    val deleteLine: Boolean? = null
): TextProvider {
    override fun text(rawText: String): String? {
        return replacementText ?: super.text(rawText)
    }

    override fun setupPaint(paint: Paint) {
        textSize?.also { paint.textSize = it }
        textColor?.also { paint.color = it }
        fakeBold?.also { paint.isFakeBoldText = it }
        underline?.also { paint.isUnderlineText = it }
        deleteLine?.also { paint.isStrikeThruText = it }
    }

}

class SimpleDrawableProvider(
    val drawable: Drawable,
    val width: Int? = null,
    val height: Int? = null,
    @TextAlign val align: Int? = null
): DrawableProvider {
    override fun drawable(): Drawable = drawable

    override fun width(fmi: Paint.FontMetricsInt): Int {
        return width ?: super.width(fmi)
    }

    override fun height(fmi: Paint.FontMetricsInt): Int {
        return height ?: super.height(fmi)
    }

    override fun align(): Int {
        return align ?: super.align()
    }
}