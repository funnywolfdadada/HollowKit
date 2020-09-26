package com.funnywolf.hollowkit.utils

import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.recyclerview.LiveList
import com.funnywolf.hollowkit.view.scroll.behavior.JellyLayout
import kotlin.math.round
import kotlin.random.Random

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/10
 */

var defaultHolderBackgroundColor = 0xFFF89798.toInt()
var westWorldHolderBackgroundColor = 0xFF9E7D6D.toInt()

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

fun RecyclerView.simpleInit(count: Int = 30, color: Int = defaultHolderBackgroundColor) {
    layoutManager = LinearLayoutManager(context)
    adapter = SimpleAdapter(getRandomStrings(count))
        .addHolderInfo(createSimpleStringHolderInfo(color))
}

class Picture(val res: Int)
val widerPictures = listOf(
    Picture(R.drawable.bing0),
    Picture(R.drawable.bing1),
    Picture(R.drawable.bing2),
    Picture(R.drawable.bing3),
    Picture(R.drawable.bing4),
    Picture(R.drawable.bing5),
    Picture(R.drawable.bing6),
    Picture(R.drawable.bing7),
    Picture(R.drawable.bing8),
    Picture(R.drawable.bing9),
    Picture(R.drawable.bing10),
    Picture(R.drawable.bing11),
    Picture(R.drawable.bing12),
    Picture(R.drawable.bing13),
    Picture(R.drawable.bing14),
    Picture(R.drawable.bing15),
    Picture(R.drawable.bing16),
    Picture(R.drawable.bing17),
    Picture(R.drawable.bing18),
    Picture(R.drawable.bing19)
)

val higherPictures = listOf(
    Picture(R.drawable.poster_1917),
    Picture(R.drawable.poster_american_dreams_in_china),
    Picture(R.drawable.poster_westworld_season_3),
    Picture(R.drawable.poster_capernaum),
    Picture(R.drawable.poster_dark_waters),
    Picture(R.drawable.poster_escape_from_pretoria),
    Picture(R.drawable.poster_jojo_rabbit),
    Picture(R.drawable.poster_little_women),
    Picture(R.drawable.poster_mrs_america),
    Picture(R.drawable.poster_mulan),
    Picture(R.drawable.poster_parasite),
    Picture(R.drawable.poster_the_call_of_the_wild),
    Picture(R.drawable.poster_the_man_standing_next),
    Picture(R.drawable.poster_the_six),
    Picture(R.drawable.poster_the_wandering_earth),
    Picture(R.drawable.poster_westworld_season_3)
)

fun RecyclerView.initPictures(enableDelete: Boolean = false) {
    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    val liveList = LiveList<Any>(ArrayList(widerPictures))
    val simpleAdapter = SimpleAdapter(liveList.get())
    liveList.bind(simpleAdapter)
    adapter = simpleAdapter
        .addHolderInfo(HolderInfo(
            Picture::class.java,
            if (enableDelete) { R.layout.holder_jelly_picture } else { R.layout.holder_picture },
            onCreate = { h ->
                h.find(R.id.image_view)?.setRoundRect(10.dp.toFloat())
                h.find(R.id.image_view)?.setOnClickListener { v ->
                    v.context.toast("Click picture ${h.data?.res}")
                }
                h.v<JellyLayout>(R.id.jelly)?.onTouchRelease = { jl ->
                    jl.smoothScrollTo(if (jl.lastScrollDir > 0) { jl.maxScroll } else { 0 }) {
                        if (jl.scrollX == jl.maxScroll) {
                            liveList.remove(h.data)
                        }
                    }
                }
            }, onBind = { h, p ->
                h.v<JellyLayout>(R.id.jelly)?.smoothScrollTo(0, 0)
                h.v<ImageView>(R.id.image_view)?.load(p.res)
            }
        ))
}

fun RecyclerView.initHorizontalPictures(enableDelete: Boolean = false) {
    layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    val liveList = LiveList<Any>(ArrayList(higherPictures))
    val simpleAdapter = SimpleAdapter(liveList.get())
    liveList.bind(simpleAdapter)
    adapter = simpleAdapter
        .addHolderInfo(HolderInfo(
            Picture::class.java,
            R.layout.holder_jelly_higher_picture,
            onCreate = { h ->
                h.find(R.id.image_view)?.setRoundRect(10.dp.toFloat())
                h.find(R.id.image_view)?.setOnClickListener { v ->
                    v.context.toast("Click picture ${h.data?.res}")
                }
                h.v<JellyLayout>(R.id.jelly)?.scrollAxis = ViewCompat.SCROLL_AXIS_VERTICAL
                h.v<JellyLayout>(R.id.jelly)?.onTouchRelease = { jl ->
                    if (jl.scrollY == jl.maxScroll) {
                        liveList.remove(h.data)
                    } else {
                        jl.smoothScrollTo(if (jl.lastScrollDir > 0) { jl.maxScroll } else { 0 }) {
                            if (jl.scrollY == jl.maxScroll) {
                                liveList.remove(h.data)
                            }
                        }
                    }
                }
            }, onBind = { h, p ->
                h.v<JellyLayout>(R.id.jelly)?.smoothScrollTo(0, 0)
                h.v<ImageView>(R.id.image_view)?.load(p.res)
            }
        ))
}
