package com.github.noamm9.ui.nodification

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.ui.utils.Resolution
import com.github.noamm9.utils.ColorUtils.withAlpha
import com.github.noamm9.utils.render.Render2D
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color

object NotificationManager {
    private val notifications = mutableListOf<Notification>()
    private var lastFrameTime = System.currentTimeMillis()

    fun push(title: String, message: String, duration: Long = 5000L) {
        notifications.add(Notification(title, message, duration))
    }

    @JvmStatic
    fun render(ctx: GuiGraphics) {
        if (notifications.isEmpty()) return

        Resolution.refresh()
        val screenW = Resolution.width
        val screenH = Resolution.height

        val now = System.currentTimeMillis()
        val delta = now - lastFrameTime
        lastFrameTime = now

        val mX = Resolution.getMouseX(mc.mouseHandler.getScaledXPos(mc.window))
        val mY = Resolution.getMouseY(mc.mouseHandler.getScaledYPos(mc.window))

        var currentYOffset = 0f
        notifications.removeIf { it.isDead }

        for (notify in notifications) {
            val width = 175f
            val height = notify.height

            val isAlive = notify.elapsedTime < notify.duration
            notify.anim.update(if (isAlive) 1f else 0f)

            if (! isAlive && notify.anim.value <= 0.01f) {
                notify.isDead = true
                continue
            }

            val x = screenW - (width + 10f) * notify.anim.value
            val y = screenH - (height + 10f) - currentYOffset

            val isHovered = mX >= x && mX <= x + width && mY >= y && mY <= y + height

            if (! isHovered && isAlive) {
                notify.elapsedTime += delta
            }

            // 1. Draw Background & 2px Top Accent
            Render2D.drawRect(ctx, x, y, width, height, Color(20, 20, 20, 240))
            Render2D.drawRect(ctx, x, y, width, 2f, Style.accentColor)

            // 2. Draw Title
            Render2D.drawString(ctx, "§a${notify.title}", x + 10f, y + 8f, Color.GREEN)

            // 3. Draw Wrapped Lines (Now starting further down for padding)
            // y + 8 (title top) + 9 (font height) + 8 (new gap) = 25
            var lineY = y + 20f
            notify.wrappedLines.forEach { line ->
                ctx.drawString(mc.font, line, (x + 10f).toInt(), lineY.toInt(), Color.GRAY.rgb, true)
                lineY += mc.font.lineHeight + 1f
            }

            // 4. Progress Bar
            val progress = (notify.elapsedTime.toFloat() / notify.duration.toFloat()).coerceIn(0f, 1f)
            val barWidth = width * (1f - progress)
            if (isAlive) {
                Render2D.drawRect(ctx, x, y + height - 1.5f, barWidth, 1.5f, Style.accentColor.withAlpha(200))
            }

            currentYOffset += (height + 5f) * notify.anim.value
        }
    }
}