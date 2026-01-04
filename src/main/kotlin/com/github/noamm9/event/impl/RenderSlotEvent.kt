package com.github.noamm9.event.impl

import com.github.noamm9.event.Event
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.inventory.Slot

open class RenderSlotEvent(val context: GuiGraphics, val slot: Slot, val screenX: Int, val screenY: Int): Event(cancelable = true) {
    class Pre(graphics: GuiGraphics, slot: Slot, screenX: Int, screenY: Int): RenderSlotEvent(graphics, slot, screenX, screenY)
    class Post(graphics: GuiGraphics, slot: Slot, screenX: Int, screenY: Int): RenderSlotEvent(graphics, slot, screenX, screenY)
}