package com.github.noamm9.features.impl.visual

import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.event.impl.RenderOverlayEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.DropdownSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.section
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.NumbersUtils.toFixed
import com.github.noamm9.utils.items.ItemUtils.skyblockId
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.width
import net.minecraft.world.entity.EquipmentSlot

object MaskTimers : Feature("Mask Timers") {
    private val onlyInDungeon by ToggleSetting("Dungeons Only", true)
    private val procNotification by ToggleSetting("Proc Notification").section("Notifications")
    private val comeBackNotification by ToggleSetting("Comeback Notification")
    private val invulnerabilityTimers by ToggleSetting("Invulnerability Timers", true)
    private val maskTimerStyle by DropdownSetting("Style", 1, listOf("Normal", "Fuck ass noam style"))

    // --- Style 0 Logic (Trackers) ---
    private class MaskTracker(
        val name: String,
        val colorCode: String,
        val cooldownMs: Int,
        val regex: Regex,
        val checkActive: () -> Boolean
    ) {
        var remainingMs = 0
        var isActive = false
        var wasOnCooldown = false

        fun reset() {
            remainingMs = 0
            isActive = false
            wasOnCooldown = false
        }
    }

    private val trackers = listOf(
        MaskTracker("Bonzo", "&9", 180000, Regex("Your (?:.+ )?Bonzo's Mask saved your life!")) {
            mc.player?.getItemBySlot(EquipmentSlot.HEAD)?.skyblockId?.contains("BONZO_MASK") == true
        },
        MaskTracker("Spirit", "&5", 30000, Regex("Second Wind Activated! Your Spirit Mask saved your life!")) {
            mc.player?.getItemBySlot(EquipmentSlot.HEAD)?.skyblockId?.contains("SPIRIT_MASK") == true
        },
        MaskTracker("Phoenix", "&c", 60000, Regex("Your Phoenix Pet saved you from certain death!")) {
            true
        }
    )

    private enum class Masks(
        val maskName: String,
        val color: String,
        val regex: Regex,
        val cooldownMax: Int,
        val invulnMax: Int
    ) {
        PHOENIX_PET("Phoenix Pet", "&c", Regex("^Your Phoenix Pet saved you from certain death!$"), 60 * 20, 20 * 4),
        SPIRIT_MASK("Spirit Mask", "&f", Regex("^Second Wind Activated! Your Spirit Mask saved your life!$"), 30 * 20, 3 * 20),
        BONZO_MASK("Bonzo Mask", "&9", Regex("^Your (?:. )?Bonzo's Mask saved your life!$"), 180 * 20, 3 * 20);

        val cleanName = maskName.replace("Pet", "").replace("Mask", "").replace(" ", "")
        var timer = -40
        var invTicks = -1
        val cooldownTime get() = timer / 20f

        companion object {
            val activeMasks = ArrayList<Masks>()

            fun updateTimers() {
                activeMasks.removeIf { mask ->
                    if (mask.invTicks != -1) mask.invTicks--
                    if (mask.timer > -40) {
                        mask.timer--
                        false
                    } else true
                }
            }

            fun reset() {
                activeMasks.clear()
                entries.forEach {
                    it.timer = -40
                    it.invTicks = -1
                }
            }
        }
    }

    private val hudElement = hudElement("Mask Timers") { context, example ->
        if (onlyInDungeon.value && !LocationUtils.inDungeon && !example) return@hudElement 0f to 0f

        if (maskTimerStyle.value == 0) {
            val lines = trackers.map { tracker ->
                val displayCooldown = if (example) (tracker.cooldownMs / 2) else tracker.remainingMs
                val arrow = if (tracker.isActive) "&a>" else "&c>"

                if (displayCooldown > 0) {
                    "${tracker.colorCode}${tracker.name} $arrow &e${(displayCooldown / 1000.0).toFixed(2)}"
                } else {
                    "${tracker.colorCode}${tracker.name} $arrow &aReady"
                }
            }
            if (lines.isEmpty() && !example) return@hudElement 0f to 0f
            lines.forEachIndexed { i, text -> Render2D.drawString(context, text, 0, i * 10) }
            return@hudElement (lines.maxOfOrNull { it.width().toFloat() } ?: 0f) to (lines.size * 10f)

        } else {
            val masksToShow = if (example) Masks.entries else Masks.activeMasks
            var maxWidth = 0f
            var yOffset = 0f

            for (mask in masksToShow) {
                val timeDisplay = if (example) mask.cooldownMax / 40f else mask.cooldownTime
                val text = if (timeDisplay > 0) {
                    "${mask.color}${mask.maskName}: &a${timeDisplay.toFixed(1)}"
                } else {
                    "${mask.color}${mask.maskName}: &aREADY"
                }

                Render2D.drawString(context, text, 0, yOffset.toInt())
                val w = text.width().toFloat()
                if (w > maxWidth) maxWidth = w
                yOffset += 10f
            }
            return@hudElement maxWidth to yOffset
        }
    }

    override fun init() {
        register<TickEvent.Start> {
            if (!LocationUtils.inSkyblock || (onlyInDungeon.value && !LocationUtils.inDungeon)) return@register
            trackers.forEach { it.isActive = it.checkActive() }
        }

        register<TickEvent.Server> {
            if (!LocationUtils.inSkyblock) return@register

            trackers.forEach { tracker ->
                if (tracker.remainingMs > 0) {
                    tracker.remainingMs -= 50
                    tracker.wasOnCooldown = true
                } else if (tracker.wasOnCooldown) {
                    tracker.wasOnCooldown = false
                    if (comeBackNotification.value) ChatUtils.showTitle("${tracker.colorCode}${tracker.name} is Ready!")
                }
            }

            Masks.updateTimers()
        }

        register<ChatMessageEvent> {
            if (!LocationUtils.inSkyblock || (onlyInDungeon.value && !LocationUtils.inDungeon)) return@register
            val msg = event.unformattedText

            trackers.forEach { tracker ->
                if (tracker.regex.matches(msg)) {
                    tracker.remainingMs = tracker.cooldownMs
                    if (procNotification.value && maskTimerStyle.value == 0) {
                        ChatUtils.showTitle("${tracker.colorCode}${tracker.name} Procced!")
                    }
                }
            }

            for (mask in Masks.entries) {
                if (mask.regex.matches(msg)) {
                    mask.timer = mask.cooldownMax
                    if (!Masks.activeMasks.contains(mask)) Masks.activeMasks.add(mask)
                    if (procNotification.value && maskTimerStyle.value == 1) {
                        ChatUtils.showTitle("${mask.color}${mask.maskName}")
                    }
                    if (invulnerabilityTimers.value) mask.invTicks = mask.invulnMax
                    break
                }
            }
        }

        register<RenderOverlayEvent> {
            if (!LocationUtils.inSkyblock || Masks.activeMasks.isEmpty() || !invulnerabilityTimers.value) return@register
            val mask = Masks.activeMasks.maxByOrNull { it.invTicks }?.takeIf { it.invTicks != -1 } ?: return@register

            val color = if (mask.invTicks < 20) "&c" else "&a"
            val str = "${mask.color}${mask.cleanName}: $color${(mask.invTicks / 20.0).toFixed(1)}"

            val x = mc.window.guiScaledWidth / 2f
            val y = mc.window.guiScaledHeight / 3f
            Render2D.drawCenteredString(event.context, str, x, y, scale = 1.5f)
        }

        register<WorldChangeEvent> {
            trackers.forEach { it.reset() }
            Masks.reset()
        }
    }
}