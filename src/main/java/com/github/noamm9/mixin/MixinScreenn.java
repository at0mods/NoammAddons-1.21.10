package com.github.noamm9.mixin;

import com.github.noamm9.features.impl.visual.CustomContainer;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Screen.class)
public abstract class MixinScreenn extends AbstractContainerEventHandler implements Renderable {
    @Shadow public int width;
    @Shadow public int height;

    @ModifyVariable(method = "renderWithTooltipAndSubtitles", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int fixRenderMouseX(int x) {
        if ((Object) this instanceof AbstractContainerScreen) {
            return (int) CustomContainer.transformMouse(x, this.width);
        }
        return x;
    }

    @ModifyVariable(method = "renderWithTooltipAndSubtitles", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private int fixRenderMouseY(int y) {
        if ((Object) this instanceof AbstractContainerScreen) {
            return (int) CustomContainer.transformMouse(y, this.height);
        }
        return y;
    }
}
