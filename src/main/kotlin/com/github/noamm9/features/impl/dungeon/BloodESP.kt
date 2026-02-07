package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.event.impl.ChatMessageEvent
import com.github.noamm9.event.impl.RenderWorldEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.render.Render3D
import com.github.noamm9.utils.world.WorldUtils
import net.minecraft.world.phys.Vec3
import org.joml.Vector2i
import java.awt.Color
import kotlin.math.floor

object BloodESP: Feature("Highlight the bloods before the dungeon start to help you for 0s bloodrush") {
  private val tracer by ToggleSetting("Tracer", true)
  private var looping = true
  private var show = false
  private var bloodCoords = emptyList<Int>()

  override fun init() {
    register<ChatMessageEvent> {
      if (!event.unformattedText.startsWith("[BOSS] The Watcher:")) return@register

      show = false

    }

    register<WorldChangeEvent> {
      bloodCoords = emptyList()
      looping = true
      show = false
    }

    register<TickEvent.End> {
      if (!looping) return@register

      val blood = findBlood()
      if (blood.isEmpty()) return@register
      looping = false
      show = true

      ChatUtils.chat("Blood found at x : ${blood[0]}, z : ${blood[1]}")
      bloodCoords = listOf(blood[0] as Int, blood[1] as Int, blood[2] as Int)
    }

    register<RenderWorldEvent> {
      if (bloodCoords.isEmpty() || !show) return@register

      val dist = 16
      val y = 99.0


      val (x, z) = when (bloodCoords[2]) {
        0 -> bloodCoords[0] + .5 to bloodCoords[1] + .5 - dist
        1 -> bloodCoords[0] + .5 - dist to bloodCoords[1] + .5
        2 -> bloodCoords[0] + .5 + dist to bloodCoords[1] + .5
        else -> bloodCoords[0] + .5 to bloodCoords[1] + .5 + dist
      }


      if (tracer.value) Render3D.renderTracer(event.ctx, Vec3(x, y, z), Color.RED, 2)

      Render3D.renderBox(event.ctx, bloodCoords[0] + .5, 66, bloodCoords[1] + .5, 31, 34, Color.RED, true, false, true,)
      Render3D.renderBox(event.ctx, x, y - 0.01, z, 3, 1, Color(255, 0, 0, 125), false, true, false)
    }
  }

  private fun findBlood(): List<Any> {
    val x = -200
    val y = 99
    val z = -200

    var triesx = 0
    var triesz = 0
    while (triesx != 6) {

      val block0 = WorldUtils.getBlockAt(x + 32 * triesx, y, z + 32 * triesz + 9).name.string == "Block of Redstone"
      val block1 = WorldUtils.getBlockAt(x + 32 * triesx + 9, y, z + 32 * triesz + 30).name.string == "Block of Redstone"
      val block2 = WorldUtils.getBlockAt(x + 32 * triesx + 21, y, z + 32 * triesz).name.string == "Block of Redstone"
      val block3 = WorldUtils.getBlockAt(x + 32 * triesx + 30, y, z + 32 * triesz + 21).name.string == "Block of Redstone"

      if (block0 || block1 || block2 || block3) {
//        tickblood.unregister()

//        text = "&c&lBlood found"
//        bloodtitle.register()
//        World.playSound("random.orb", 1, 2)
//        setTimeout(() => {
//          bloodtitle.unregister()
//        }, 1250)

        val orientation = if (block0) 0 else if (block1) 1 else if (block2) 2 else 3
        return listOf(x + triesx * 32 + 15, z + triesz * 32 + 15, orientation)
      }

      triesz++

      if (triesz == 6) {
        triesz = 0
        triesx++
      }

    }

    return emptyList()
  }
}