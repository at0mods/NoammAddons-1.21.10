package com.github.noamm9.features.impl.tweaks

import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.features.Feature
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

object SnappyTappy: Feature("Prevents standing still when pressing opposing direction keys") {
    private val pressTicks = mutableMapOf<KeyMapping, Long>()
    private val prevState = mutableMapOf<KeyMapping, Boolean>()
    private var tick = 0L

    override fun init() {
        register<TickEvent.Start> {
            tick ++
            val o = mc.options
            val movementKeys = listOf(o.keyLeft, o.keyRight, o.keyUp, o.keyDown, o.keyJump, o.keyShift, o.keySprint)

            if (mc.screen != null) {
                if (pressTicks.isNotEmpty()) {
                    movementKeys.forEach { it.isDown = false }
                    pressTicks.clear()
                    prevState.clear()
                }
                return@register
            }

            for (key in movementKeys) {
                val physicalDown = isKeyDown(key)
                if (physicalDown && prevState[key] != true) {
                    pressTicks[key] = tick
                }
                prevState[key] = physicalDown
                key.isDown = physicalDown
            }

            resolvePair(o.keyLeft, o.keyRight)
            resolvePair(o.keyUp, o.keyDown)
        }
    }

    private fun resolvePair(a: KeyMapping, b: KeyMapping) {
        if (! a.isDown || ! b.isDown) return
        val timeA = pressTicks[a] ?: 0L
        val timeB = pressTicks[b] ?: 0L
        if (timeA >= timeB) b.isDown = false
        else a.isDown = false
    }

    private fun isKeyDown(key: KeyMapping): Boolean {
        val handle = mc?.window?.handle() ?: return false
        val bound = KeyBindingHelper.getBoundKeyOf(key) ?: return false
        return if (bound.type == InputConstants.Type.MOUSE) GLFW.glfwGetMouseButton(handle, bound.value) == GLFW.GLFW_PRESS
        else GLFW.glfwGetKey(handle, bound.value) == GLFW.GLFW_PRESS
    }
}