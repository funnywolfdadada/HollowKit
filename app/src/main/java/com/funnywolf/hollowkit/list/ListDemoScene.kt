package com.funnywolf.hollowkit.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.recyclerview.LiveList
import com.funnywolf.hollowkit.recyclerview.RecyclerViewLoadMore
import com.funnywolf.hollowkit.recyclerview.SimpleAdapter

/**
 * 列表 demo，会用到 [SimpleAdapter]、[RecyclerViewLoadMore] 和 [LiveList]
 *
 * @author funnywolf
 * @since 2020/3/21
 */
class ListDemoScene: Scene() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.scene_list_demo, container, false)
    }

}