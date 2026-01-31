package com.github.noamm9.features.impl.visual

import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.event.impl.RenderOverlayEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.NumbersUtils.toFixed
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.width

object MaskTimers: Feature("Mask Timers") {
    private val popAlert by ToggleSetting("Proc Notification")
    private val invulnerabilityTimers by ToggleSetting("Invulnerability Timers")

    private enum class Masks(
        val maskName: String,
        val color: String,
        val regex: Regex,
        val cooldownMax: Int,
        val invulnMax: Int
    ) {
        PHOENIX_PET(
            "Phoenix Pet", "&c",
            Regex("^Your Phoenix Pet saved you from certain death!$"),
            60 * 20,
            20 * 4
        ),
        SPIRIT_MASK(
            "Spirit Mask", "&f",
            Regex("^Second Wind Activated! Your Spirit Mask saved your life!$"),
            30 * 20,
            3 * 20
        ),
        BONZO_MASK(
            "Bonzo Mask", "&9",
            Regex("^Your (?:. )?Bonzo's Mask saved your life!$"),
            180 * 20,
            3 * 20
        );

        val cleanName = maskName.replace("Pet", "").replace("Mask", "").replace(" ", "")

        var timer = - 40
        var invTicks = - 1
        val cooldownTime get() = timer / 20f

        companion object {
            val activeMasks = ArrayList<Masks>()

            fun updateTimers() {
                activeMasks.removeIf { mask ->
                    if (mask.invTicks != - 1) mask.invTicks --

                    if (mask.timer > - 40) {
                        mask.timer --
                        false
                    }
                    else true
                }
            }

            fun reset() {
                activeMasks.clear()
                entries.forEach {
                    it.timer = - 40
                    it.invTicks = - 1
                }
            }
        }
    }

    override fun init() {
        hudElement("Mask Timers", shouldDraw = { LocationUtils.inSkyblock }) { context, example ->
            val masksToShow = if (example) Masks.entries else Masks.activeMasks

            var maxWidth = 0f
            var yOffset = 0f

            for (mask in masksToShow) {
                val timeDisplay = if (example) mask.cooldownMax / 40f else mask.cooldownTime

                val text = if (timeDisplay > 0) "${mask.color}${mask.maskName}: &a${timeDisplay.toFixed(1)}"
                else "${mask.color}${mask.maskName}: &aREADY"

                Render2D.drawString(context, text, 0, yOffset)

                val w = text.width()
                if (w > maxWidth) maxWidth = w.toFloat()

                yOffset += 10f
            }

            return@hudElement maxWidth to yOffset
        }

        register<TickEvent.Server> { Masks.updateTimers() }
        register<WorldChangeEvent> { Masks.reset() }

        register<ChatMessageEvent> {
            if (! LocationUtils.inSkyblock) return@register
            val msg = event.unformattedText

            for (mask in Masks.entries) {
                if (! mask.regex.matches(msg)) continue
                mask.timer = mask.cooldownMax

                if (! Masks.activeMasks.contains(mask)) Masks.activeMasks.add(mask)
                if (popAlert.value) ChatUtils.showTitle("${mask.color}${mask.maskName}")
                if (invulnerabilityTimers.value) mask.invTicks = mask.invulnMax
                break
            }
        }

        register<RenderOverlayEvent> {
            if (! LocationUtils.inSkyblock || Masks.activeMasks.isEmpty()) return@register
            val mask = Masks.activeMasks.maxByOrNull { it.invTicks }?.takeIf { it.invTicks != - 1 } ?: return@register

            val color = if (mask.invTicks < 20) "&c" else "&a"
            val str = "${mask.color}${mask.cleanName}: $color${(mask.invTicks / 20.0).toFixed(1)}"

            val x = mc.window.guiScaledWidth / 2f
            val y = mc.window.guiScaledHeight / 3f

            Render2D.drawCenteredString(event.context, str, x, y, scale = 1.5f)
        }
    }
}