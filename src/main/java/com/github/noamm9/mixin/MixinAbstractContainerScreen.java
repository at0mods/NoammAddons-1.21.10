package com.github.noamm9.mixin;

import com.github.noamm9.NoammAddons;
import com.github.noamm9.event.EventBus;
import com.github.noamm9.event.impl.ContainerEvent;
import com.github.noamm9.features.impl.tweaks.ScrollableTooltip;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen {
    @Shadow @Nullable protected Slot hoveredSlot;

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    protected MixinAbstractContainerScreen(Component component) {
        super(component);
    }

    @Shadow
    protected abstract List<Component> getTooltipFromContainerItem(ItemStack itemStack);

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

    @Inject(method = "mouseScrolled", at = @At("TAIL"))
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        EventBus.post(new ContainerEvent.MouseScroll((Screen) (Object) this, mouseX, mouseY, horizontalAmount, verticalAmount));
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/ResourceLocation;)V", opcode = Opcodes.GETFIELD), cancellable = true)
    private void onRenderTooltipMerged(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci, @Local ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            ScrollableTooltip.setSlot(this.hoveredSlot.index);

            ContainerEvent.Render.Tooltip event = new ContainerEvent.Render.Tooltip(
                (Screen) (Object) this, context, stack, mouseX, mouseY, new ArrayList<>(getTooltipFromContainerItem(stack))
            );

            ci.cancel();

            if (!EventBus.post(event)) {
                context.setTooltipForNextFrame(
                    NoammAddons.mc.font,
                    event.getLore(),
                    stack.getTooltipImage(),
                    mouseX, mouseY,
                    stack.get(DataComponents.TOOLTIP_STYLE)
                );
            }
        }
    }

/*
    // 1. Fix Render Mouse Coordinates
    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int rescaleRenderMouseX(int x) {
        return (int) CustomContainer.transformMouse(x, this.width);
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private int rescaleRenderMouseY(int y) {
        return (int) CustomContainer.transformMouse(y, this.height);
    }

    // 2. Fix Tooltip Mouse Coordinates (usually called inside render)
    @ModifyVariable(method = "renderTooltip", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int rescaleTooltipX(int x) {
        return (int) CustomContainer.transformMouse(x, this.width);
    }

    @ModifyVariable(method = "renderTooltip", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private int rescaleTooltipY(int y) {
        return (int) CustomContainer.transformMouse(y, this.height);
    }

    // 3. Fix Mouse Clicks (Minecraft standard uses double mouseX, double mouseY)
    @ModifyVariable(method = "mouseClicked", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private MouseButtonEvent rescaleClickX(MouseButtonEvent value) {
        return CustomContainer.transformMouse(value, this.width, this.height);
    }

    @ModifyVariable(method = "mouseReleased", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private MouseButtonEvent rescaleReleaseX(MouseButtonEvent value) {
        return CustomContainer.transformMouse(value, this.width, this.height);
    }

    @ModifyVariable(method = "mouseDragged", at = @At("HEAD"), argsOnly = true)
    private MouseButtonEvent rescaleDragX(MouseButtonEvent value) {
        return CustomContainer.transformMouse(value, this.width, this.height);
    }*/
}


