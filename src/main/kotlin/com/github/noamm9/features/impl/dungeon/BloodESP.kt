package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.event.impl.RenderWorldEvent
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.withDescription
import com.github.noamm9.utils.MathUtils.Vec3
import com.github.noamm9.utils.dungeons.DungeonListener
import com.github.noamm9.utils.dungeons.map.DungeonInfo
import com.github.noamm9.utils.dungeons.map.handlers.DungeonScanner
import com.github.noamm9.utils.render.Render3D
import com.github.noamm9.utils.world.WorldUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import java.awt.Color

object BloodESP: Feature("Highlight the bloods before the dungeon start to help you for 0s bloodrush") {
    private val tracer by ToggleSetting("Tracer", true).withDescription("Draws a tracer to the blood room's door")

    private var bloodData: Pair<BlockPos, Int>? = null
    private var scanTicks = 0

    override fun init() {
        register<WorldChangeEvent> {
            bloodData = null
            scanTicks = 0
        }

        register<TickEvent.Start> {
            if (DungeonListener.dungeonStarted) return@register
            if (bloodData != null) return@register
            bloodData = findBlood()
        }

        register<RenderWorldEvent> {
            if (DungeonListener.dungeonStarted) return@register
            val (center, rotation) = bloodData ?: return@register
            val halfRoom = DungeonScanner.roomSize / 2

            val (doorX, doorZ) = when (rotation) {
                0 -> center.x + 0.5 to (center.z + 0.5 - halfRoom)
                1 -> (center.x + 0.5 - halfRoom) to center.z + 0.5
                2 -> (center.x + 0.5 + halfRoom) to center.z + 0.5
                else -> center.x + 0.5 to (center.z + 0.5 + halfRoom)
            }

            if (tracer.value) {
                Render3D.renderTracer(event.ctx, Vec3(doorX, center.y, doorZ), Color.RED)
            }

            Render3D.renderBox(
                event.ctx,
                center.x + 0.5, 66, center.z + 0.5,
                31, 34,
                Color.RED,
                outline = true,
                fill = false,
                phase = true
            )
        }
    }

    private fun findBlood(): Pair<BlockPos, Int>? {
        val mainRoom = DungeonInfo.uniqueRooms["Blood"]?.mainRoom ?: return null
        val pos = BlockPos(mainRoom.x, 99, mainRoom.z)

        DungeonScanner.clayBlocksCorners.forEachIndexed { i, (dx, dz) ->
            if (WorldUtils.getBlockAt(pos.x + dx, pos.y, pos.z + dz) == Blocks.BLUE_TERRACOTTA) {
                return pos to i
            }
        }

        return null
    }
}