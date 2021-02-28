package com.funnywolf.hollowkit.scenes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.results.startActivityForResult
import com.funnywolf.hollowkit.utils.toast

/**
 * @author https://github.com/funnywolfdadada
 * @since 2021/2/27
 */
class ActivityResultScene: Scene() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.scene_activity_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vs = arrayOf<TextView>(view.findViewById(R.id.c1), view.findViewById(R.id.c2), view.findViewById(R.id.c3))
        view.findViewById<View>(R.id.start).setOnClickListener {
            val act = activity as FragmentActivity
            vs.forEach {
                act.startActivityForResult(buildIntent(it.context, it.text.toString()), null) { c, i ->
                    act.toast("$c, ${i?.getMsg()}")
                }
            }
        }
    }

}

private const val KEY_MSG = "KEY_MSG"
private fun buildIntent(context: Context, msg: String) = Intent().setClass(context, TestResultActivity::class.java).putExtra(KEY_MSG, msg)
private fun Intent.getMsg(): String? = extras?.getString(KEY_MSG)

class TestResultActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val msg = intent.getMsg()
        AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("YES") { _, _ ->
                    setResult(RESULT_OK, Intent().putExtra(KEY_MSG, msg))
                }
                .setNegativeButton("NO") { _, _ ->
                    setResult(RESULT_CANCELED, Intent().putExtra(KEY_MSG, msg))
                }
                .setOnDismissListener {
                    finish()
                }
                .setCancelable(true)
                .show()
    }

}