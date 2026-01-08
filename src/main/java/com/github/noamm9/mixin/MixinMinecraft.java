package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.visual.CpsDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow @Nullable public Screen screen;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        CpsDisplay.addLeftClick();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void onStartUseItem(CallbackInfo ci) {
        CpsDisplay.addRightClick();
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    public void onSetcreenHead(ClientLevel clientLevel, CallbackInfo ci) {
        System.gc();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof LevelLoadingScreen && !(this.screen instanceof DirectJoinServerScreen || this.screen instanceof JoinMultiplayerScreen)) {
            setScreen(null);
            ci.cancel();
        }
    }
}

