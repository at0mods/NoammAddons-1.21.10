package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.tweaks.Camera;
import com.github.noamm9.features.impl.visual.CustomContainer;
import com.github.noamm9.ui.nodification.NotificationManager;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;
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
    private void onRenderEnd(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci, @Local GuiGraphics context) {
        NotificationManager.render(context);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", shift = At.Shift.BEFORE))
    private void onBeforeScreenRender(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci, @Local GuiGraphics context) {
        float scale = CustomContainer.getScale();
        if (scale != 1.0f) {
            int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            context.pose().pushMatrix();
            context.pose().translate(width / 2f, height / 2f);
            context.pose().scale(scale, scale);
            context.pose().translate(-width / 2f, -height / 2f);

            GlStateManager._enableBlend();
            GlStateManager._blendFuncSeparate(770, 771, 1, 0);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", shift = At.Shift.AFTER))
    private void onAfterScreenRender(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci, @Local GuiGraphics context) {
        if (CustomContainer.getScale() != 1.0f) {
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            context.pose().popMatrix();
        }
    }
}
