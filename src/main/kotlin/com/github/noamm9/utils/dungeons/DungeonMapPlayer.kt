package com.github.noamm9.utils.dungeons


import com.github.noamm9.utils.dungeons.map.handlers.DungeonScanner
import com.github.noamm9.utils.dungeons.map.utils.MapUtils
import net.minecraft.world.phys.Vec3

data class DungeonMapPlayer(val teammate: DungeonPlayer) {
    var mapX = 0f
    var mapZ = 0f
    var yaw = 0f
    var icon = ""

    fun getRealPos() = Vec3(
        (mapX - MapUtils.startCorner.first) / MapUtils.coordMultiplier + DungeonScanner.startX - 15,
        teammate.entity?.y ?: .0,
        (mapZ - MapUtils.startCorner.second) / MapUtils.coordMultiplier + DungeonScanner.startZ - 15
    )
}