package com.github.noamm9.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface IMouseHandler {
    @Accessor("xpos")
    double getXPos();

    @Accessor("ypos")
    double getYPos();
}
