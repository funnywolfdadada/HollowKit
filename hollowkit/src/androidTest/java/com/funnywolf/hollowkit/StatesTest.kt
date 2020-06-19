package com.funnywolf.hollowkit

import org.junit.Test

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/6/19
 */
class StatesTest {

    @Test
    fun test() {
        concurrentTest()
    }

    @Test
    fun concurrentTest() {
        val types = arrayOf("0", "1", "2", "3", "4")
        val states = States()
        states
            .addListener(object : StateListener {
                override fun onStateChanged(state: State) {
                    println("$this onStateChanged: $state")
                    if (state.type == types[0]) {
                        states.removeListener(this)
                    }
                }
            })
            .addListener(object : StateListener {
                override fun onStateChanged(state: State) {
                    println("$this onStateChanged: $state")
                    if (state.type == types[1]) {
                        states.removeListener(this)
                    }
                }
            })
            .addListener(object : StateListener {
                override fun onStateChanged(state: State) {
                    println("$this onStateChanged: $state")
                    if (state.type == types[2]) {
                        states.removeListener(this)
                    }
                }
            })
            .addListener(object : StateListener {
                override fun onStateChanged(state: State) {
                    println("$this onStateChanged: $state")
                    if (state.type == types[3]) {
                        states.removeListener(this)
                    }
                }
            })
            .addListener(object : StateListener {
                override fun onStateChanged(state: State) {
                    println("$this onStateChanged: $state")
                    if (state.type == types[4]) {
                        states.removeListener(this)
                    }
                }
            })
            .addListener(object : StateListener {
                override fun onStateChanged(state: State) {
                    println("$this onStateChanged: $state")
                }
            })

        types.forEachIndexed { i, s ->
            states.set(when(i % 3) {
                0 -> State.ready(s, i)
                1 -> State.loading(s, i)
                else -> State.error(s, null, i)
            })
        }
    }

}