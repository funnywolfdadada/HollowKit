package com.funnywolf.hollowkit.http

import androidx.annotation.WorkerThread
import okhttp3.*
import okio.*

/**
 * 提供 OkHttp 请求 body 的上下行进度监听
 * 1、OkHttpClient 构建时需要添加应用拦截器 [ProgressIntercept]
 * 2、[Request] 构建时调用 [Request.Builder.uploadProgress] 实现上传进度监听
 * 3、[Request] 构建时调用 [Request.Builder.downloadProgress] 实现下载进度监听
 *
 * @author https://github.com/funnywolfdadada
 * @since 2021/4/13
 */
fun Request.Builder.uploadProgress(listener: OkUploadListener?) = tag(OkUploadListener::class.java, listener)
fun Request.Builder.downloadProgress(listener: OkDownloadListener?) = tag(OkDownloadListener::class.java, listener)

object ProgressIntercept: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val rawRequest = chain.request()
        val uploadListener = rawRequest.tag(OkUploadListener::class.java)
        val downloadListener = rawRequest.tag(OkDownloadListener::class.java)
        // 替换请求 body 实现上传的进度监听
        val request = replaceRequestBody(rawRequest, uploadListener)
        val response = chain.proceed(request)
        // 替换相应 body 实现下载的进度监听
        return replaceResponseBody(response, downloadListener)
    }

    private fun replaceRequestBody(request: Request, listener: OkUploadListener?): Request {
        val body = request.body
        if (body == null || listener == null) {
            return request
        }
        return request.newBuilder()
            .method(request.method, ProgressRequestBody(body, listener))
            .build()
    }

    private fun replaceResponseBody(response: Response, listener: OkDownloadListener?): Response {
        val body = response.body
        if (body == null || listener == null) {
            return response
        }
        return response.newBuilder()
            .body(ProgressResponseBody(body, listener))
            .build()
    }

}

interface OkUploadListener {
    @WorkerThread
    fun upload(curr: Long, contentLength: Long) {}
}

interface OkDownloadListener {
    @WorkerThread
    fun download(curr: Long, contentLength: Long) {}
}

class ProgressResponseBody(
    private val body: ResponseBody,
    private val listener: OkDownloadListener
): ResponseBody() {
    private val contentLength = body.contentLength()

    /**
     * 带进度的 source，套上 buffer 保证所有的读取都经过 [Source.read] 方法而不是 [BufferedSource] 的其他方法
     */
    private val progressSource = body.source().progress {
        listener.download(it, contentLength)
    }.buffer()

    override fun contentLength(): Long = contentLength

    override fun contentType(): MediaType? = body.contentType()

    override fun source(): BufferedSource = progressSource

    override fun toString(): String = "ProgressResponseBody:$body"
}

/**
 * 带进度的 source，每次读取都会回调累计读到的长度（从初始值开始）
 */
fun Source.progress(initLength: Long = 0L, readCallback: (Long)->Unit) = object: ForwardingSource(this) {
    private var curr = initLength

    init {
        // 回调一次初始值
        readCallback(curr)
    }

    override fun read(sink: Buffer, byteCount: Long): Long = super.read(sink, byteCount).also {
        // 没有读到末尾时更新读取长度并回调
        if (it > 0) {
            curr += it
            readCallback(curr)
        }
    }

}

class ProgressRequestBody(
    private val body: RequestBody,
    private val listener: OkUploadListener
): RequestBody() {

    override fun contentLength(): Long = body.contentLength()

    override fun contentType(): MediaType? = body.contentType()

    override fun writeTo(sink: BufferedSink) {
        val contentLength = contentLength()
        // 带进度的 sink，套上 buffer 保证所有的写入都经过 Sink.write 方法而不是 BufferedSink 的其他方法
        val progressSink = sink.progress {
            listener.upload(it, contentLength)
        }.buffer()
        body.writeTo(progressSink)
        // *** 注意 ***
        // progressSink 是个 buffer，走到这里 body 写完了，但是 buffer 里的不一定完全写入 sink
        // 所以要手动 flush 一下，等待数据写入完毕
        progressSink.flush()
    }

    override fun toString(): String = "ProgressRequestBody:$body"
}

/**
 * 带进度的 sink，每次读取都会回调累计写入的长度（从初始值开始）
 */
fun Sink.progress(initLength: Long = 0L, writeCallback: (Long)->Unit) = object: ForwardingSink(this) {
    private var curr = initLength

    init {
        // 回调一次初始值
        writeCallback(curr)
    }

    override fun write(source: Buffer, byteCount: Long) {
        super.write(source, byteCount)
        // 更新读取长度并回调
        if (byteCount > 0) {
            curr += byteCount
            writeCallback(curr)
        }
    }

}
