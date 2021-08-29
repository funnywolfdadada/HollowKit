package com.funnywolf.hollowkit.app.fragments

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.funnywolf.hollowkit.app.databinding.PageStatefulLayoutBinding
import com.funnywolf.hollowkit.drawable.LinearGradientProvider
import com.funnywolf.hollowkit.drawable.RoundRectDrawable
import com.funnywolf.hollowkit.utils.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/11/15
 */
class StatefulLayoutFragment: Fragment() {

    @Volatile
    var state = 1

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding = PageStatefulLayoutBinding.inflate(inflater, container, false)
        val sl = binding.stateful
        sl.viewArray.put(1, View(sl.context).apply {
            background = RoundRectDrawable(Color.RED, 10.dp, 20.dp, 30.dp, 40.dp)
            layoutParams = FrameLayout.LayoutParams(200.dp, 100.dp, Gravity.CENTER)
        })
        sl.viewArray.put(2, View(sl.context).apply {
            background = RoundRectDrawable().radii(10.dp)
                    .fillShader(LinearGradientProvider(intArrayOf(Color.RED, Color.GREEN)))
            layoutParams = FrameLayout.LayoutParams(100.dp, 200.dp, Gravity.CENTER)
        })
        sl.viewArray.put(3, View(sl.context).apply {
            background = RoundRectDrawable().radii(10.dp)
                    .fillShader(LinearGradientProvider(intArrayOf(Color.GREEN, Color.BLUE)))
                    .ringSize(10.dp)
                    .ringColor(Color.RED)
            layoutParams = FrameLayout.LayoutParams(200.dp, 100.dp, Gravity.CENTER)
        })
        sl.viewArray.put(4, View(sl.context).apply {
            background = RoundRectDrawable().radii(10.dp)
                    .fillShader(LinearGradientProvider(intArrayOf(Color.BLUE, Color.RED)))
                    .ringSize(10.dp)
                    .ringShader(LinearGradientProvider(intArrayOf(Color.GREEN, Color.BLUE), GradientDrawable.Orientation.BL_TR))
            layoutParams = FrameLayout.LayoutParams(100.dp, 200.dp, Gravity.CENTER)
        })

        binding.next.setOnClickListener {
            val s = state++ % 5
            binding.next.text = s.toString()
            GlobalScope.launch {
                sl.state = s
            }
        }
        return binding.root
    }

}
