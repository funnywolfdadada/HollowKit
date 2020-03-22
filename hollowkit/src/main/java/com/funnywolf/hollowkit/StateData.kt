package com.funnywolf.hollowkit

/**
 * Data with [state] and [error] message
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/2/16
 */
data class StateData<T> (
    val state: Int,
    val data: T? = null,
    val error: Throwable? = null
)

const val STATE_LOADING = 0
const val STATE_READY = 1
const val STATE_ERROR = 2
