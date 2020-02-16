package com.funnywolf.hollowkit

import io.reactivex.Observable
import retrofit2.Response
import java.lang.Exception

/**
 * Utilities for [Response]
 *
 * @author funnywolf
 * @since 2020/2/16
 */

fun <T> Response<T>.getBodyIfSuccess(): T? {
    return if (isSuccessful) { body() } else { null }
}

fun <T> Response<T>.getOrError(): T {
    if (!isSuccessful) {
        throw Exception("Response error: ${errorBody()}")
    }
    return body() ?: throw Exception("Body is null, raw = ${raw()}")
}

fun <T> Response<T>.requestSuccess(): Response<T> {
    if (!isSuccessful) {
        throw Exception("Response error: ${errorBody()}")
    }
    return this
}

fun <T> Observable<Response<T>>.mapBody(): Observable<T> {
    return map { it.getOrError() }
}

fun <T> Observable<Response<T>>.requestSuccess(): Observable<Response<T>> {
    return map { it.requestSuccess() }
}
