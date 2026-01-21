package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.dungeon.SecretHitboxes;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClipContext.class)
public class MixinClipContext {
    @WrapOperation(method = "getBlockShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At(target = "Lnet/minecraft/world/level/ClipContext$Block;get(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", value = "INVOKE"))
    VoxelShape getBlockShape(ClipContext.Block instance, BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext, Operation<VoxelShape> original) {
        if (SecretHitboxes.INSTANCE.enabled) return SecretHitboxes.INSTANCE.getShape(blockState).orElse(
            original.call(instance, blockState, blockGetter, blockPos, collisionContext)
        );
        else return original.call(instance, blockState, blockGetter, blockPos, collisionContext);
    }
}