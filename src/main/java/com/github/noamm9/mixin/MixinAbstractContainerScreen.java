package com.github.noamm9.mixin;

import com.github.noamm9.event.EventBus;
import com.github.noamm9.event.impl.ContainerEvent;
import com.github.noamm9.features.impl.tweaks.ScrollableTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {
    @Shadow @Nullable protected Slot hoveredSlot;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    protected void onInit(CallbackInfo ci) {
        if (EventBus.post(new ContainerEvent.Open((Screen) (Object) this))) {
            ci.cancel();
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    protected void onClose(CallbackInfo ci) {
        if (EventBus.post(new ContainerEvent.Close((Screen) (Object) this))) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlotPre(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (EventBus.post(new ContainerEvent.Render.Slot.Pre((Screen) (Object) this, guiGraphics, slot))) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void onDrawSlotPost(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        EventBus.post(new ContainerEvent.Render.Slot.Post((Screen) (Object) this, guiGraphics, slot));
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClickedSlot(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (slot == null) return;

        if (EventBus.post(new ContainerEvent.SlotClick((Screen) (Object) this, slot, button, actionType))) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (EventBus.post(new ContainerEvent.MouseClick((Screen) (Object) this, click.x(), click.y(), click.button(), click.modifiers()))) {
            cir.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void onKeyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (EventBus.post(new ContainerEvent.Keyboard((Screen) (Object) this, input.key(), (char) input.input(), input.scancode(), input.modifiers()))) {
            cir.cancel();
        }
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    public void onDrawMouseoverTooltip(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            ScrollableTooltip.setSlot(hoveredSlot.index);
        }

        if (EventBus.post(new ContainerEvent.Render.Tooltip((Screen) (Object) this, context, mouseX, mouseY))) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseScrolled", at = @At("TAIL"))
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        EventBus.post(new ContainerEvent.MouseScroll((Screen) (Object) this, mouseX, mouseY, horizontalAmount, verticalAmount));
    }
}

