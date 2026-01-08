package com.github.noamm9.features.impl.general

import com.github.noamm9.event.impl.MouseClickEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.mixin.IKeyMapping
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.utils.PlayerUtils
import org.lwjgl.glfw.GLFW

object Etherwarp: Feature() {
    private val leftClickEtherwarp by ToggleSetting("Left Click Etherwarp", true)

    override fun init() {
        register<MouseClickEvent> {
            if (! leftClickEtherwarp.value) return@register
            if (event.button != 0) return@register
            if (event.action != GLFW.GLFW_PRESS) return@register
            val player = mc.player ?: return@register
            if (! player.isCrouching) return@register
            val item = player.inventory.selectedItem ?: return@register
            if (! PlayerUtils.isHoldingEtherwarpItem(item)) return@register
            event.isCanceled = true

            (mc.options.keyUse as IKeyMapping).clickCount += 1
            PlayerUtils.swingArm()
        }
    }
}