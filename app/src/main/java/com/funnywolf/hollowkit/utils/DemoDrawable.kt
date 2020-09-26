package com.funnywolf.hollowkit.utils

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRectF

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/8/9
 */
class CircleDrawables(
    private val color: Int,
    private val column: Int,
    private val row: Int,
    private val radius: Int
): Drawable() {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var gapX = 0F
    private var gapY = 0F

    private var bgColor: Int = 0
    private var bgRadius = 0F

    fun background(color: Int, radius: Float = 0F): CircleDrawables {
        bgColor = color
        bgRadius = radius
        invalidateSelf()
        return this
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        bounds ?: return
        if (column == 0 || row == 0) {
            return
        }
        gapX = (bounds.width() - radius * 2F * column) / (column + 1F)
        gapY = (bounds.height() - radius * 2F * row) / (row + 1F)
    }

    override fun draw(canvas: Canvas) {
        paint.color = bgColor
        canvas.drawRoundRect(bounds.toRectF(), bgRadius, bgRadius, paint)
        paint.color = color
        for (i in 0 until column) {
            val x = gapX + radius + (gapX + radius * 2) * i
            for (j in 0 until row) {
                val y = gapY + radius + (gapY + radius * 2) * j
                canvas.drawCircle(x, y, radius.toFloat(), paint)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

}

class RoundRectDrawables(
    private val color: Int,
    private val column: Int,
    private val row: Int,
    private val gap: Int,
    private val radius: Float
): Drawable() {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var rectWidth = 0F
    private var rectHeight = 0F

    private var bgColor = 0
    private var bgRadius = 0F

    fun background(color: Int, radius: Float = 0F): RoundRectDrawables {
        bgColor = color
        bgRadius = radius

        invalidateSelf()
        return this
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        bounds ?: return

        if (column == 0 || row == 0) {
            return
        }
        rectWidth = (bounds.width() - gap * (column + 1F)) / column
        rectHeight = (bounds.height() - gap * (row + 1F)) / row
    }

    override fun draw(canvas: Canvas) {
        paint.color = bgColor
        canvas.drawRoundRect(bounds.toRectF(), bgRadius, bgRadius, paint)
        paint.color = color
        for (i in 0 until column) {
            val x = gap + (gap + rectWidth) * i
            for (j in 0 until row) {
                val y = gap + (gap + rectHeight) * j
                canvas.drawRoundRect(x, y, x + rectWidth, y + rectHeight, radius, radius, paint)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

}
