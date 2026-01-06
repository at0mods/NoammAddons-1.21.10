package com.github.noamm9.features.impl.visual

import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.utils.location.LocationUtils
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.height
import com.github.noamm9.utils.render.Render2D.width

object PetDisplay: Feature("Displays your active pet on screen") {
    private val chatPetRuleRegex = Regex("§cAutopet §eequipped your §7\\[Lvl .*] (?<pet>.*)§e! §a§lVIEW RULE")
    private val chatSpawnRegex = Regex("§aYou summoned your (?<pet>.*)§a!")
    private val chatDespawnRegex = Regex("§aYou despawned your .*§a!")

    val petDisplayHud = hudElement("PetDisplay", { LocationUtils.inSkyblock && cacheData.getData()["pet"] != null }) { context, example ->
        val text = if (example) "&6Golden Dragon" else cacheData.getData()["pet"].toString()
        Render2D.drawString(context, text, 0, 0)
        return@hudElement text.width().toFloat() to text.height().toFloat()
    }

    override fun init() {
        register<ChatMessageEvent> {
            if (! LocationUtils.inSkyblock) return@register
            event.formattedText.let {
                if (chatSpawnRegex.matches(it)) {
                    cacheData.getData().remove("pet")
                    return@register
                }

                val match1 = chatDespawnRegex.find(it)?.destructured?.component1()
                val match3 = chatPetRuleRegex.find(it)?.destructured?.component1()
                cacheData.getData()["pet"] = match1 ?: match3 ?: return@let
            }
        }
    }
}