package com.funnywolf.hollowkit.richtext

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.drawable.RoundRectDrawable
import com.funnywolf.hollowkit.utils.dp

/**
 * 富文本测试页面
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/9
 */
class RichTextScene: Scene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return LayoutInflater.from(container.context).inflate(R.layout.scene_rich_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tv = view.findViewById<TextView>(R.id.text_view)
        tv.ellipsize = TextUtils.TruncateAt.END
        var textSize = 24.dp
        val tp = object: TextProvider {
            override fun text(rawText: String): String? {
                return "f局g"
            }

            override fun setupPaint(paint: Paint) {
                paint.textSize = textSize.toFloat()
                paint.color = Color.GREEN
            }

        }
        val uds = UniDecorSpan().apply {
            textProvider = tp
            backgroundDrawable = SimpleDrawableProvider(view.context.getDrawable(R.drawable.picture_1)!!)
            leftDrawable = SimpleDrawableProvider(view.context.getDrawable(R.drawable.avatar_1)!!, 20.dp, 20.dp, TextAlign.ASCENT)
            rightDrawable = SimpleDrawableProvider(view.context.getDrawable(R.drawable.avatar_2)!!, 20.dp, 20.dp, TextAlign.DESCENT)
        }

        val richText = SpannableStringBuilder()
            .append(
                "围",
                UniDecorSpan().apply {
                    align = TextAlign.DESCENT
                    textProvider = SimpleTextProvider(
                        textSize = 48.dp.toFloat(),
                        textColor = Color.RED,
                        fakeBold = true,
                        underline = true,
                        deleteLine = true
                    )
                },
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            .append("绕台海的大棋，从1949年算起已经下了70年，")
            .append(
                "g这场漫长g",
                UniDecorSpan().apply {
                    align = TextAlign.BASELINE
                    drawFontMatrix = true
                    padding.top = 3.dp
                    padding.bottom = 1.dp
                    padding.left = 5.dp
                    padding.right = 10.dp
                    textProvider = SimpleTextProvider(textColor = Color.CYAN)
                    backgroundDrawable = SimpleDrawableProvider(
                        RoundRectDrawable(Color.BLUE, 20.dp)
                    )
                    leftDrawable = SimpleDrawableProvider(
                        view.context.getDrawable(R.drawable.ic_camera)!!,
                        align = TextAlign.ASCENT
                    )
                },
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            .append(
                "这场漫长的棋",
                UniDecorSpan().apply {
                    drawFontMatrix = true
                },
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            .append(
                ".",
                UniDecorSpan().apply {
                    align = TextAlign.DESCENT
                    replacementDrawable = SimpleDrawableProvider(
                        view.context.getDrawable(R.drawable.ic_access_alarms)!!,
                        align = TextAlign.CENTER
                    )
                },
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            .append("围绕台海的大棋，从1949年算起已经下了70年，")
            .append("A", uds, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            .append("g现在终于接近终盘。在我们进行终局")
        tv.text = richText

        view.findViewById<SeekBar>(R.id.sb_size).setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSize = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.findViewById<View>(R.id.tv_align_baseline).setOnClickListener {
            uds.align = TextAlign.BASELINE
            tv.text = richText
        }

        view.findViewById<View>(R.id.tv_align_ascent).setOnClickListener {
            uds.align = TextAlign.ASCENT
            tv.text = richText
        }

        view.findViewById<View>(R.id.tv_align_center).setOnClickListener {
            uds.align = TextAlign.CENTER
            tv.text = richText
        }

        view.findViewById<View>(R.id.tv_align_descent).setOnClickListener {
            uds.align = TextAlign.DESCENT
            tv.text = richText
        }

        view.findViewById<SeekBar>(R.id.sb_padding_left).setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.padding.left = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.findViewById<SeekBar>(R.id.sb_padding_right).setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.padding.right = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.findViewById<SeekBar>(R.id.sb_padding_top).setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.padding.top= progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.findViewById<SeekBar>(R.id.sb_padding_bottom).setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.padding.bottom = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.findViewById<SeekBar>(R.id.sb_margin_top).setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.margin.top = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.findViewById<SeekBar>(R.id.sb_margin_bottom).setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.margin.bottom = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.findViewById<SeekBar>(R.id.sb_margin_left).setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.margin.left = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.findViewById<SeekBar>(R.id.sb_margin_right).setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.margin.right = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

}