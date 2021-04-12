package com.funnywolf.hollowkit.scenes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.bytedance.scene.Scene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.http.*
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import okio.sink
import java.io.File
import java.io.IOException

/**
 * @author https://github.com/funnywolfdadada
 * @since 2021/4/13
 */
class OkHttpProgressScene: Scene() {
    private val tag = "OkHttpProgress"
    private val client = OkHttpClient.Builder()
            .addInterceptor(ProgressIntercept)
            .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.scene_okhttp_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val progressText = view.findViewById<TextView>(R.id.progress_text)
        view.findViewById<View>(R.id.download).setOnClickListener {
            download(progressBar, progressText)
        }
        view.findViewById<View>(R.id.upload).setOnClickListener {
            upload(progressBar, progressText)
        }
    }

    private fun download(progressBar: ProgressBar, progressText: TextView) {
        val pic = "https://cn.bing.com/th?id=OHR.YurisNight_ZH-CN5738817931_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp"
        val file = File(progressBar.context.cacheDir, "bing.jpg")
        val request = Request.Builder()
            .url(pic)
            .cacheControl(CacheControl.FORCE_NETWORK)
            .downloadProgress(object: OkDownloadListener {
                override fun download(curr: Long, contentLength: Long) {
                    val text = "Download: $curr/$contentLength"
                    Log.d(tag, text)
                    progressBar.max = (contentLength / 1024).toInt()
                    progressBar.progress = (curr / 1024).toInt()
                    progressText.post { progressText.text = text }
                }
            })
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.also {
                    it.source().readAll(file.sink())
                }
                val text = "Complete: $response}"
                Log.d(tag, text)
                progressText.post { progressText.text = text }
            }
        })
    }

    private fun upload(progressBar: ProgressBar, progressText: TextView) {
        val pic = "https://cn.bing.com/th?id=OHR.YurisNight_ZH-CN5738817931_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp"
        val file = File(progressBar.context.cacheDir, "bing.jpg")
        val request = Request.Builder()
            .url(pic)
            .post(file.asRequestBody())
            .cacheControl(CacheControl.FORCE_NETWORK)
            .uploadProgress(object: OkUploadListener {
                override fun upload(curr: Long, contentLength: Long) {
                    val text = "Upload: $curr/$contentLength"
                    Log.d(tag, text)
                    progressBar.max = (contentLength / 1024).toInt()
                    progressBar.progress = (curr / 1024).toInt()
                    progressText.post { progressText.text = text }
                }
            })
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val text = "Complete: $response}"
                Log.d(tag, text)
                progressText.post { progressText.text = text }
            }
        })
    }

}