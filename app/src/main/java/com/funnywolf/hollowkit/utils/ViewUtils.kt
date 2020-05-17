package com.funnywolf.hollowkit.utils

import android.content.Context
import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.view.ViewOutlineProvider

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/10
 */
private fun Context.displayMetrics() = this.resources.displayMetrics
fun Context.getScreenHeight() = displayMetrics().heightPixels
fun Float.dp2pix(context: Context) = (context.displayMetrics().density * this).toInt()
fun Int.dp2pix(context: Context) = this.toFloat().dp2pix(context)

fun View.setRoundRect(radius: Float) {
    clipToOutline = true
    outlineProvider = object: ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radius)
        }
    }
}

fun roundRectDrawable(
    color: Int,
    topLeft: Float,
    topRight: Float = topLeft,
    bottomLeft: Float = topLeft,
    bottomRight: Float = topLeft
): Drawable {
    val shape = RoundRectShape(floatArrayOf(
        topLeft, topLeft,
        topRight, topRight,
        bottomLeft, bottomLeft,
        bottomRight, bottomRight
    ), null, null)
    val d = ShapeDrawable(shape)
    d.paint.color = color
    return d
}
