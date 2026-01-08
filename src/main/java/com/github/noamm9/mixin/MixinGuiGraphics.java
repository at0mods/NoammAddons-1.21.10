package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.tweaks.ScrollableTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiGraphics.class)
public abstract class MixinGuiGraphics {
    @Shadow @Final private Matrix3x2fStack pose;

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void onRenderTooltipPre(Font font, List<ClientTooltipComponent> list, int i, int j, ClientTooltipPositioner clientTooltipPositioner, @Nullable ResourceLocation resourceLocation, CallbackInfo ci) {
        if (list.isEmpty()) {
            ci.cancel();
            return;
        }

        if (ScrollableTooltip.INSTANCE.enabled) {
            float scale = 1 + (float) ScrollableTooltip.scale / 10;

            this.pose.pushMatrix();
            this.pose.translate(i, j);
            this.pose.scale(scale);
            this.pose.translate(ScrollableTooltip.scrollAmountX, ScrollableTooltip.scrollAmountY);
            this.pose.translate(-i, -j);
        }
    }

    @Inject(method = "renderTooltip", at = @At("TAIL"))
    private void onRenderTooltipPost(CallbackInfo ci) {
        if (ScrollableTooltip.INSTANCE.enabled) {
            this.pose.popMatrix();
        }
    }
}