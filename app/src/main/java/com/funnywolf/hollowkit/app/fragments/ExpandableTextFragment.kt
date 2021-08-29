package com.funnywolf.hollowkit.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.funnywolf.hollowkit.app.databinding.PageExpandableTextBinding

/**
 * 带展开的 TextView
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/7/31
 */
class ExpandableTextFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PageExpandableTextBinding.inflate(inflater, container, false)
        val view = binding.root
        val tv = binding.textView
        tv.expandText = SpannableStringBuilder()
            .append("展开", ForegroundColorSpan(Color.RED), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv.setOnClickListener {
            if (tv.maxLines <= 0 || tv.maxLines == Int.MAX_VALUE) {
                tv.maxLines = 5
            } else {
                tv.maxLines = Int.MAX_VALUE
            }
        }
        binding.addMaxLines.setOnClickListener {
            if (tv.maxLines < Int.MAX_VALUE) { tv.maxLines += 1 }
        }
        binding.minusMaxLines.setOnClickListener {
            if (tv.maxLines > 1) { tv.maxLines -= 1 }
        }
        binding.addPadding.setOnClickListener {
            val padding = view.paddingLeft
            if (padding < Int.MAX_VALUE) {
                view.setPadding(padding + 10, 0, padding + 10, 0)
            }
        }
        binding.minusPadding.setOnClickListener {
            val padding = view.paddingLeft
            if (padding > 10) {
                view.setPadding(padding - 10, 0, padding - 10, 0)
            }
        }
        var text = tv.text.toString()
        binding.addText.setOnClickListener {
            text += "发付款计划司机会就开始放发付"
            tv.text = text
        }
        binding.minusText.setOnClickListener {
            text = text.substring(0, text.length - 10)
            tv.text = text
        }
        return binding.root
    }

}