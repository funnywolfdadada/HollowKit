package com.funnywolf.hollowkit.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.SparseArray
import androidx.annotation.MainThread
import androidx.core.util.isEmpty
import androidx.fragment.app.FragmentActivity
import com.funnywolf.hollowkit.results.ResultFragment
import com.funnywolf.hollowkit.results.startActivityForResult

/**
 * @author https://github.com/funnywolfdadada
 * @since 2021/2/24
 */
@MainThread
fun FragmentActivity.requestPermissions(
        permissions: Array<String>, callback: PermissionCallback
): FragmentActivity {
    if (isAllPermissionGranted(permissions)) {
        callback(permissions.toList(), emptyList())
        return this
    }
    val f = (supportFragmentManager.findFragmentByTag(PERMISSION_FRAGMENT_TAG) as? PermissionFragment) ?: PermissionFragment().also {
        supportFragmentManager.beginTransaction().add(it, PERMISSION_FRAGMENT_TAG).commitNowAllowingStateLoss()
    }
    f.requestPermission(permissions, callback)
    return this
}

fun FragmentActivity.requestPermissionsInSettings(
        permissions: Array<String>, callback: PermissionCallback
): FragmentActivity {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", packageName, null))
    startActivityForResult(intent, null) { _, _ ->
        val granted = grantedPermissions(permissions)
        val denied = permissions.toMutableList().also { it.removeAll(granted) }
        callback(granted, denied)
    }
    return this
}

fun Context.isAllPermissionGranted(permissions: Array<String>): Boolean {
    return permissions.find { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED } == null
}

fun Context.grantedPermissions(permissions: Array<String>): List<String> {
    return permissions.filterTo(ArrayList(permissions.size)) {
        checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }
}

typealias PermissionCallback = (granted: List<String>, denied: List<String>)->Unit

private const val PERMISSION_FRAGMENT_TAG = "PERMISSION_FRAGMENT_TAG"

class PermissionFragment: ResultFragment() {

    private var requestingPermissions: PermissionRequest? = null
    private val pendingRequests = SparseArray<PermissionRequest>(2)

    @MainThread
    fun requestPermission(permissions: Array<String>, callback: PermissionCallback) {
        val r = PermissionRequest(generateRequestCode(), permissions, callback)
        pendingRequests.put(r.requestCode, r)
        requestNextPermission()
    }

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         requestNextPermission()
     }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        requestingPermissions?.also {
            if (it.requestCode == requestCode) {
                invokeCallback(it, permissions, grantResults)
                requestingPermissions = null
            } else {
                Log.e(PERMISSION_FRAGMENT_TAG, "requestingPermissions's requestCode ${it.requestCode} != onRequestPermissionsResult's requestCode $requestCode")
            }
        }
        requestNextPermission()
    }

    private fun requestNextPermission() {
        if (!isAdded || requestingPermissions != null || pendingRequests.isEmpty()) {
            return
        }
        val it = pendingRequests.valueAt(0)
        pendingRequests.remove(it.requestCode)
        requestingPermissions = it
        requestPermissions(it.permissions, it.requestCode)
    }

    private fun invokeCallback(r: PermissionRequest, permissions: Array<String>, grantResults: IntArray) {
        val granted = permissions.filterIndexedTo(ArrayList(permissions.size)) { i, _ ->
            grantResults[i] == PackageManager.PERMISSION_GRANTED
        }
        val denied = r.permissions.toMutableList().also { it.removeAll(granted) }
        r.callback(granted, denied)
    }

    private class PermissionRequest(
            val requestCode: Int,
            val permissions: Array<String>,
            val callback: PermissionCallback
    )
}
