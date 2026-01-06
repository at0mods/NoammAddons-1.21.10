package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot as McSlot

abstract class ContainerEvent(val screen: Screen): Event(cancelable = true) {
    abstract class Render(screen: Screen, val context: GuiGraphics): ContainerEvent(screen) {
        abstract class Slot(screen: Screen, ctx: GuiGraphics, val slot: McSlot): Render(screen, ctx) {
            class Pre(screen: Screen, ctx: GuiGraphics, slot: McSlot): Slot(screen, ctx, slot)
            class Post(screen: Screen, ctx: GuiGraphics, slot: McSlot): Slot(screen, ctx, slot)
        }

        class Pre(screen: Screen, ctx: GuiGraphics, val mouseX: Int, val mouseY: Int): Render(screen, ctx)
        class Tooltip(screen: Screen, ctx: GuiGraphics, val mouseX: Int, val mouseY: Int): Render(screen, ctx)
        class Post(screen: Screen, ctx: GuiGraphics, val mouseX: Int, val mouseY: Int): Render(screen, ctx)
    }

    class Open(screen: Screen): ContainerEvent(screen)
    class Close(screen: Screen): ContainerEvent(screen)

    class SlotClick(screen: Screen, val slot: McSlot, val button: Int, val clickType: ClickType): ContainerEvent(screen)
    class MouseClick(screen: Screen, val mouseX: Double, val mouseY: Double, val button: Int, val modifiers: Int): ContainerEvent(screen)

    class Keyboard(screen: Screen, val key: Int, val input: Char, val scancode: Int, val modifiers: Int): ContainerEvent(screen)
}