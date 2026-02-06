package com.github.noamm9.features.impl.general.teleport

import com.github.noamm9.NoammAddons
import com.github.noamm9.utils.MathUtils.add
import com.github.noamm9.utils.Utils.equalsOneOf
import com.github.noamm9.utils.items.ItemUtils.customData
import com.github.noamm9.utils.items.ItemUtils.skyblockId
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.CarpetBlock
import net.minecraft.world.level.block.SkullBlock
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sign

object EtherwarpHelper {
    private const val EYE_HEIGHT = 1.62
    private const val SNEAK_OFFSET = 0.08

    private val extraPassable = listOf(CarpetBlock::class.java, SkullBlock::class.java)

    data class EtherPos(val succeeded: Boolean, val pos: BlockPos?) {
        companion object {
            val NONE = EtherPos(false, null)
        }
    }

    fun getEtherPos(pos: Vec3, distance: Double = 60.0, returnEnd: Boolean = false): EtherPos {
        val player = NoammAddons.mc.player ?: return EtherPos.NONE
        val startPos = pos.add(y = EYE_HEIGHT - if (player.isCrouching) SNEAK_OFFSET else 0.0)
        val endPos = startPos.add(player.lookAngle.scale(distance))
        val result = traverseVoxels(startPos, endPos)

        return if (result != null) EtherPos(true, result)
        else if (returnEnd) EtherPos(true, BlockPos.containing(endPos))
        else EtherPos.NONE
    }

    fun getEtherwarpDistance(stack: ItemStack): Double? {
        if (stack.skyblockId.equalsOneOf("ASPECT_OF_THE_VOID", "ASPECT_OF_THE_END")) {
            val nbt = stack.customData
            if (nbt.getByte("ethermerge").orElse(0) != 1.toByte()) return null
            val tuners = nbt.getByte("tuned_transmission").orElse(0).toInt()
            return 57.0 + tuners
        }
        return null
    }

    private fun traverseVoxels(start: Vec3, end: Vec3): BlockPos? {
        var x = floor(start.x).toInt()
        var y = floor(start.y).toInt()
        var z = floor(start.z).toInt()

        val endX = floor(end.x).toInt()
        val endY = floor(end.y).toInt()
        val endZ = floor(end.z).toInt()

        val dirX = end.x - start.x
        val dirY = end.y - start.y
        val dirZ = end.z - start.z

        val stepX = sign(dirX).toInt()
        val stepY = sign(dirY).toInt()
        val stepZ = sign(dirZ).toInt()

        val tDeltaX = if (dirX == 0.0) Double.POSITIVE_INFINITY else abs(1.0 / dirX)
        val tDeltaY = if (dirY == 0.0) Double.POSITIVE_INFINITY else abs(1.0 / dirY)
        val tDeltaZ = if (dirZ == 0.0) Double.POSITIVE_INFINITY else abs(1.0 / dirZ)

        var tMaxX = if (dirX == 0.0) Double.POSITIVE_INFINITY else abs((floor(start.x) + max(0.0, stepX.toDouble()) - start.x) / dirX)
        var tMaxY = if (dirY == 0.0) Double.POSITIVE_INFINITY else abs((floor(start.y) + max(0.0, stepY.toDouble()) - start.y) / dirY)
        var tMaxZ = if (dirZ == 0.0) Double.POSITIVE_INFINITY else abs((floor(start.z) + max(0.0, stepZ.toDouble()) - start.z) / dirZ)

        val currentPos = BlockPos.MutableBlockPos()

        repeat(1000) {
            currentPos.set(x, y, z)

            if (isValidEtherwarpBlock(currentPos)) return currentPos
            if (! isPassable(currentPos)) return null
            if (x == endX && y == endY && z == endZ) return null

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    tMaxX += tDeltaX
                    x += stepX
                }
                else {
                    tMaxZ += tDeltaZ
                    z += stepZ
                }
            }
            else {
                if (tMaxY < tMaxZ) {
                    tMaxY += tDeltaY
                    y += stepY
                }
                else {
                    tMaxZ += tDeltaZ
                    z += stepZ
                }
            }
        }

        return null
    }

    private fun isValidEtherwarpBlock(pos: BlockPos): Boolean {
        if (isPassable(pos)) return false
        if (! isPassable(pos.above(1))) return false
        return isPassable(pos.above(2))
    }

    private fun isPassable(pos: BlockPos): Boolean {
        val level = NoammAddons.mc.level ?: return true
        val state = level.getBlockState(pos)
        if (extraPassable.any { it.isInstance(state.block) }) return true
        return state.getCollisionShape(level, pos, CollisionContext.empty()).isEmpty
    }
}