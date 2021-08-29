package com.funnywolf.hollowkit.app.fragments.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

/**
 * [BehavioralScrollView] 的 Demo 入口
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/21
 */
class ScrollBehaviorFragment: Fragment() {

    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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

        val list = listOf(
            LinkageScrollFragment(),
            SecondFloorFragment(),
            JellyFragment()
        )
        viewPager.adapter = object: FragmentStatePagerAdapter(childFragmentManager) {
            override fun getCount(): Int = list.size
            override fun getItem(position: Int): Fragment = list[position]
        }
    }

}
