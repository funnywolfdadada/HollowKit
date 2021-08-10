package com.funnywolf.hollowkit.scenes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.FragmentActivity
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.databinding.ScenePermissionRequestBinding
import com.funnywolf.hollowkit.permission.requestPermissions

/**
 * @author https://github.com/funnywolfdadada
 * @since 2021/2/25
 */
class PermissionRequestScene: Scene() {

    private val TAG = "Permission"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val binding = ScenePermissionRequestBinding.inflate(inflater, container, false)
        val checkBoxes = binding.layout.let { vg ->
            ArrayList<CheckBox>().also { cs ->
                for (i in 0 until vg.childCount) {
                    (vg.getChildAt(i) as? CheckBox)?.also {
                        cs.add(it)
                    }
                }
            }
        }
        binding.request.setOnClickListener { v ->
            val ps = checkBoxes.filter { it.isChecked }.map { it.text.toString() }.toTypedArray()
            val start = System.currentTimeMillis()
            ps.forEach {
                v.context.checkSelfPermission(it)
            }
            Log.d(TAG, "check ${ps.size} cost ${System.currentTimeMillis() - start}ms")
            (activity as FragmentActivity).requestPermissions(ps)  { g, d ->
                Log.d(TAG, "granted: $g, denied: $d")
            }
            v.postDelayed({
                (activity as FragmentActivity).requestPermissions(ps)  { g, d ->
                    Log.d(TAG, "granted: $g, denied: $d")
                }
            }, 200)
        }
        return binding.root
    }

}