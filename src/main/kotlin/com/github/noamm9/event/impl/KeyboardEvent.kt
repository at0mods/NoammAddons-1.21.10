package com.github.noamm9.event.impl

import com.github.noamm9.event.Event

class KeyboardEvent(val key: Int, val modifiers: Int, val scanCode: Int): Event(true)