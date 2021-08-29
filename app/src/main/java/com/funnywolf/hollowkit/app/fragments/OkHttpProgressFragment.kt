package com.funnywolf.hollowkit.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.funnywolf.hollowkit.app.databinding.PageOkhttpProgressBinding
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
class OkHttpProgressFragment: Fragment() {
    private val TAG = "OkHttpProgress"
    private val client = OkHttpClient.Builder()
            .addInterceptor(ProgressIntercept)
            .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = PageOkhttpProgressBinding.inflate(inflater, container, false)
        binding.download.setOnClickListener {
            download(binding.progressBar, binding.progressText)
        }
        binding.upload.setOnClickListener {
            upload(binding.progressBar, binding.progressText)
        }
        return binding.root
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
                    Log.d(TAG, text)
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
                Log.d(TAG, text)
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
                    Log.d(TAG, text)
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
                Log.d(TAG, text)
                progressText.post { progressText.text = text }
            }
        })
    }

}