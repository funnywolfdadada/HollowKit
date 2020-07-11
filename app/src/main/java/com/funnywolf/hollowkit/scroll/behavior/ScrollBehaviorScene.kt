package com.funnywolf.hollowkit.scroll.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scene.ui.GroupSceneUIUtility
import com.funnywolf.hollowkit.view.scroll.behavior.BehavioralScrollView
import com.funnywolf.hollowkit.view.scroll.behavior.FloatingHeaderBehavior
import com.google.android.material.tabs.TabLayout
import java.util.LinkedHashMap

/**
 * [BehavioralScrollView] 的 Demo 入口
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class ScrollBehaviorScene: GroupScene() {

    private lateinit var  tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        val context = inflater.context
        viewPager = ViewPager(context).apply {
            id = View.generateViewId()
        }
        tabLayout = TabLayout(context).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return BehavioralScrollView(context).apply {
            setupBehavior(FloatingHeaderBehavior(viewPager, tabLayout))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = LinkedHashMap<String, UserVisibleHintGroupScene>().apply {
            put("五级嵌套", ComplexBehaviorScene())
            put("联动滚动", LinkedScrollScene())
            put("折叠头部", CollapsingHeaderScene())
            put("悬浮头部", FloatingHeaderScene())
            put("下拉刷新", PullRefreshScene())
            put("底部浮层", BottomSheetScene())
        }
        GroupSceneUIUtility.setupWithViewPager(viewPager, this, list)
        tabLayout.setupWithViewPager(viewPager)
    }

}
