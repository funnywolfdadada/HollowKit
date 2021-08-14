package com.funnywolf.hollowkit.app.utils

import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.funnywolf.hollowkit.app.R
import com.funnywolf.hollowkit.app.databinding.HolderJellyHigherPictureBinding
import com.funnywolf.hollowkit.app.databinding.HolderSimpleViewBinding
import com.funnywolf.hollowkit.recyclerview.*
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.view.scroll.behavior.JellyLayout
import com.funnywolf.hollowkit.view.setRoundRect
import kotlin.math.round
import kotlin.random.Random

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/10
 */

var defaultHolderBackgroundColor = 0xFFF89798.toInt()
var westWorldHolderBackgroundColor = 0xFF9E7D6D.toInt()

class SimpleAnyHolder(v: View): SimpleHolder<Any>(v)

inline fun <reified VH: SimpleHolder<in T>, reified T> SimpleAdapter.addOnBindListener(
    crossinline onBind: (VH, T)->Unit
): SimpleAdapter {
    return addOnBindHolderListener { vh, d ->
        if (vh is VH && d is T) {
            onBind(vh, d)
        }
    }
}

fun SimpleAdapter.addSimpleStringMapper(color: Int = 0xFFF89798.toInt()) = addMapper(
    HolderMapInfo(String::class.java, R.layout.holder_simple_view, SimpleAnyHolder::class.java)
).addOnBindListener<SimpleAnyHolder, String> { vh, d ->
    val binding = HolderSimpleViewBinding.bind(vh.itemView)
    binding.root.setBackgroundColor(color)
    binding.root.setOnClickListener {
        it.context.toast("Clicked $d")
    }
    binding.content.text = d
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
    if (layoutManager == null) {
        layoutManager = LinearLayoutManager(context)
    }
    val list = AdapterList<Any>()
    adapter = SimpleAdapter(list).addSimpleStringMapper(color)
        .also { list.bind(it) }
    list.clear()
    list.addAll(getRandomStrings(count))
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

fun RecyclerView.initPictures(enableDelete: Boolean = false): AdapterList<Any> {
    if (layoutManager == null) {
        layoutManager = LinearLayoutManager(context)
    }
    val list = AdapterList<Any>()
    adapter = SimpleAdapter(list).addMapper(
            HolderMapInfo(
                Picture::class.java,
                if (enableDelete) { R.layout.holder_jelly_picture } else { R.layout.holder_picture },
                SimpleAnyHolder::class.java
            )
    ).addOnBindListener<SimpleAnyHolder, Picture> { vh, d ->
        vh.itemView.findViewById<View>(R.id.image_view)?.setRoundRect(10.dp.toFloat())
        vh.itemView.findViewById<View>(R.id.image_view)?.setOnClickListener {
            vh.itemView.context.toast("Click picture ${d.res}")
        }
        val jellyLayout = vh.itemView.findViewById<JellyLayout>(R.id.jelly)
        jellyLayout?.scrollAxis = ViewCompat.SCROLL_AXIS_HORIZONTAL
        jellyLayout?.onTouchRelease = { jl ->
            jl.smoothScrollTo(if (jl.lastScrollDir > 0) { jl.maxScroll } else { 0 }) {
                if (jl.scrollX == jl.maxScroll) {
                    d.also { list.remove(it) }
                }
            }
        }
        vh.itemView.findViewById<JellyLayout>(R.id.jelly)?.smoothScrollTo(0, 0)
        vh.itemView.findViewById<ImageView>(R.id.image_view)?.load(d.res)
    }.also { list.bind(it) }
    list.clear()
    list.addAll(widerPictures)
    return list
}

fun RecyclerView.initHorizontalPictures(): AdapterList<Any> {
    if (layoutManager == null) {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }
    val list = AdapterList<Any>()
    adapter = SimpleAdapter(list).addMapper(
        HolderMapInfo(Picture::class.java, R.layout.holder_jelly_higher_picture, SimpleAnyHolder::class.java)
    ).addOnBindListener<SimpleAnyHolder, Picture> { vh, d ->
        val binding = HolderJellyHigherPictureBinding.bind(vh.itemView)
        binding.imageView.setRoundRect(10.dp.toFloat())
        binding.imageView.setOnClickListener {
            vh.itemView.context.toast("Click picture ${d.res}")
        }
        binding.jelly.onTouchRelease = { jl ->
            if (jl.scrollY == jl.maxScroll) {
                list.remove(d)
            } else {
                jl.smoothScrollTo(if (jl.lastScrollDir > 0) { jl.maxScroll } else { 0 }) {
                    if (jl.scrollY == jl.maxScroll) {
                        list.remove(d)
                    }
                }
            }
        }
        binding.jelly.smoothScrollTo(0, 0)
        binding.imageView.load(d.res)
    }.also {
        list.bind(it)
    }
    list.clear()
    list.addAll(higherPictures)
    return list
}
