package com.funnywolf.hollowkit.results

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author https://github.com/funnywolfdadada
 * @since 2021/2/27
 */
@MainThread
fun FragmentActivity.startActivityForResult(
        intent: Intent, options: Bundle?, callback: ResultCallback
): FragmentActivity {
    val f = (supportFragmentManager.findFragmentByTag(RESULT_FRAGMENT_TAG) as? ResultFragment) ?: ResultFragment().also {
        supportFragmentManager.beginTransaction().add(it, RESULT_FRAGMENT_TAG).commitNowAllowingStateLoss()
    }
    f.startActivityForResult(intent, options, callback)
    return this
}

private const val RESULT_FRAGMENT_TAG = "RESULT_FRAGMENT_TAG"

typealias ResultCallback = (resultCode: Int, data: Intent?)->Unit

open class ResultFragment: Fragment() {

    private val requestCode = AtomicInteger()
    protected val pending = ArrayList<ActivityResult>(2)
    protected val starting = SparseArray<ActivityResult>(2)

    @MainThread
    fun startActivityForResult(intent: Intent, options: Bundle?, callback: ResultCallback) {
        pending.add(ActivityResult(generateRequestCode(), intent, options, callback))
        startActivitiesForResult()
    }

    protected fun generateRequestCode(): Int {
        var code = requestCode.incrementAndGet()
        if (code < 0 || code > 0x0000FFFF) {
            code = 0
            requestCode.set(code)
        }
        return code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivitiesForResult()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        starting[requestCode]?.also {
            starting.remove(requestCode)
            it.callback(resultCode, data)
        }
        startActivitiesForResult()
    }

    private fun startActivitiesForResult() {
        if (!isAdded || pending.isEmpty()) {
            return
        }
        pending.forEach {
            startActivityForResult(it.intent, it.requestCode, it.options)
            starting.put(it.requestCode, it)
        }
        pending.clear()
    }

    protected class ActivityResult(
            val requestCode: Int,
            val intent: Intent,
            val options: Bundle?,
            val callback: ResultCallback
    )
}
