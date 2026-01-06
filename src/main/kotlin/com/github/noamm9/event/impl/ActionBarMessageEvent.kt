package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import com.github.noamm9.utils.ChatUtils.formattedText
import com.github.noamm9.utils.ChatUtils.unformattedText
import net.minecraft.network.chat.Component

class ActionBarMessageEvent(val component: Component): Event(cancelable = true) {
    inline val formattedText: String get() = component.formattedText
    inline val unformattedText: String get() = component.unformattedText

    var message = formattedText
}