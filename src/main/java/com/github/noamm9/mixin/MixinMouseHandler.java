package com.github.noamm9.mixin;

import com.github.noamm9.event.EventBus;
import com.github.noamm9.event.impl.MouseClickEvent;
import com.github.noamm9.features.impl.visual.CustomContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long l, MouseButtonInfo mouseButtonInfo, int i, CallbackInfo ci) {
        if (EventBus.post(new MouseClickEvent(mouseButtonInfo.button(), i, mouseButtonInfo.modifiers()))) {
            ci.cancel();
        }
    }

    // Targets 'double d' and 'double e' in onButton (Clicks/Releases)
    @ModifyVariable(method = "onButton", at = @At(value = "STORE"), ordinal = 0)
    private double rescaleClickX(double d) {
        if (minecraft.screen instanceof AbstractContainerScreen) {
            return CustomContainer.transformMouse(d, minecraft.screen.width);
        }
        return d;
    }

    @ModifyVariable(method = "onButton", at = @At(value = "STORE"), ordinal = 1)
    private double rescaleClickY(double e) {
        if (minecraft.screen instanceof AbstractContainerScreen) {
            return CustomContainer.transformMouse(e, minecraft.screen.height);
        }
        return e;
    }

    // Targets 'double f' and 'double g' in handleAccumulatedMovement (Move/Drag)
    @ModifyVariable(method = "handleAccumulatedMovement", at = @At(value = "STORE"), ordinal = 0)
    private double rescaleMoveX(double f) {
        if (minecraft.screen instanceof AbstractContainerScreen) {
            return CustomContainer.transformMouse(f, minecraft.screen.width);
        }
        return f;
    }

    @ModifyVariable(method = "handleAccumulatedMovement", at = @At(value = "STORE"), ordinal = 1)
    private double rescaleMoveY(double g) {
        if (minecraft.screen instanceof AbstractContainerScreen) {
            return CustomContainer.transformMouse(g, minecraft.screen.height);
        }
        return g;
    }


    /*
     * Intercepts 'double d' (X) in onButton
     * Ordinal 0 = first double stored in the method

    @ModifyVariable(method = "onButton", at = @At(value = "STORE"), ordinal = 0)
    private double rescaleX(double d) {
        if (!hookMouse) return d;
        return d / getNoammScale();
    }

    /**
     * Intercepts 'double e' (Y) in onButton
     * Ordinal 1 = second double stored in the method

    @ModifyVariable(method = "onButton", at = @At(value = "STORE"), ordinal = 1)
    private double rescaleY(double e) {
        if (!hookMouse) return e;
        return e / getNoammScale();
    }

    /*
    @Inject(method = "onScroll", at = @At(value = "HEAD"), cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {

    }*/
}