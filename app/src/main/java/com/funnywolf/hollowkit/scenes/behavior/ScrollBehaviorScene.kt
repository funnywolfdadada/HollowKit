package com.funnywolf.hollowkit.scenes.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.bytedance.scene.group.GroupScene
import com.bytedance.scene.group.UserVisibleHintGroupScene
import com.bytedance.scene.ui.GroupSceneUIUtility
import java.util.LinkedHashMap

/**
 * [BehavioralScrollView] 的 Demo 入口
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class ScrollBehaviorScene: GroupScene() {

    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        val context = inflater.context
        return ViewPager(context).apply {
            viewPager = this
            id = View.generateViewId()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = LinkedHashMap<String, UserVisibleHintGroupScene>().apply {
            put("联动", LinkageScrollScene())
            put("下拉二楼", SecondFloorScene())
            put("弹性", JellyScene())
        }
        GroupSceneUIUtility.setupWithViewPager(viewPager, this, list)
    }

}
