package com.funnywolf.hollowkit.app.utils

import android.content.Context
import android.widget.Toast

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/22
 */
fun Context.toast(text: CharSequence?, duration: Int = Toast.LENGTH_SHORT) {
    text ?: return
    Toast.makeText(this, text, duration).show()
}