package com.github.noamm9.event.impl

import com.github.noamm9.event.Event

abstract class ServerEvent: Event() {
    object Connect: ServerEvent()
    object Disconnect: ServerEvent()
}

