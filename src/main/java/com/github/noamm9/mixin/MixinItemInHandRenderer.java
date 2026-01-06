package com.github.noamm9.mixin;


import com.github.noamm9.features.impl.visual.Animations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {
    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), ordinal = 3, argsOnly = true)
    private float disableItemDip(float equipProgress) {
        return Animations.disableItemDip(equipProgress);
    }

    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private float modifySwingProgress(float equipProgress) {
        return Animations.disableSwingAnimation(equipProgress);
    }

    @Inject(method = "applyItemArmTransform", at = @At("TAIL"))
    private void hookItemScaleTail(PoseStack matrices, HumanoidArm arm, float swingProgress, CallbackInfo ci) {
        Animations.applyItemTransforms(matrices);
        Animations.scaledSwing(matrices, swingProgress);
    }
}

