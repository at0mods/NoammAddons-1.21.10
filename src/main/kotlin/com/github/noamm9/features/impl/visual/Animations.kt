package com.github.noamm9.features.impl.visual

import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.*
import com.github.noamm9.ui.clickgui.componnents.impl.ButtonSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.utils.ItemUtils.skyblockId
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.util.Mth
import kotlin.math.exp

object Animations: Feature("Allows you to modify your hand viewmodel") {
    val mainHandItemScale by SliderSetting("Item Scale", 0.0, - 1.5f, 1.5f, 0.05f)
        .withDescription("0 is normal size. -0.5 is half size. 1 is double size.")

    val mainHandX by SliderSetting("X", 0.0, - 2.0, 2.0, 0.05)
    val mainHandY by SliderSetting("Y", 0.0, - 2.0, 2.0, 0.05)
    val mainHandZ by SliderSetting("Z", 0.0, - 2.0, 2.0, 0.05)
    val mainHandPositiveX by SliderSetting("Rotation X", 0f, - 50f, 50f)
    val mainHandPositiveY by SliderSetting("Rotation Y", 0f, - 50f, 50f)
    val mainHandPositiveZ by SliderSetting("Rotation Z", 0f, - 50f, 50f)

    val disableReequip by ToggleSetting("Disable equip animation").withDescription("Disables the equip animation when ur held item changes.")
    val disableSwingAnimation by ToggleSetting("Disable swing animation").withDescription("Disables the held item swing animation")
    val terminatorOnly by ToggleSetting("Terminator Only").withDescription("Disables the swing animation only for terminator").showIf { disableSwingAnimation.value }

    val swingSpeed by SliderSetting("Swing Speed", 0, - 2f, 1f, 0.05f).hideIf { disableSwingAnimation.value }
    val ignoreHaste by ToggleSetting("Ignore Haste").withDescription("Ignores the haste speed boost.").hideIf { disableSwingAnimation.value }
    private val scaleSwing by ToggleSetting("Scale Swing").hideIf { disableSwingAnimation.value }

    private val reset by ButtonSetting("Reset") {
        configSettings.forEach(Setting<*>::reset)
    }

    @JvmStatic
    fun disableItemDip(equipProgress: Float): Float {
        if (enabled && disableReequip.value) {
            return 0f
        }
        return equipProgress
    }

    @JvmStatic
    fun disableSwingAnimation(swingProgress: Float): Float {
        if (! enabled) return swingProgress
        if (disableSwingAnimation.value) {
            if (terminatorOnly.value) {
                val isHoldingTerminator = mc.player?.mainHandItem?.skyblockId == "TERMINATOR"
                return if (isHoldingTerminator) 1f else swingProgress
            }
            return 1f
        }
        return swingProgress
    }

    @JvmStatic
    fun applyItemTransforms(matrices: PoseStack) {
        if (! enabled) return

        val s = (1.0f + mainHandItemScale.value.toFloat()).coerceAtLeast(0.001f)
        matrices.translate(mainHandX.value.toFloat(), mainHandY.value.toFloat(), mainHandZ.value.toFloat())
        matrices.mulPose(Axis.XP.rotationDegrees(mainHandPositiveX.value.toFloat()))
        matrices.mulPose(Axis.YP.rotationDegrees(mainHandPositiveY.value.toFloat()))
        matrices.mulPose(Axis.ZP.rotationDegrees(mainHandPositiveZ.value.toFloat()))
        matrices.scale(s, s, s)
    }

    @JvmStatic
    fun scaledSwing(matrices: PoseStack, swingProgress: Float) {
        if (! scaleSwing.value || ! enabled) return

        val scale = exp(1 + mainHandItemScale.value.toDouble()).toFloat()
        val f = - 0.4f * Mth.sin(Mth.sqrt(swingProgress) * Math.PI.toFloat()) * scale
        val f1 = 0.2f * Mth.sin(Mth.sqrt(swingProgress) * Math.PI.toFloat() * 2.0f) * scale
        val f2 = - 0.2f * Mth.sin(swingProgress * Math.PI.toFloat()) * scale
        matrices.translate(f, f1, f2)
    }
}