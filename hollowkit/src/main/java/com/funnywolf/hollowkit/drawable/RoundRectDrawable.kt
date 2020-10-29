package com.funnywolf.hollowkit.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import kotlin.math.hypot


/**
 * 圆角矩形，支持边框和 [Shader]
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/7/3
 */
open class RoundRectDrawable(
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

    var fillColor = color
        set(value) {
            field = value
            invalidateSelf()
        }

    var fillShaderProvider: ShaderProvider? = null
        set(value) {
            field = value
            updateShader()
            invalidateSelf()
        }

    var ringColor: Int = 0
        set(value) {
            field = value
            invalidateSelf()
        }

    var ringShaderProvider: ShaderProvider? = null
        set(value) {
            field = value
            updateShader()
            invalidateSelf()
        }

    var ringSize: Int = 0
        set(value) {
            field = value
            updateRingPath()
            invalidateSelf()
        }

    private val paint = Paint().also {
        it.isAntiAlias = true
        it.style = Paint.Style.FILL
    }

    private val radii = floatArrayOf(
        leftTop.toFloat(), leftTop.toFloat(),
        rightTop.toFloat(), rightTop.toFloat(),
        rightBottom.toFloat(), rightBottom.toFloat(),
        leftBottom.toFloat(), leftBottom.toFloat()
    )
    private val fillPath = Path()
    private var fillShader: Shader? = null

    private val innerRadii = FloatArray(radii.size)
    private val ringPath = Path()
    private var ringShader: Shader? = null

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        updateFillPath()
        updateRingPath()
        updateShader()
    }

    private fun updateFillPath() {
        fillPath.rewind()
        fillPath.fillType = if (inverse) {
            Path.FillType.INVERSE_WINDING
        } else {
            Path.FillType.WINDING
        }
        fillPath.addRoundRect(RectF(bounds), radii, Path.Direction.CW)
    }

    private fun updateRingPath() {
        if (ringSize <= 0) {
            return
        }
        val bf = RectF(bounds)
        val ratio = 1 - 2 * ringSize / hypot(bf.width(), bf.height())
        for (i in innerRadii.indices) {
            innerRadii[i] = radii[i] * ratio
        }
        ringPath.rewind()
        ringPath.addRoundRect(bf.left, bf.top, bf.right, bf.bottom, radii, Path.Direction.CW)
        ringPath.addRoundRect(bf.left + ringSize, bf.top + ringSize, bf.right - ringSize, bf.bottom - ringSize, innerRadii, Path.Direction.CCW)
    }

    private fun updateShader() {
        fillShader = fillShaderProvider?.invoke(bounds)
        ringShader = ringShaderProvider?.invoke(bounds)
    }

    override fun draw(canvas: Canvas) {
        paint.shader = fillShader
        paint.color = fillColor
        canvas.drawPath(fillPath, paint)
        if (ringColor != 0 && ringSize != 0) {
            paint.shader = ringShader
            paint.color = ringColor
            canvas.drawPath(ringPath, paint)
        }
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

interface ShaderProvider: Function1<Rect, Shader>

class LinearGradientProvider(
    private val orientation: GradientDrawable.Orientation,
    private val colors: IntArray,
    private val positions: FloatArray? = null,
    private val model: Shader.TileMode = Shader.TileMode.CLAMP
): ShaderProvider {

    override fun invoke(r: Rect): Shader {
        val p = when (orientation) {
            GradientDrawable.Orientation.TOP_BOTTOM -> {
                RectF(0F, r.top.toFloat(), 0F, r.bottom.toFloat())
            }
            GradientDrawable.Orientation.TR_BL -> {
                RectF(r.right.toFloat(), r.top.toFloat(), r.left.toFloat(), r.bottom.toFloat())
            }
            GradientDrawable.Orientation.RIGHT_LEFT -> {
                RectF(r.right.toFloat(), 0F, r.left.toFloat(), 0F)
            }
            GradientDrawable.Orientation.BR_TL -> {
                RectF(r.right.toFloat(), r.bottom.toFloat(), r.left.toFloat(), r.top.toFloat())
            }
            GradientDrawable.Orientation.BOTTOM_TOP -> {
                RectF(0F, r.bottom.toFloat(), 0F, r.top.toFloat())
            }
            GradientDrawable.Orientation.BL_TR -> {
                RectF(r.left.toFloat(), r.bottom.toFloat(), r.right.toFloat(), r.top.toFloat())
            }
            GradientDrawable.Orientation.LEFT_RIGHT -> {
                RectF(r.left.toFloat(), 0F, r.right.toFloat(), 0F)
            }
            GradientDrawable.Orientation.TL_BR -> {
                RectF(r.left.toFloat(), r.top.toFloat(), r.right.toFloat(), r.bottom.toFloat())
            }
        }
        return LinearGradient(p.left, p.top, p.right, p.bottom, colors, positions, model)
    }

}

