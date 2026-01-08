package com.github.noamm9.features.impl.general

import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.mixin.IKeyMapping
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.hideIf
import com.github.noamm9.ui.clickgui.componnents.impl.KeybindSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.withDescription
import com.github.noamm9.utils.ItemUtils.skyblockId

object AutoClicker: Feature("Automatically clicks for you.") {
    private val cps by SliderSetting("Clicks Per Second", 5, 1, 15).withDescription("How many times per second the auto should click.")
    private val terminatorCheck by ToggleSetting("Terminator Only", false).withDescription("Only activate when holding a Terminator.")
    private val leftClickToggle by ToggleSetting("Left Click", false).hideIf { terminatorCheck.value }
    private val leftClickKeybind by KeybindSetting("Left Click Keybind").hideIf { terminatorCheck.value || ! leftClickToggle.value }
    private val rightClickToggle by ToggleSetting("Right Click", false).hideIf { terminatorCheck.value }
    private val rightClickKeybind by KeybindSetting("Right Click Keybind").hideIf { terminatorCheck.value || ! rightClickToggle.value }

    private var nextLeftClick = 0L
    private var nextRightClick = 0L

    override fun init() {
        register<TickEvent.Start> {
            if (mc.screen != null || mc.player == null) return@register
            val player = mc.player?.takeUnless { it.isUsingItem } ?: return@register
            val now = System.currentTimeMillis()

            if (terminatorCheck.value) {
                if (! mc.options.keyUse.isDown) return@register
                if (player.mainHandItem.skyblockId != "TERMINATOR") return@register
                if (now < nextLeftClick) return@register

                nextLeftClick = getNextClick(now)
                (mc.options.keyAttack as IKeyMapping).clickCount += 1
            }
            else {
                if (leftClickToggle.value && leftClickKeybind.isDown() && now >= nextLeftClick) {
                    nextLeftClick = getNextClick(now)
                    (mc.options.keyAttack as IKeyMapping).clickCount += 1
                }

                if (rightClickToggle.value && rightClickKeybind.isDown() && now >= nextRightClick) {
                    nextRightClick = getNextClick(now)
                    (mc.options.keyUse as IKeyMapping).clickCount += 1
                }
            }
        }
    }

    private fun getNextClick(now: Long): Long {
        val delay = (1000.0 / cps.value.toInt()).toLong()
        val randomOffset = (Math.random() * 60.0 - 30.0).toLong()
        return now + delay + randomOffset
    }
}

