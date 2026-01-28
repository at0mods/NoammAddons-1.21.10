package com.github.noamm9.features.impl.visual

import com.github.noamm9.event.EventPriority
import com.github.noamm9.event.impl.RenderOverlayEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.render.Render2D
import java.awt.Color

object DarkMode: Feature("Darkens the screen") {
    private val opacity by SliderSetting("Opacity", 10, 1, 255, 1)

    override fun init() {
        register<RenderOverlayEvent>(EventPriority.HIGHEST) {
            if(!enabled) return@register
            Render2D.drawRect(event.context, 0, 0, mc.window.guiScaledWidth, mc.window.guiScaledHeight, Color.BLACK.withAlpha(opacity.value))
        }
    }
}