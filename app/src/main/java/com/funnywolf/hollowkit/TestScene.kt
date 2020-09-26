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
        val list = ArrayList<Any>()
        list.addAll(smallDemoCards(11))
        list.addAll(middleDemoCards(11))
        val rv = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SimpleAdapter(list)
                .addHolderInfo(smallDemoCardInfo)
                .addHolderInfo(middleDemoCardInfo)
            setBackgroundColor(westWorldHolderBackgroundColor)
        }
        return rv
    }

}
