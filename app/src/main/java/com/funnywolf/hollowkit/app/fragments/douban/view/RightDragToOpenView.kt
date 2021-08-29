package com.funnywolf.hollowkit.app.fragments.douban.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.FloatRange

class RightDragToOpenView: FrameLayout {

    @FloatRange(from = 0.0, to = 1.0)
    var process: Float = 0F
        set(value) {
            field = value
            updateProcess()
        }

    @FloatRange(from = 0.0, to = 1.0)
    var textProcess: Float = 0F
        private set

    val textView: TextView
    var look = "查\n看"
    var releaseToLook = "释\n放\n查\n看"

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        textView = TextView(context)
        textView.textSize = 10F
        textView.gravity = Gravity.CENTER
        textView.minWidth = 30
        textView.maxWidth = 100
        textView.background = getDrawable()
        addView(textView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
    }

    private fun getDrawable() = object : Drawable() {
        private val paint = Paint().also { p ->
            p.isAntiAlias = true
            p.color = 0x20000000
        }

        override fun draw(canvas: Canvas) {
            val bound = bounds
            canvas.drawArc(
                bound.left.toFloat(),
                bound.top.toFloat(),
                bound.left.toFloat() + bound.width() * 4,
                bound.bottom.toFloat(),
                90F, 180F,
                false, paint
            )
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

    }

    private fun updateProcess() {
        val exposedWidth = width * process
        when {
            exposedWidth < textView.minWidth -> {
                textProcess = 0F
                textView.text = look
                textView.layoutParams.width = textView.minWidth
                textView.x = 0F
            }
            exposedWidth < textView.maxWidth -> {
                textProcess = exposedWidth / textView.maxWidth
                textView.text = if (textProcess < 0.7) {
                    look
                } else {
                    releaseToLook
                }
                textView.layoutParams.width = exposedWidth.toInt()
                textView.x = 0F
            }
            else -> {
                textProcess = 1F
                textView.text = releaseToLook
                textView.layoutParams.width = textView.maxWidth
                textView.x = exposedWidth - textView.maxWidth
            }
        }
        requestLayout()
    }

}