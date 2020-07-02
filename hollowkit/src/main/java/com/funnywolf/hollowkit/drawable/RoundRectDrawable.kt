package com.funnywolf.hollowkit.drawable

import android.graphics.*
import android.graphics.drawable.Drawable


/**
 * 圆角矩形
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/7/3
 */
class RoundRectDrawable(
    color: Int,
    leftTop: Int,
    rightTop: Int = leftTop,
    rightBottom: Int = rightTop,
    leftBottom: Int = rightBottom,
    /**
     * 反向，绘制四个角，中间透明圆角矩形
     */
    private val inverse: Boolean = false
): Drawable() {

    private val paint = Paint().also {
        it.isAntiAlias = true
        it.style = Paint.Style.FILL
        it.color = color
    }

    private val radii = floatArrayOf(
        leftTop.toFloat(), leftTop.toFloat(),
        rightTop.toFloat(), rightTop.toFloat(),
        rightBottom.toFloat(), rightBottom.toFloat(),
        leftBottom.toFloat(), leftBottom.toFloat()
    )

    private val path = Path()

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        bounds ?: return
        path.apply {
            rewind()
            fillType = if (inverse) {
                Path.FillType.INVERSE_WINDING
            } else {
                Path.FillType.WINDING
            }
            addRoundRect(RectF(bounds), radii, Path.Direction.CW)
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

}