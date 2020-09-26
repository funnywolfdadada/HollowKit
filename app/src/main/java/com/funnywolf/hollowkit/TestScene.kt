package com.funnywolf.hollowkit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.utils.*

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/9
 */
class TestScene: Scene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val rv = RecyclerView(context).apply {

        }
        return rv
    }

}
