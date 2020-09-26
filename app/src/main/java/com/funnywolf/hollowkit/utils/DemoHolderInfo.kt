package com.funnywolf.hollowkit.utils

import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.drawable.RoundRectDrawable

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/26
 */
val white = arrayOf(
    0x10FFFFFF,
    0x20FFFFFF,
    0x40FFFFFF,
    0x80FFFFFF.toInt(),
    0xA0FFFFFF.toInt()
)

class SmallDemoCard
fun smallDemoCards(n: Int = 1) = ArrayList<SmallDemoCard>(n).apply {
    repeat(n) {
        add(SmallDemoCard())
    }
}
val smallDemoCardInfo = HolderInfo(
    SmallDemoCard::class.java,
    R.layout.holder_demo_card_small,
    onCreate = {
        it.itemView.setOnClickListener { v ->
            v.context.toast("Click SmallDemoCard")
        }
        it.itemView.background = RoundRectDrawable(white[1], 10.dp)
        it.find(R.id.head)?.background = CircleDrawables(white[2], 1, 1, 40.dp)
        it.find(R.id.content)?.background = RoundRectDrawables(white[2], 1, 2, 20.dp, 10.dp.toFloat())
    }
)

class MiddleDemoCard
fun middleDemoCards(n: Int = 1) = ArrayList<MiddleDemoCard>(n).apply {
    repeat(n) {
        add(MiddleDemoCard())
    }
}
val middleDemoCardInfo = HolderInfo(
    MiddleDemoCard::class.java,
    R.layout.holder_demo_card_middle,
    onCreate = {
        it.itemView.setOnClickListener { v ->
            v.context.toast("Click MiddleDemoCard")
        }
        it.itemView.background = RoundRectDrawable(white[1], 10.dp)
        it.find(R.id.head)?.background = CircleDrawables(white[2], 1, 1, 40.dp)
        it.find(R.id.content)?.background = RoundRectDrawables(white[2], 1, 2, 20.dp, 10.dp.toFloat())
        it.find(R.id.detail)?.background = RoundRectDrawables(white[2], 2, 6, 10.dp, 10.dp.toFloat())
    }
)
