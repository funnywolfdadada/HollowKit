package com.funnywolf.hollowkit.utils

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/1
 */
fun roundRectDrawable(
    color: Int,
    leftTop: Int, rightTop: Int = leftTop,
    rightBottom: Int = leftTop, leftBottom: Int = leftTop
): ShapeDrawable {
    val s = RoundRectShape(floatArrayOf(
        leftTop.toFloat(), leftTop.toFloat(),
        rightTop.toFloat(), rightTop.toFloat(),
        rightBottom.toFloat(), rightBottom.toFloat(),
        leftBottom.toFloat(), leftBottom.toFloat()
    ), null, null)
    return ShapeDrawable(s).apply {
        paint.color = color
    }
}