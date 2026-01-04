package com.github.noamm9.features.impl.misc

import com.github.noamm9.event.impl.PacketEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.DropdownSetting
import com.github.noamm9.ui.clickgui.componnents.impl.KeybindSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.showIf
import com.github.noamm9.utils.ChatUtils
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import org.lwjgl.glfw.GLFW

object PvpBlink: Feature("Desyncs your position to avoid hits/knockback.") {

    private val mode by DropdownSetting("Mode", "Manual", listOf("Manual", "Auto"))
    private val blinkDuration by SliderSetting("Auto Duration (ms)", 200.0, 50.0, 1000.0, 50.0)
        .showIf { mode.value == "Auto" }

    private val key by KeybindSetting("Blink Key", GLFW.GLFW_KEY_P)
        .showIf { mode.value == "Manual" }

    private var isBlinking = false
    private var blinkStartTime = 0L

    override fun init() {
        register<PacketEvent.Received> {
            if (mode.value == "Auto" && event.packet is ClientboundSetEntityMotionPacket) {
                if (event.packet.id == mc.player?.id) {
                    startBlink()
                }
            }

            while (isBlinking) {
                Thread.sleep(1)
            }
        }

        register<TickEvent.Start> {
            val now = System.currentTimeMillis()
            if (mode.value == "Manual") {
                isBlinking = key.isDown()
            }
            else if (isBlinking) {
                if (now - blinkStartTime > blinkDuration.value.toLong()) {
                    stopBlink()
                }
            }
        }
    }

    private fun startBlink() {
        if (isBlinking) return
        isBlinking = true
        blinkStartTime = System.currentTimeMillis()
        ChatUtils.modMessage("§bBlink Started")
    }

    private fun stopBlink() {
        isBlinking = false
    }

    override fun onDisable() {
        super.onDisable()
        stopBlink()
    }
}
