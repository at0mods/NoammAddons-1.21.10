package com.github.noamm9.ui.clickgui.componnents.impl

import com.github.noamm9.config.Savable
import com.github.noamm9.ui.clickgui.componnents.Setting
import com.github.noamm9.ui.clickgui.componnents.Style
import com.github.noamm9.ui.utils.Animation
import com.github.noamm9.utils.render.Render2D
import com.github.noamm9.utils.render.Render2D.width
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color
import kotlin.math.abs
import kotlin.math.round

open class SliderSetting<T: Number>(
    name: String,
    value: T,
    val min: T,
    val max: T,
    val step: T
): Setting<T>(name, value), Savable {
    private var dragging = false
    private val hoverAnim = Animation(200)
    private val sliderAnim = Animation(250, getPercent(value))

    override fun draw(ctx: GuiGraphics, mouseX: Int, mouseY: Int) {
        val isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
        val target = getPercent(value)

        if (dragging) {
            val pct = ((mouseX - (x + 8.0)) / (width - 16.0)).coerceIn(0.0, 1.0)
            val range = max.toDouble() - min.toDouble()
            val rawValue = min.toDouble() + (range * pct)
            value = snapToStep(rawValue)

            if (abs(sliderAnim.value - target) < 0.05f) sliderAnim.set(target)
        }

        hoverAnim.update(if (isHovered || dragging) 1f else 0f)
        sliderAnim.update(target)

        Style.drawBackground(ctx, x, y, width, 20f)
        Style.drawHoverBar(ctx, x, y, 20f, hoverAnim.value)
        Style.drawNudgedText(ctx, name, x + 8f, y + 2f, hoverAnim.value)

        val valStr = stringfy(value)
        Render2D.drawString(ctx, valStr, x + width - valStr.width() - 8f, y + 2f, Color(180, 180, 180))

        Style.drawSlider(ctx, x + 8f, y + 14f, width - 16f, sliderAnim.value, hoverAnim.value, Style.accentColor)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            dragging = true
            return true
        }
        return false
    }

    override fun mouseReleased(button: Int) {
        dragging = false
    }

    private fun getPercent(valIn: T): Float {
        val current = valIn.toDouble()
        val minD = min.toDouble()
        val maxD = max.toDouble()
        return ((current - minD) / (maxD - minD)).toFloat()
    }

    private fun snapToStep(rawDouble: Double): T {
        val minD = min.toDouble()
        val maxD = max.toDouble()
        val stepD = step.toDouble()

        if (stepD <= 0) return rawDouble.coerceIn(minD, maxD).convertToType()

        val steps = round((rawDouble - minD) / stepD)
        val steppedValue = (minD + (steps * stepD)).coerceIn(minD, maxD)

        return steppedValue.convertToType()
    }

    private fun Number.convertToType(): T {
        @Suppress("UNCHECKED_CAST")
        return when (min) {
            is Int -> toInt() as T
            is Long -> toLong() as T
            is Float -> toFloat() as T
            is Double -> toDouble() as T
            else -> this as T
        }
    }

    private fun stringfy(v: T): String {
        return when (v) {
            is Int, is Long -> "%d".format(v.toLong())
            else -> {
                val dVal = v.toDouble()
                val stepD = step.toDouble()
                if (stepD % 1.0 == 0.0) "%.0f".format(dVal)
                else {
                    val stepStr = stepD.toString()
                    val decimalPlaces = stepStr.substringAfter('.', "").length
                    "%.${decimalPlaces.coerceAtMost(2)}f".format(dVal)
                }
            }
        }
    }

    override fun write(): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement?) {
        element?.asNumber?.let {
            value = snapToStep(it.toDouble())
        }
    }
}