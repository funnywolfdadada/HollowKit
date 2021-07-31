package com.funnywolf.hollowkit.scenes

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.view.ExpandableTextView

/**
 * 带展开的 TextView
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/7/31
 */
class ExpandableTextScene: Scene() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.scene_expandable_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tv = view.findViewById<ExpandableTextView>(R.id.text_view).setExpandText("展开", Color.RED)
        tv.setOnClickListener {
            if (tv.maxLines <= 0 || tv.maxLines == Int.MAX_VALUE) {
                tv.maxLines = 5
            } else {
                tv.maxLines = Int.MAX_VALUE
            }
        }
        view.findViewById<View>(R.id.add_max_lines).setOnClickListener {
            if (tv.maxLines < Int.MAX_VALUE) { tv.maxLines += 1 }
        }
        view.findViewById<View>(R.id.minus_max_lines).setOnClickListener {
            if (tv.maxLines > 1) { tv.maxLines -= 1 }
        }
        view.findViewById<View>(R.id.add_padding).setOnClickListener {
            val padding = view.paddingLeft
            if (padding < Int.MAX_VALUE) {
                view.setPadding(padding + 10, 0, padding + 10, 0)
            }
        }
        view.findViewById<View>(R.id.minus_padding).setOnClickListener {
            val padding = view.paddingLeft
            if (padding > 10) {
                view.setPadding(padding - 10, 0, padding - 10, 0)
            }
        }
        var text = tv.text.toString()
        view.findViewById<View>(R.id.add_text).setOnClickListener {
            text += "发付款计划司机会就开始放发付"
            tv.text = text
        }
        view.findViewById<View>(R.id.minus_text).setOnClickListener {
            text = text.substring(0, text.length - 10)
            tv.text = text
        }
    }

}