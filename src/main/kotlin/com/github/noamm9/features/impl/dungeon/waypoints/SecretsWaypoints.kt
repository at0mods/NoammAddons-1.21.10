package com.github.noamm9.features.impl.dungeon.waypoints

import com.github.noamm9.event.impl.DungeonEvent
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.dungeons.enums.SecretType
import com.github.noamm9.utils.dungeons.map.core.UniqueRoom
import com.github.noamm9.utils.dungeons.map.utils.ScanUtils
import com.github.noamm9.utils.render.Render3D
import com.github.noamm9.utils.render.RenderContext
import com.github.noamm9.utils.world.WorldUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

object SecretsWaypoints {
    private data class SecretWaypoint(val pos: BlockPos, val type: SecretType, val clicked: Boolean = false) {
        val color = when (type) {
            SecretType.REDSTONE_KEY -> Color.RED
            SecretType.WITHER_ESSANCE -> Color.BLACK
            else -> Color.MAGENTA
        }
    }

    private val waypoints by lazy { ScanUtils.roomList.associate { it.name to it.secretCoords } }
    private val currentRoomWaypoints: CopyOnWriteArrayList<SecretWaypoint> = CopyOnWriteArrayList()

    fun onRoomEnter(room: UniqueRoom) {
        if (! DungeonWaypoints.secretWaypoints.value) return
        currentRoomWaypoints.clear()
        if (room.rotation == null) return

        val roomRotation = 360 - room.rotation !!
        val roomCorner = room.corner !!
        val roomName = room.name

        waypoints[roomName]?.let { secretCoords ->
            val roomWaypoints = mutableListOf<SecretWaypoint>()
            secretCoords.redstoneKey.forEach { roomWaypoints.add(SecretWaypoint(ScanUtils.getRealCoord(it, roomCorner, roomRotation), SecretType.REDSTONE_KEY)) }
            secretCoords.wither.forEach { roomWaypoints.add(SecretWaypoint(ScanUtils.getRealCoord(it, roomCorner, roomRotation), SecretType.WITHER_ESSANCE)) }
            secretCoords.bat.forEach { roomWaypoints.add(SecretWaypoint(ScanUtils.getRealCoord(it, roomCorner, roomRotation), SecretType.BAT)) }
            secretCoords.item.forEach { roomWaypoints.add(SecretWaypoint(ScanUtils.getRealCoord(it, roomCorner, roomRotation), SecretType.ITEM)) }
            secretCoords.chest.forEach { roomWaypoints.add(SecretWaypoint(ScanUtils.getRealCoord(it, roomCorner, roomRotation), SecretType.CHEST)) }
            currentRoomWaypoints.addAll(roomWaypoints)
        }
    }

    fun onRenderWorld(ctx: RenderContext) {
        if (! DungeonWaypoints.secretWaypoints.value) return
        if (currentRoomWaypoints.isEmpty()) return

        for (waypoint in currentRoomWaypoints) {
            if (waypoint.type == SecretType.REDSTONE_KEY && WorldUtils.getBlockAt(waypoint.pos) != Blocks.PLAYER_HEAD) continue
            Render3D.renderBlock(ctx, waypoint.pos, waypoint.color, fill = false, outline = true, phase = true)
        }
    }

    fun onSecret(event: DungeonEvent.SecretEvent) {
        if (! DungeonWaypoints.secretWaypoints.value) return
        if (currentRoomWaypoints.isEmpty()) return
        val (batItems, others) = currentRoomWaypoints.partition { it.type.equalsOneOf(SecretType.BAT, SecretType.ITEM) }
        val list = if (event.type.equalsOneOf(SecretType.BAT, SecretType.ITEM)) batItems else others
        list.minByOrNull { it.pos.distSqr(event.pos) }?.let(currentRoomWaypoints::remove)
    }

    fun clear() = currentRoomWaypoints.clear()
}