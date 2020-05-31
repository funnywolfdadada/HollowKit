package com.funnywolf.hollowkit.richtext

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
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
        tv.maxLines = 3
        var textSize = 24.dp
        val tp = object: TextProvider {
            override fun text(rawText: String): String? {
                return "f局g"
            }

            override fun textSize(paintTextSize: Float): Float {
                return textSize.toFloat()
            }

            override fun textColor(paintColor: Int): Int {
                return Color.GREEN
            }

        }
        val bg = object: DrawableProvider {

            override fun drawable(): Drawable {
                return view.context.getDrawable(R.drawable.picture_1)!!
            }

        }
        val replacement = object: DrawableProvider {

            override fun drawable(): Drawable {
                return view.context.getDrawable(R.drawable.avatar_3)!!
            }

            override fun width(fmi: Paint.FontMetricsInt): Int = 20.dp
            override fun height(fmi: Paint.FontMetricsInt): Int = 20.dp
        }
        val left = object: DrawableProvider {

            override fun drawable(): Drawable {
                return view.context.getDrawable(R.drawable.avatar_1)!!
            }

            override fun width(fmi: Paint.FontMetricsInt): Int = 20.dp
            override fun height(fmi: Paint.FontMetricsInt): Int = 20.dp
            override fun align(): Int = TextAlign.ASCENT
        }
        val right = object: DrawableProvider {

            override fun drawable(): Drawable {
                return view.context.getDrawable(R.drawable.avatar_2)!!
            }

            override fun width(fmi: Paint.FontMetricsInt): Int = 20.dp
            override fun height(fmi: Paint.FontMetricsInt): Int = 20.dp
            override fun align(): Int = TextAlign.DESCENT
        }
        val uds = UniDecorSpan().apply {
            textProvider = tp
            backgroundDrawable = bg
            replacementDrawable = replacement
            leftDrawable = left
            rightDrawable = right
        }

        val richText = SpannableStringBuilder()
            .append("围绕台海的大棋，从1949年算起已经下了70年，这场漫长的棋f")
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