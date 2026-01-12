package com.github.noamm9.utils

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.utils.ItemUtils.customData
import com.github.noamm9.utils.ItemUtils.skyblockId
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object PlayerUtils {
    fun swingArm() = with(mc.player !!) {
        if (! swinging || this.swingTime < 0) {
            swingingArm = InteractionHand.MAIN_HAND
            swingTime = - 1
            swinging = true
        }
    }

    fun isHoldingEtherwarpItem(itemstack: ItemStack): Boolean {
        if (mc.isSingleplayer && itemstack.`is`(Items.DIAMOND_SHOVEL)) return true
        return itemstack.skyblockId == "ETHERWARP_CONDUIT" || itemstack.customData.getBoolean("ethermerge").orElse(false)
    }

    fun rotate(yaw_: Float, pitch_: Float) = mc.player?.apply {
        var yaw = xRot + MathUtils.normalizeYaw(yaw_ - xRot)
        var pitch = yRot + MathUtils.normalizeYaw(pitch_ - yRot)

        val rotations = MathUtils.Rotation(yaw, pitch)
        val lastRotations = MathUtils.Rotation(xRot, yRot)

        val fixedRotations = MathUtils.fixRot(rotations, lastRotations)

        yaw = fixedRotations.yaw
        pitch = fixedRotations.pitch

        pitch = MathUtils.normalizePitch(pitch)

        xRot = yaw
        yRot = pitch
    }

    fun getHotbarSlot(i: Int): ItemStack? {
        if (! Inventory.isHotbarSlot(i)) return null
        val player = mc.player ?: return null
        return player.inventory.getItem(i)
    }
}