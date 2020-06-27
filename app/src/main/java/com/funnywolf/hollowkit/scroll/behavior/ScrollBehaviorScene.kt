package com.funnywolf.hollowkit.scroll.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scene.ui.GroupSceneUIUtility
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.google.android.material.tabs.TabLayout
import java.util.LinkedHashMap

/**
 * [BehavioralScrollView] 的 Demo 入口
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class ScrollBehaviorScene: GroupScene() {

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        return inflater.inflate(R.layout.scene_nested_scroll_behavior, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = LinkedHashMap<String, UserVisibleHintGroupScene>().apply {
            put("下拉刷新", PullRefreshScene())
            put("折叠头部", CollapsingHeaderScene())
            put("底部浮层", BottomSheetScene())
        }
        viewPager = view.findViewById<ViewPager>(R.id.view_pager).apply {
            GroupSceneUIUtility.setupWithViewPager(this, this@ScrollBehaviorScene, list)
        }
        tabLayout = view.findViewById<TabLayout>(R.id.tab_layout).apply {
            setupWithViewPager(viewPager)
        }
    }

}
