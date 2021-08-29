package com.funnywolf.hollowkit.app.fragments

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
import androidx.fragment.app.Fragment
import com.funnywolf.hollowkit.app.R
import com.funnywolf.hollowkit.app.databinding.PageRichTextBinding
import com.funnywolf.hollowkit.drawable.RoundRectDrawable
import com.funnywolf.hollowkit.richtext.*
import com.funnywolf.hollowkit.utils.dp

/**
 * 富文本测试页面
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/9
 */
class RichTextFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val binding = PageRichTextBinding.inflate(inflater, container, false)
        val tv = binding.textView
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
            backgroundDrawable = SimpleDrawableProvider(context.getDrawable(R.drawable.picture_1)!!)
            leftDrawable = SimpleDrawableProvider(context.getDrawable(R.drawable.avatar_1)!!, 20.dp, 20.dp, TextAlign.ASCENT)
            rightDrawable = SimpleDrawableProvider(context.getDrawable(R.drawable.avatar_2)!!, 20.dp, 20.dp, TextAlign.DESCENT)
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
                        context.getDrawable(R.drawable.ic_camera)!!,
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
                        context.getDrawable(R.drawable.ic_access_alarms)!!,
                        align = TextAlign.CENTER
                    )
                },
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            .append("围绕台海的大棋，从1949年算起已经下了70年，")
            .append("A", uds, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            .append("g现在终于接近终盘。在我们进行终局")
        tv.text = richText

        binding.sbSize.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSize = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.tvAlignBaseline.setOnClickListener {
            uds.align = TextAlign.BASELINE
            tv.text = richText
        }

        binding.tvAlignAscent.setOnClickListener {
            uds.align = TextAlign.ASCENT
            tv.text = richText
        }

        binding.tvAlignCenter.setOnClickListener {
            uds.align = TextAlign.CENTER
            tv.text = richText
        }

        binding.tvAlignDescent.setOnClickListener {
            uds.align = TextAlign.DESCENT
            tv.text = richText
        }

        binding.sbPaddingLeft.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.padding.left = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbPaddingRight.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.padding.right = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbPaddingTop.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.padding.top= progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbPaddingBottom.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.padding.bottom = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbMarginTop.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.margin.top = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbMarginBottom.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.margin.bottom = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbMarginLeft.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.margin.left = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbMarginRight.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                uds.margin.right = progress
                tv.text = richText
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return binding.root
    }

}