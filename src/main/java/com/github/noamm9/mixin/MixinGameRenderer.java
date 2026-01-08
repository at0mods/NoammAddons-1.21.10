package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.tweaks.Camera;
import com.github.noamm9.ui.nodification.NotificationManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow @Final private GuiRenderState guiRenderState;

    @Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
    private static void onGetNightVisionScale(LivingEntity entity, float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (Camera.isFullBright()) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    public void onBobHurt(PoseStack poseStack, float f, CallbackInfo ci) {
        if (this.minecraft.options.damageTiltStrength().get() == 0) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderDeferredSubtitles()V"))
    private void onRenderEnd(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        GuiGraphics guiGraphics = new GuiGraphics(this.minecraft, this.guiRenderState);
        NotificationManager.render(guiGraphics);
    }
}
