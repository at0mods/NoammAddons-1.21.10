package com.github.noamm9.features.impl.dungeon

import com.github.noamm9.features.Feature
import com.github.noamm9.ui.clickgui.componnents.getValue
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.ui.clickgui.componnents.provideDelegate
import com.github.noamm9.ui.clickgui.componnents.withDescription
import com.github.noamm9.utils.location.LocationUtils
import net.minecraft.core.Direction
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock
import net.minecraft.world.level.block.LeverBlock
import net.minecraft.world.level.block.SkullBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.AttachFace
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.*

object SecretHitboxes: Feature("Changes the hitboxes of secret blocks to be larger.") {
    val lever by ToggleSetting("Lever").withDescription("Full block lever hitbox.")
    val button by ToggleSetting("Button").withDescription("Full block button hitbox.")
    val skull by ToggleSetting("Skulls").withDescription("Full block Skull hitbox.")

    fun getShape(state: BlockState): Optional<VoxelShape> {
        if (! LocationUtils.inDungeon) return Optional.ofNullable(null)

        return Optional.ofNullable(
            when (state.block) {
                is SkullBlock if skull.value -> Shapes.block()
                is LeverBlock if lever.value -> Shapes.block()
                is ButtonBlock if button.value -> getHackButtonShape(
                    state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE),
                    state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING),
                    state.getValue(ButtonBlock.POWERED)
                )

                else -> null
            }
        )
    }

    private fun getHackButtonShape(face: AttachFace, direction: Direction, powered: Boolean): VoxelShape {
        val f2 = (if (powered) 1 else 2) / 16.0
        return when (face) {
            AttachFace.CEILING -> Shapes.box(0.0, 1.0 - f2, 0.0, 1.0, 1.0, 1.0)
            AttachFace.FLOOR -> Shapes.box(0.0, 0.0, 0.0, 1.0, 0.0 + f2, 1.0)
            else -> when (direction) {
                Direction.EAST -> Shapes.box(0.0, 0.0, 0.0, f2, 1.0, 1.0)
                Direction.WEST -> Shapes.box(1.0 - f2, 0.0, 0.0, 1.0, 1.0, 1.0)
                Direction.SOUTH -> Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, f2)
                Direction.NORTH -> Shapes.box(0.0, 0.0, 1.0 - f2, 1.0, 1.0, 1.0)
                Direction.UP -> Shapes.box(0.0, 0.0, 0.0, 1.0, 0.0 + f2, 1.0)
                Direction.DOWN -> Shapes.box(0.0, 1.0 - f2, 0.0, 1.0, 1.0, 1.0)
            }
        }
    }
}