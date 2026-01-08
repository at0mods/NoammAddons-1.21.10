package com.github.noamm9.features.impl.visual

import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.input.MouseButtonInfo

object CustomContainer: Feature() {
    private val customScale by SliderSetting("Custom Container Scale", 1, 0.1, 5, 0.1)

    @JvmStatic
    fun getScale(): Float {
        // Only scale if it's an actual container screen
        return if (mc.screen is AbstractContainerScreen<*>) customScale.value.toFloat() else 1.0f
    }

    // This converts a real mouse coordinate to the "Scaled" coordinate
    // based on the center-scale transformation used in the Mixin
    @JvmStatic
    fun transformMouse(input: Double, screenSize: Int): Double {
        val scale = getScale()
        if (scale == 1.0f) return input
        val center = screenSize / 2.0
        return (input - center) / scale + center
    }

    @JvmStatic
    fun transformMouse(event: MouseButtonEvent, screenWidth: Int, screenHeight: Int): MouseButtonEvent {
        val scale = getScale()
        if (scale == 1.0f) return event
        val centerX = screenWidth / 2.0
        val centerY = screenHeight / 2.0
        return MouseButtonEvent(((event.x - centerX) / scale + centerX), ((event.y - centerY) / scale + centerY), MouseButtonInfo(
            event.button(), event.modifiers()
        ))
    }
}