package com.github.noamm9.mixin;

import com.github.noamm9.event.EventBus;
import com.github.noamm9.event.impl.RenderSlotEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {
    @Shadow
    protected int topPos;

    @Shadow
    protected int leftPos;

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void onRenderSlotPre(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (EventBus.post(new RenderSlotEvent.Pre(graphics, slot, leftPos, topPos))) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void onRenderSlotPost(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        EventBus.post(new RenderSlotEvent.Post(graphics, slot, leftPos, topPos));
    }
}