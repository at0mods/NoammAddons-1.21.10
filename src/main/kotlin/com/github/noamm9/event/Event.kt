package com.github.noamm9.event

abstract class Event(val cancelable: Boolean = false) {
    var isCanceled = false
        set(value) {
            if (! cancelable) return
            field = value
        }

    fun cancel() {
        isCanceled = true
    }
}