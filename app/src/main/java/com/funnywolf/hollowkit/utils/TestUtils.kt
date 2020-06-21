package com.funnywolf.hollowkit.utils

import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.funnywolf.hollowkit.R
import kotlin.math.round
import kotlin.random.Random

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/10
 */

fun createSimpleStringHolderInfo(color: Int = 0xFFF89798.toInt()): HolderInfo<String> {
    return HolderInfo(String::class.java,
        R.layout.holder_simple_view,
        onCreate = { holder ->
            holder.itemView.setBackgroundColor(color)
            holder.itemView.setOnClickListener {
                Toast.makeText(it.context, "Clicked ${holder.data}", Toast.LENGTH_SHORT).show()
            }
        },
        onBind = { holder, data ->
            holder.v<TextView>(R.id.content)?.text = data
        })
}

fun getRandomString(length: Int = (Math.random() * 3 + 7).toInt()): String {
    return String(CharArray(length) {
        'A' + (Math.random() * 26).toInt()
    })
}

fun getRandomStrings(n: Int, prefixIndex: Boolean = true, prefix: String = "", suffix: String = "",
                     sizeMin: Int = 3, sizeMax: Int = 10): MutableList<String> {
    return MutableList(n) {
        "${
        if (prefixIndex) { "$it:" } else { "" }
        }$prefix${
        getRandomString(round(Math.random() * (sizeMax - sizeMin) + sizeMin).toInt())
        }$suffix"
    }
}

fun getRandomInt(n: Int, start: Int = 0, end: Int = Int.MAX_VALUE): MutableList<Int> {
    return MutableList(n) {
        Random.nextInt(start, end)
    }
}

fun RecyclerView.simpleInit(count: Int = 30, color: Int = 0xFFF89798.toInt()) {
    layoutManager = LinearLayoutManager(context)
    adapter = SimpleAdapter(getRandomStrings(count))
        .addHolderInfo(createSimpleStringHolderInfo(color))
}
