package com.github.noamm9.utils.dungeons.map.utils

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.utils.DataDownloader
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.dungeons.map.DungeonInfo
import com.github.noamm9.utils.dungeons.map.core.Room
import com.github.noamm9.utils.dungeons.map.core.RoomData
import com.github.noamm9.utils.dungeons.map.core.UniqueRoom
import com.github.noamm9.utils.dungeons.map.handlers.DungeonScanner
import com.github.noamm9.utils.dungeons.map.handlers.DungeonScanner.startX
import com.github.noamm9.utils.dungeons.map.handlers.DungeonScanner.startZ
import com.github.noamm9.utils.world.WorldUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import kotlin.math.round

object ScanUtils {
    private val roomList by lazy { DataDownloader.loadJson<List<RoomData>>("rooms.json") }

    fun getRoomData(hash: Int) = roomList.find { hash in it.cores }
    fun getRoomData(name: String) = roomList.find { it.name == name }

    fun getRoomGraf(pos: Vec3): Pair<Int, Int> {
        val roomIndexX = round((pos.x - startX) / DungeonScanner.roomSize).toInt()
        val roomIndexZ = round((pos.z - startZ) / DungeonScanner.roomSize).toInt()
        val gridX = roomIndexX * 2
        val gridZ = roomIndexZ * 2
        return gridX.coerceIn(0, 10) to gridZ.coerceIn(0, 10)
    }

    fun getRoomCorner(pair: Pair<Int, Int>): Pair<Int, Int> {
        return Pair(
            - 200 + pair.first * 32,
            - 200 + pair.second * 32
        )
    }

    fun getRoomCenter(pair: Pair<Int, Int>): Pair<Int, Int> {
        return Pair(pair.first + 15, pair.second + 15)
    }

    fun getRoomFromPos(vec: Vec3): UniqueRoom? {
        val (gx, gz) = getRoomGraf(vec)
        val unq = (DungeonInfo.dungeonList[gz * 11 + gx] as? Room)?.uniqueRoom
        return unq
    }

    fun getCore(x: Int, z: Int): Int {
        val sb = StringBuilder(150)

        for (y in 140 downTo 12) {
            val id = LegacyRegistry.getLegacyId(WorldUtils.getStateAt(BlockPos(x, y, z)))
            if (id.equalsOneOf(5, 54, 146)) continue
            sb.append(id)
        }
        return sb.toString().hashCode()
    }

    fun BlockPos.rotate(degree: Int): BlockPos {
        return when ((degree % 360 + 360) % 360) {
            0 -> BlockPos(x, y, z)
            90 -> BlockPos(z, y, - x)
            180 -> BlockPos(- x, y, - z)
            270 -> BlockPos(- z, y, x)
            else -> BlockPos(x, y, z)
        }
    }

    fun getHighestY(x: Int, z: Int): Int {
        mc.level ?: return - 1
        var height = 0

        for (idx in 256 downTo 0) {
            val blockState = WorldUtils.getStateAt(x, idx, z)
            val block = blockState?.block ?: continue
            if (blockState.isAir || block == Blocks.GOLD_BLOCK) continue

            height = idx
            break
        }

        return height
    }
}