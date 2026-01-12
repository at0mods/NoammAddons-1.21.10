package com.github.noamm9.utils.world

import com.github.noamm9.NoammAddons.mc
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

object WorldUtils {
    fun getStateAt(pos: BlockPos) = mc.level?.getBlockState(pos) ?: Blocks.AIR.defaultBlockState()
    fun getStateAt(x: Int, y: Int, z: Int) = this.getStateAt(BlockPos(x, y, z))
    fun getBlockAt(pos: BlockPos) = getStateAt(pos).block
    fun getBlockAt(vec3: Vec3) = getBlockAt(BlockPos(vec3.x.toInt(), vec3.y.toInt(), vec3.z.toInt()))
    fun getBlockAt(x: Int, y: Int, z: Int) = getBlockAt(BlockPos(x, y, z))

    fun BlockState.getLegacyId() = LegacyBlockIds.getLegacyId(this)

    fun isChunkLoaded(x: Int, z: Int): Boolean {
        val player = mc.player ?: return false
        val cx = x shr 4
        val cz = z shr 4
        val pcx = player.x.toInt() shr 4
        val pcz = player.z.toInt() shr 4
        val b1 = mc.level?.chunkSource?.hasChunk(x shr 4, z shr 4) ?: false
        val b2 = abs(cx - pcx) + abs(cz - pcz) <= 9
        return b1 && b2
    }

    fun registryName(block: Block): String {
        val registry = BuiltInRegistries.BLOCK.getKey(block)
        return "${registry.namespace}:${registry.path}"
    }

    fun getBlockEntityList(): List<BlockPos> {
        val player = mc.player ?: return emptyList()
        val level = mc.level ?: return emptyList()
        val renderDistance = mc.options.renderDistance().get()
        val pX = player.chunkPosition().x
        val pZ = player.chunkPosition().z

        return buildList {
            for (x in (pX - renderDistance) .. (pX + renderDistance)) {
                for (z in (pZ - renderDistance) .. (pZ + renderDistance)) {
                    val chunk = level.getChunk(x, z, ChunkStatus.FULL, false) ?: continue
                    addAll(chunk.blockEntitiesPos)
                }
            }
        }
    }
}