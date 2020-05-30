package com.funnywolf.hollowkit.utils

import android.content.res.Resources
import kotlin.math.ceil
import kotlin.math.round

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/30
 */
val Number.dp: Int
    get() {
        return (this.toFloat() * Resources.getSystem().displayMetrics.density + 0.5).toInt()
    }
val Number.sp: Int
    get() {
        return (this.toFloat() * Resources.getSystem().displayMetrics.scaledDensity + 0.5).toInt()
    }

