package com.github.noamm9.features

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.config.Config
import com.github.noamm9.event.EventBus.register
import com.github.noamm9.event.impl.RenderOverlayEvent
import com.github.noamm9.features.impl.dev.ClickGui
import com.github.noamm9.features.impl.dev.CompTest
import com.github.noamm9.features.impl.dungeon.StarMobEsp
import com.github.noamm9.features.impl.general.AutoClicker
import com.github.noamm9.features.impl.general.CakeNumbers
import com.github.noamm9.features.impl.misc.PvpBlink
import com.github.noamm9.features.impl.tweaks.Camera
import com.github.noamm9.features.impl.visual.*
import com.github.noamm9.ui.clickgui.CategoryType
import com.github.noamm9.ui.hud.HudEditorScreen
import com.github.noamm9.ui.hud.HudElement
import com.github.noamm9.ui.utils.Resolution

object FeatureManager {
    val hudElements = mutableListOf<HudElement>()

    val features = mutableSetOf(
        StarMobEsp, AutoClicker, CakeNumbers,
        ClockDisplay, FpsDisplay, TpsDisplay,
        WarpCooldown, PetDisplay, Camera, PvpBlink,

        CompTest, ClickGui
    ).sortedBy { it.name }


    fun registerFeatures() {
        features.forEach {
            it.initialize()
            hudElements.addAll(it.hudElements)
        }
        Config.load()

        register<RenderOverlayEvent> {
            if (mc.screen == HudEditorScreen) return@register
            Resolution.refresh()
            Resolution.apply(event.context)
            hudElements.forEach { if (it.shouldDraw) it.renderElement(event.context, false) }
            Resolution.restore(event.context)
        }
    }

    fun getFeaturesByCategory(category: CategoryType): List<Feature> {
        return features.filter { it.category == category }
    }

    fun getFeatureByName(name: String): Feature? {
        return features.find { it.name == name }
    }

    fun getHudByName(name: String): HudElement? {
        return hudElements.find { it.name == name }
    }
}