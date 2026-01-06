package com.github.noamm9.mixin;

import com.github.noamm9.event.EventBus;
import com.github.noamm9.event.impl.ContainerEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    protected void onRenderPre(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (EventBus.post(new ContainerEvent.Render.Pre((Screen) (Object) this, context, mouseX, mouseY))) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    protected void onRenderPost(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        EventBus.post(new ContainerEvent.Render.Post((Screen) (Object) this, context, mouseX, mouseY));
    }
}
